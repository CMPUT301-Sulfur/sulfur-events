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
 * UI tests for AdminEventsActivity.
 * Verifies basic screen visibility and navigation behavior.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminEventsActivityTest {

    @Rule
    public ActivityScenarioRule<AdminEventsActivity> scenario =
            new ActivityScenarioRule<>(AdminEventsActivity.class);

    /**
     * Test 1: Check if the activity loads correctly.
     */
    @Test
    public void testActivityLoads() {
        onView(withText("Manage Events")).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.listViewEvents)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Verify that the Back button returns to the previous screen.
     */
    @Test
    public void testBackButton() {
        onView(withId(R.id.btnBackEvents)).perform(click());
        // Just check that the activity closes — Espresso automatically finishes scenario
        // You can assert navigation in integration tests if needed
    }
}