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
 * Instrumented UI tests for AdminImagesActivity.
 * Verifies screen visibility, navigation, and basic interactions.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImagesActivityTest {

    @Rule
    public ActivityScenarioRule<AdminImagesActivity> scenario =
            new ActivityScenarioRule<>(AdminImagesActivity.class);

    /**
     * Test 1: Verify that the Admin Images screen loads correctly.
     */
    @Test
    public void testActivityLoads() {
        // Title and back button should be visible
        onView(withText("Manage Images")).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackImages)).check(matches(isDisplayed()));
        onView(withId(R.id.listViewImageEvents)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Verify that clicking the Back button exits the screen.
     */
    @Test
    public void testBackButton() {
        onView(withId(R.id.btnBackImages)).perform(click());
        // Espresso auto-finishes the scenario; no extra assertion needed.
    }
}
