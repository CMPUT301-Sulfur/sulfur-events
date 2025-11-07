package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;



import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OrganizerCreateEventTest {

    @Rule
    public ActivityScenarioRule<OrganizerActivity> scenario = new
            ActivityScenarioRule<>(OrganizerActivity.class);



    @Test
    public void TestCreateEventButton() {
        onView(withId(R.id.CreateEventButton)).perform(click());

        onView(withId(R.id.etEventName)).perform(typeText("Test File Event test"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(typeText("This is a test run by the test file"), closeSoftKeyboard());
        onView(withId(R.id.etStartDate)).perform(typeText("11/7/2025"), closeSoftKeyboard());
        onView(withId(R.id.etEndDate)).perform(typeText("11/7/2025"), closeSoftKeyboard());
        onView(withId(R.id.etLocation)).perform(typeText("Edmonton"), closeSoftKeyboard());
        onView(withId(R.id.etLimitGuests)).perform(typeText("2"), closeSoftKeyboard());
        onView(withId(R.id.organizerEmail)).perform(typeText("Test@gmail.com"), closeSoftKeyboard());

        SystemClock.sleep(2500);

    }






}
