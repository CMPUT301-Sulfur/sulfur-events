package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

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
}