package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> scenario =
            new ActivityScenarioRule<>(ProfileActivity.class);

    /**
     * Test 1: Verify that ProfileActivity loads correctly and displays all UI elements.
     */
    @Test
    public void testProfileActivityLoads() {
        // Check that all profile display TextViews are visible
        onView(withId(R.id.name_display)).check(matches(isDisplayed()));
        onView(withId(R.id.email_display)).check(matches(isDisplayed()));
        onView(withId(R.id.phone_display)).check(matches(isDisplayed()));

        // Check that the edit button is visible
        onView(withId(R.id.edit_button)).check(matches(isDisplayed()));

        // Check that bottom navigation is visible
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Verify that user name is displayed with the correct label.
     */
    @Test
    public void testNameDisplayShown() {
        // Check that the name display contains the "Name:" label
        onView(withId(R.id.name_display))
                .check(matches(withText(containsString("Name:"))));
    }

    /**
     * Test 3: Verify that user email is displayed with the correct label.
     */
    @Test
    public void testEmailDisplayShown() {
        // Check that the email display contains the "Email:" label
        onView(withId(R.id.email_display))
                .check(matches(withText(containsString("Email:"))));
    }

    /**
     * Test 4: Verify that phone number is displayed with the correct label.
     * This should show either the phone number or "Not provided" if unavailable.
     */
    @Test
    public void testPhoneDisplayShown() {
        // Check that the phone display contains the "Phone Number:" label
        onView(withId(R.id.phone_display))
                .check(matches(withText(containsString("Phone Number:"))));
    }

    /**
     * Test 5: Verify that clicking the edit button navigates to UpdateProfileActivity.
     */
    @Test
    public void testEditButtonOpensUpdateProfile() {
        // Click the edit button
        onView(withId(R.id.edit_button)).perform(click());

    }
}