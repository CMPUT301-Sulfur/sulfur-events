package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class OrganizerEditEventTest {

    @Test
    public void TestEditEvent() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // 1. Setup: Ensure Profile exists (Activity needs this to get the email)
        String deviceId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Fallback for emulators that might return null
        if (deviceId == null) deviceId = "TEST_DEVICE_ID";

        User mockUser = new User();
        mockUser.setName("Test Organizer");
        mockUser.setEmail("organizer@test.com");

        com.google.android.gms.tasks.Tasks.await(
                db.collection("Profiles").document(deviceId).set(mockUser)
        );

        // 2. Setup: Create the initial Event in Firestore
        String eventId = "testEdit_" + System.currentTimeMillis();
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", "Original Name");
        eventData.put("description", "Original Desc");
        eventData.put("startDate", "01/01/2025");
        eventData.put("endDate", "02/01/2025");
        eventData.put("location", "Edmonton");
        eventData.put("limitGuests", "10");
        eventData.put("waitingListLimit", "5");

        com.google.android.gms.tasks.Tasks.await(
                db.collection("Events").document(eventId).set(eventData)
        );

        // 3. Prepare Intent (Simulating "Edit" mode)
        Intent intent = new Intent(context, OrganizerCreateEventActivity.class);
        intent.putExtra("isEdit", true);
        intent.putExtra("eventId", eventId);
        intent.putExtra("eventName", "Original Name");
        intent.putExtra("description", "Original Desc");
        intent.putExtra("startDate", "01/01/2025");
        intent.putExtra("endDate", "02/01/2025");
        intent.putExtra("location", "Edmonton");
        intent.putExtra("capacity", "10");
        intent.putExtra("waitingListLimit", "5");

        // 4. Launch Activity MANUALLY (Safe because this class has no @Rule)
        try (ActivityScenario<OrganizerCreateEventActivity> scenario = ActivityScenario.launch(intent)) {

            // CRITICAL: Wait for onCreate's async Firestore fetch to get the email.
            SystemClock.sleep(3000);

            // 5. Verify UI is in Edit Mode
            onView(withId(R.id.etEventName)).check(matches(withText("Original Name")));
            onView(withId(R.id.GenerateEventButton)).check(matches(withText("Save Changes")));

            // 6. Perform Edit: Change the Event Name
            onView(withId(R.id.etEventName))
                    .perform(scrollTo(), replaceText("UPDATED EVENT NAME"), closeSoftKeyboard());

            // 7. Click Save
            onView(withId(R.id.GenerateEventButton)).perform(scrollTo(), click());

            // 8. Wait for Firestore update
            SystemClock.sleep(3000);

            // 9. Verify Firestore Update
            DocumentSnapshot doc = com.google.android.gms.tasks.Tasks.await(
                    db.collection("Events").document(eventId).get()
            );

            assertTrue("Document should exist", doc.exists());
            assertTrue("Event name should be updated",
                    "UPDATED EVENT NAME".equals(doc.getString("eventName")));
        }
    }
}