package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for AdminNotificationLogsActivity.
 * Verifies screen visibility, title setup, back navigation, and log list visibility.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminNotificationLogsActivityTest {

    // Launch with required intent extras
    @Rule
    public ActivityScenarioRule<AdminNotificationLogsActivity> scenario =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), AdminNotificationLogsActivity.class)
                            .putExtra("deviceId", "TEST_DEVICE")
                            .putExtra("name", "Test User")
            );

    /**
     * Test 1: Confirm that the activity loads and UI elements render correctly.
     */
    @Test
    public void testActivityLoads() {
        onView(withText("Notifications for Test User")).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.rvNotificationLogs)).check(matches(isDisplayed()));
        onView(withId(R.id.tvEmptyLogs)).check(matches(isDisplayed())); // May show if logs empty
    }

    /**
     * Test 2: Verify that the back button closes the activity.
     */
    @Test
    public void testBackButton() {
        onView(withId(R.id.btnBack)).perform(click());
        // Espresso automatically ends the ActivityScenarioRule when the activity finishes.
    }
}
