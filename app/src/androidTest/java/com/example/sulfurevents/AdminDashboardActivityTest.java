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
 * Instrumented UI test for AdminDashboardActivity.
 * Ensures that each button correctly launches its intended admin management screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminDashboardActivityTest {

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> scenario =
            new ActivityScenarioRule<>(AdminDashboardActivity.class);

    /**
     * Test 1: Verify that AdminDashboardActivity loads correctly.
     */
    @Test
    public void testDashboardLoads() {
        // check that all three buttons are visible
        onView(withId(R.id.btnManageEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnManageProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.btnManageImages)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Check that clicking "Manage Events" opens the Manage Events screen.
     */
    @Test
    public void testOpenManageEvents() {
        onView(withId(R.id.btnManageEvents)).perform(click());
        onView(withText("Manage Events")).check(matches(isDisplayed()));
    }

    /**
     * Test 3: Check that clicking "Manage Profiles" opens the Manage Profiles screen.
     */
    @Test
    public void testOpenManageProfiles() {
        onView(withId(R.id.btnManageProfiles)).perform(click());
        onView(withText("Manage Profiles")).check(matches(isDisplayed()));
    }

    /**
     * Test 4: Check that clicking "Manage Images" opens the Manage Images screen.
     */
    @Test
    public void testOpenManageImages() {
        onView(withId(R.id.btnManageImages)).perform(click());
        onView(withText("Manage Images")).check(matches(isDisplayed()));
    }
}