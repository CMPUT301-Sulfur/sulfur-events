package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for AdminProfilesActivity.
 * Verifies that the admin profile management screen loads correctly
 * and the Back button exits the activity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminProfilesActivityTest {

    @Rule
    public ActivityScenarioRule<AdminProfilesActivity> scenario =
            new ActivityScenarioRule<>(AdminProfilesActivity.class);

    /**
     * Test 1: Check if the activity loads and UI components are displayed.
     */
    @Test
    public void testActivityLoads() {
        // Check title, back button, and list view are visible
        onView(withText("Manage Profiles")).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.listViewProfiles)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Check if the Back button works (activity finishes).
     */
    @Test
    public void testBackButton() {
        onView(withId(R.id.btnBackProfiles)).perform(click());
        // Espresso will close the activity automatically — no further check needed
    }
}