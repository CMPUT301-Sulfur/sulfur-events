package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.media.metrics.Event;
import android.net.Uri;
import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;


public class OrganizerCreateEventTest {

    @Rule
    public ActivityScenarioRule<OrganizerActivity> scenario = new
            ActivityScenarioRule<>(OrganizerActivity.class);

    @Rule
    public IntentsTestRule<OrganizerActivity> intentsRule =
            new IntentsTestRule<>(OrganizerActivity.class);




    @Test
    public void TestCreateEventButton() {
        onView(withId(R.id.CreateEventButton)).perform(click());

        onView(withId(R.id.etEventName)).perform(typeText("Test File Event test"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(typeText("This is a test run by the test file"), closeSoftKeyboard());


        // Start date
        onView(withId(R.id.etStartDate)).perform(click());
        onView(withText("OK")).perform(click());

        // End date
        onView(withId(R.id.etEndDate)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.etLocation)).perform(typeText("Edmonton"), closeSoftKeyboard());
        onView(withId(R.id.switchGeolocation)).perform(click());
        onView(withId(R.id.etLimitGuests)).perform(typeText("2"), closeSoftKeyboard());
        onView(withId(R.id.etWaitingListLimit)).perform(typeText("1"), closeSoftKeyboard());
        onView(withId(R.id.etWaitingListLimit)).perform(pressImeActionButton());

        //SystemClock.sleep(2500);
        onView(withId(R.id.GenerateEventButton)).perform(scrollTo()).perform(click());

        SystemClock.sleep(2500);

    }


    @Test
    public void TestSampleEntrants() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        String eventId = "testEvent_sampling";


        int totalEntrants = 10;
        for (int i = 0; i < totalEntrants; i++) {
            String id = "DEV_" + i;

            User u = new User();
            u.setName("User " + i);
            u.setEmail("u" + i + "@mail.com");
            db.collection("Profiles").document(id).set(u);
        }


        Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("eventName", "Sampling Test Event");
        eventData.put("description", "desc");
        eventData.put("startDate", "now");
        eventData.put("endDate", "later");
        eventData.put("location", "Edmonton");
        eventData.put("limitGuests", "2");
        //eventData.put("organizerEmail", "test@mail.com");
        eventData.put("waiting_list", java.util.Arrays.asList(
                "DEV_0","DEV_1","DEV_2","DEV_3","DEV_4","DEV_5","DEV_6","DEV_7","DEV_8","DEV_9"
        ));
        eventData.put("invited_list", new java.util.ArrayList<>());
        eventData.put("enrolled_list", new java.util.ArrayList<>());
        eventData.put("cancelled_list", new java.util.ArrayList<>());


        com.google.android.gms.tasks.Tasks.await(
                db.collection("Events").document(eventId).set(eventData)
        );


        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                OrganizerInvitedActivity.class);
        intent.putExtra("eventId", eventId);

        ActivityScenario<OrganizerInvitedActivity> scenario = ActivityScenario.launch(intent);
        scenario.moveToState(Lifecycle.State.RESUMED);

        SystemClock.sleep(1500);


        onView(withId(R.id.btnDrawOneReplacement)).perform(click());

        SystemClock.sleep(1000);


        DocumentSnapshot doc = com.google.android.gms.tasks.Tasks.await(
                db.collection("Events").document(eventId).get()
        );

        java.util.List<String> invited = (java.util.List<String>) doc.get("invited_list");
        assertTrue(invited != null && invited.size() >= 1);
    }

}
