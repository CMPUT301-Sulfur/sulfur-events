package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * Instrumentation test suite for verifying notification-related functionality
 * for entrants in the Event Lottery System.
 * <p>
 * This class covers the user stories:
 * <ul>
 *     <li>US 01.04.01 – Entrant receives a notification when selected ("INVITED")</li>
 *     <li>US 01.04.02 – Entrant receives a notification when not selected ("NOT_SELECTED")</li>
 *     <li>US 01.05.03 – Entrant declines an invitation and a replacement is drawn</li>
 * </ul>
 * <p>
 * Each test interacts with the Firestore emulator / live backend to ensure
 * notification documents are created under the correct collections.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationsFlowTest {

    /**
     * Test: US 01.04.01
     * <p>
     * Verifies that when an entrant is invited to an event, a corresponding
     * "INVITED" notification is created in Firestore under
     * {@code Profiles/<entrantId>/notifications}.
     * <p>
     * Steps:
     * <ol>
     *     <li>Create a test event with two entrants in the waiting list.</li>
     *     <li>Simulate drawing one entrant from the waiting list.</li>
     *     <li>Verify that the selected entrant receives an INVITED notification.</li>
     * </ol>
     * Expected result: The selected entrant's profile has one new notification
     * document where {@code type = "INVITED"}.
     */
    @Test
    public void testInvitedNotificationCreated() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String invitedId = "DEV_INVITED_1";
        String waitingId = "DEV_WAITING_1";

        // Prepare sample profiles
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", "Test User");
        Tasks.await(db.collection("Profiles").document(invitedId).set(profile));
        Tasks.await(db.collection("Profiles").document(waitingId).set(profile));

        // Prepare test event
        String eventId = "event_for_invite_test";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Invite Test Event");
        event.put("limitGuests", "1");
        event.put("waiting_list", Arrays.asList(invitedId, waitingId));
        event.put("invited_list", new ArrayList<String>());
        event.put("enrolled_list", new ArrayList<String>());
        event.put("cancelled_list", new ArrayList<String>());
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Simulate the organizer drawing one entrant
        TaskHelpers.inviteFirstWaitingUser(db, eventId);
        SystemClock.sleep(1500);

        // Verify INVITED notification created
        List<DocumentSnapshot> notifDocs = Tasks.await(
                db.collection("Profiles")
                        .document(invitedId)
                        .collection("notifications")
                        .get()
        ).getDocuments();

        boolean foundInvited = notifDocs.stream()
                .anyMatch(doc -> "INVITED".equals(doc.getString("type")));

        assertTrue("Expected an INVITED notification for invited user", foundInvited);
    }

    /**
     * Test: US 01.04.02
     * <p>
     * Verifies that when an event reaches full capacity, all remaining entrants
     * on the waiting list receive a "NOT_SELECTED" notification.
     * <p>
     * Steps:
     * <ol>
     *     <li>Create a full event with {@code enrolled_list.size() == limitGuests}.</li>
     *     <li>Invoke {@code checkAndNotifyNotSelectedIfFull()} logic.</li>
     *     <li>Verify that all users on the waiting list have NOT_SELECTED notifications.</li>
     * </ol>
     * Expected result: Each waiting user receives a NOT_SELECTED notification.
     */
    @Test
    public void testNotSelectedWhenFull() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String waiting1 = "DEV_WAITING_A";
        String waiting2 = "DEV_WAITING_B";
        String enrolled1 = "DEV_ENROLLED_A";
        String enrolled2 = "DEV_ENROLLED_B";

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", "Test User");
        for (String id : Arrays.asList(waiting1, waiting2, enrolled1, enrolled2)) {
            Tasks.await(db.collection("Profiles").document(id).set(profile));
        }

        // Create event at capacity
        String eventId = "event_full_test";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Full Event");
        event.put("limitGuests", "2");
        event.put("waiting_list", Arrays.asList(waiting1, waiting2));
        event.put("enrolled_list", Arrays.asList(enrolled1, enrolled2));
        event.put("invited_list", new ArrayList<String>());
        event.put("cancelled_list", new ArrayList<String>());
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Call helper logic
        TaskHelpers.checkAndNotifyNotSelectedIfFull(db, eventId);
        SystemClock.sleep(1500);

        // Verify both waiting users got NOT_SELECTED
        boolean w1NotSel = containsType(db, waiting1, "NOT_SELECTED");
        boolean w2NotSel = containsType(db, waiting2, "NOT_SELECTED");

        assertTrue("waiting1 should have NOT_SELECTED", w1NotSel);
        assertTrue("waiting2 should have NOT_SELECTED", w2NotSel);
    }

    /**
     * Test: US 01.05.03
     * <p>
     * Verifies that when an invited entrant declines an invitation,
     * the next person in the waiting list is automatically invited
     * and receives an "INVITED" notification.
     * <p>
     * Steps:
     * <ol>
     *     <li>Create an event with one invited user and one waiting user.</li>
     *     <li>Simulate decline by moving the invited user to {@code cancelled_list}.</li>
     *     <li>Call {@code drawReplacementAndNotify()}.</li>
     *     <li>Verify that the waiting user now has an INVITED notification.</li>
     * </ol>
     * Expected result: The next waiting user is moved to invited_list and receives an INVITED notification.
     */
    @Test
    public void testDeclineInvitesReplacement() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String invitedNow = "DEV_INVITED_NOW";
        String nextWaiting = "DEV_NEXT_WAITING";

        Map<String, Object> prof = new HashMap<>();
        prof.put("name", "User");
        Tasks.await(db.collection("Profiles").document(invitedNow).set(prof));
        Tasks.await(db.collection("Profiles").document(nextWaiting).set(prof));

        // Create event
        String eventId = "event_decline_test";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Decline Draw Event");
        event.put("limitGuests", "1");
        event.put("waiting_list", Arrays.asList(nextWaiting));
        event.put("invited_list", Arrays.asList(invitedNow));
        event.put("enrolled_list", new ArrayList<String>());
        event.put("cancelled_list", new ArrayList<String>());
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Simulate decline
        Tasks.await(
                db.collection("Events").document(eventId)
                        .update(
                                "invited_list", new ArrayList<String>(),
                                "cancelled_list", Arrays.asList(invitedNow)
                        )
        );

        // Draw replacement
        TaskHelpers.drawReplacementAndNotify(db, eventId);
        SystemClock.sleep(1500);

        // Verify waiting user is invited
        boolean foundInvited = containsType(db, nextWaiting, "INVITED");
        assertTrue("next waiting user should get INVITED notification", foundInvited);
    }

    /** Utility method to check if a user’s notifications contain a given type. */
    private boolean containsType(FirebaseFirestore db, String userId, String type) throws Exception {
        List<DocumentSnapshot> docs = Tasks.await(
                db.collection("Profiles").document(userId)
                        .collection("notifications").get()
        ).getDocuments();
        for (DocumentSnapshot d : docs) {
            if (type.equals(d.getString("type"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test: US 01.04.03 – Opt-out for invitations
     *
     * Verifies that when an entrant has notifications disabled
     * (notificationsEnabled = false in their profile), they do NOT
     * receive an INVITED notification even if they are selected.
     *
     * Steps:
     * 1) Create a profile with notificationsEnabled = false.
     * 2) Create an event where that user is first in waiting_list.
     * 3) Call TaskHelpers.inviteFirstWaitingUser().
     * 4) Verify no INVITED notification is created for that user.
     */
    @Test
    public void testOptedOutUserDoesNotReceiveInvitedNotification() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String optedOutId = "DEV_OPT_OUT_INVITED";
        String otherWaiting = "DEV_OTHER_WAITING";

        // Profiles: opted-out user and a normal user
        Map<String, Object> optedProfile = new HashMap<>();
        optedProfile.put("name", "Opted Out User");
        optedProfile.put("notificationsEnabled", false); // key part

        Map<String, Object> normalProfile = new HashMap<>();
        normalProfile.put("name", "Normal User");

        Tasks.await(db.collection("Profiles").document(optedOutId).set(optedProfile));
        Tasks.await(db.collection("Profiles").document(otherWaiting).set(normalProfile));

        // Event where opted-out user is selected first
        String eventId = "event_opt_out_invited_test";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Opt-Out Invite Event");
        event.put("limitGuests", "1");
        event.put("waiting_list", Arrays.asList(optedOutId, otherWaiting));
        event.put("invited_list", new ArrayList<String>());
        event.put("enrolled_list", new ArrayList<String>());
        event.put("cancelled_list", new ArrayList<String>());
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Simulate organizer drawing first waiting user
        TaskHelpers.inviteFirstWaitingUser(db, eventId);
        SystemClock.sleep(1500);

        // Opted-out user should NOT get INVITED
        boolean hasInvited = containsType(db, optedOutId, "INVITED");
        assertFalse("Opted-out user should NOT receive INVITED notification", hasInvited);
    }

    /**
     * Test: US 01.04.03 – Opt-out for NOT_SELECTED
     *
     * Verifies that when an event reaches full capacity and notifications
     * are sent with type NOT_SELECTED, users who opted out of notifications
     * do NOT receive that notification, while others still do.
     *
     * Steps:
     * 1) Create two waiting users: one opted-out, one normal.
     * 2) Create a full event (enrolled == limitGuests).
     * 3) Call TaskHelpers.checkAndNotifyNotSelectedIfFull().
     * 4) Verify only the normal user gets NOT_SELECTED.
     */
    @Test
    public void testOptedOutUserDoesNotReceiveNotSelected() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String optedOutWaiting = "DEV_OPT_OUT_WAITING";
        String normalWaiting = "DEV_NORMAL_WAITING";
        String enrolled1 = "DEV_ENROLLED_OPT";
        String enrolled2 = "DEV_ENROLLED_NORM";

        Map<String, Object> optedProfile = new HashMap<>();
        optedProfile.put("name", "Opted Out User");
        optedProfile.put("notificationsEnabled", false);

        Map<String, Object> normalProfile = new HashMap<>();
        normalProfile.put("name", "Normal User");

        // Profiles
        Tasks.await(db.collection("Profiles").document(optedOutWaiting).set(optedProfile));
        Tasks.await(db.collection("Profiles").document(normalWaiting).set(normalProfile));
        Tasks.await(db.collection("Profiles").document(enrolled1).set(normalProfile));
        Tasks.await(db.collection("Profiles").document(enrolled2).set(normalProfile));

        // Full event
        String eventId = "event_opt_out_not_selected_test";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Opt-Out NotSelected Event");
        event.put("limitGuests", "2");
        event.put("waiting_list", Arrays.asList(optedOutWaiting, normalWaiting));
        event.put("enrolled_list", Arrays.asList(enrolled1, enrolled2));
        event.put("invited_list", new ArrayList<String>());
        event.put("cancelled_list", new ArrayList<String>());
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Trigger NOT_SELECTED logic
        TaskHelpers.checkAndNotifyNotSelectedIfFull(db, eventId);
        SystemClock.sleep(1500);

        boolean optedHasNotSelected = containsType(db, optedOutWaiting, "NOT_SELECTED");
        boolean normalHasNotSelected = containsType(db, normalWaiting, "NOT_SELECTED");

        assertFalse("Opted-out waiting user should NOT receive NOT_SELECTED", optedHasNotSelected);
        assertTrue("Normal waiting user SHOULD receive NOT_SELECTED", normalHasNotSelected);
    }

    /**
     * Test: US 02.07.01 – Organizer broadcasts to all waiting entrants.
     *
     * Verifies that when the organizer sends a broadcast to the waiting list,
     * all waiting entrants with notifications enabled receive a
     * WAITING_BROADCAST notification, and opted-out entrants do NOT.
     */
    @Test
    public void testBroadcastToWaitingEntrantsRespectsOptOut() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String waitingOptIn = "DEV_WAITING_BCAST_IN";
        String waitingOptOut = "DEV_WAITING_BCAST_OUT";

        Map<String, Object> optInProfile = new HashMap<>();
        optInProfile.put("name", "Waiting Opt-in");

        Map<String, Object> optOutProfile = new HashMap<>();
        optOutProfile.put("name", "Waiting Opt-out");
        optOutProfile.put("notificationsEnabled", false);

        Tasks.await(db.collection("Profiles").document(waitingOptIn).set(optInProfile));
        Tasks.await(db.collection("Profiles").document(waitingOptOut).set(optOutProfile));

        String eventId = "event_waiting_broadcast";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Waiting Broadcast Event");
        event.put("limitGuests", "5");
        event.put("waiting_list", Arrays.asList(waitingOptIn, waitingOptOut));
        event.put("invited_list", new ArrayList<String>());
        event.put("enrolled_list", new ArrayList<String>());
        event.put("cancelled_list", new ArrayList<String>());
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Organizer broadcast to waiting entrants
        TaskHelpers.broadcastToWaitingEntrants(db, eventId, "Message to waiting entrants");
        SystemClock.sleep(1500);

        boolean inHas = containsType(db, waitingOptIn, "WAITING_BROADCAST");
        boolean outHas = containsType(db, waitingOptOut, "WAITING_BROADCAST");

        assertTrue("Opted-in waiting entrant should receive WAITING_BROADCAST", inHas);
        assertFalse("Opted-out waiting entrant should NOT receive WAITING_BROADCAST", outHas);
    }

    /**
     * Test: US 02.07.02 – Organizer broadcasts to all selected entrants.
     *
     * Verifies that when the organizer sends a broadcast to selected entrants
     * (invited/enrolled), only opted-in selected entrants receive
     * SELECTED_BROADCAST notifications.
     */
    @Test
    public void testBroadcastToSelectedEntrantsRespectsOptOut() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String invitedOptIn = "DEV_INVITED_BCAST_IN";
        String invitedOptOut = "DEV_INVITED_BCAST_OUT";

        Map<String, Object> optInProfile = new HashMap<>();
        optInProfile.put("name", "Invited Opt-in");

        Map<String, Object> optOutProfile = new HashMap<>();
        optOutProfile.put("name", "Invited Opt-out");
        optOutProfile.put("notificationsEnabled", false);

        Tasks.await(db.collection("Profiles").document(invitedOptIn).set(optInProfile));
        Tasks.await(db.collection("Profiles").document(invitedOptOut).set(optOutProfile));

        String eventId = "event_selected_broadcast";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Selected Broadcast Event");
        event.put("limitGuests", "2");
        event.put("waiting_list", new ArrayList<String>());
        event.put("invited_list", Arrays.asList(invitedOptIn, invitedOptOut));
        event.put("enrolled_list", new ArrayList<String>());
        event.put("cancelled_list", new ArrayList<String>());
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Organizer broadcast to selected entrants
        TaskHelpers.broadcastToInvitedEntrants(db, eventId, "Message to selected entrants");
        SystemClock.sleep(1500);

        boolean inHas = containsType(db, invitedOptIn, "SELECTED_BROADCAST");
        boolean outHas = containsType(db, invitedOptOut, "SELECTED_BROADCAST");

        assertTrue("Opted-in selected entrant should receive SELECTED_BROADCAST", inHas);
        assertFalse("Opted-out selected entrant should NOT receive SELECTED_BROADCAST", outHas);
    }

    /**
     * Test: US 02.07.03 – Organizer broadcasts to all cancelled entrants.
     *
     * Verifies that when the organizer sends a broadcast to cancelled entrants,
     * only opted-in cancelled entrants receive CANCELLED_BROADCAST notifications.
     */
    @Test
    public void testBroadcastToCancelledEntrantsRespectsOptOut() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String cancelledOptIn = "DEV_CANCELLED_BCAST_IN";
        String cancelledOptOut = "DEV_CANCELLED_BCAST_OUT";

        Map<String, Object> optInProfile = new HashMap<>();
        optInProfile.put("name", "Cancelled Opt-in");

        Map<String, Object> optOutProfile = new HashMap<>();
        optOutProfile.put("name", "Cancelled Opt-out");
        optOutProfile.put("notificationsEnabled", false);

        Tasks.await(db.collection("Profiles").document(cancelledOptIn).set(optInProfile));
        Tasks.await(db.collection("Profiles").document(cancelledOptOut).set(optOutProfile));

        String eventId = "event_cancelled_broadcast";
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", "Cancelled Broadcast Event");
        event.put("limitGuests", "2");
        event.put("waiting_list", new ArrayList<String>());
        event.put("invited_list", new ArrayList<String>());
        event.put("enrolled_list", new ArrayList<String>());
        event.put("cancelled_list", Arrays.asList(cancelledOptIn, cancelledOptOut));
        Tasks.await(db.collection("Events").document(eventId).set(event));

        // Organizer broadcast to cancelled entrants
        TaskHelpers.broadcastToCancelledEntrants(db, eventId, "Message to cancelled entrants");
        SystemClock.sleep(1500);

        boolean inHas = containsType(db, cancelledOptIn, "CANCELLED_BROADCAST");
        boolean outHas = containsType(db, cancelledOptOut, "CANCELLED_BROADCAST");

        assertTrue("Opted-in cancelled entrant should receive CANCELLED_BROADCAST", inHas);
        assertFalse("Opted-out cancelled entrant should NOT receive CANCELLED_BROADCAST", outHas);
    }

}
