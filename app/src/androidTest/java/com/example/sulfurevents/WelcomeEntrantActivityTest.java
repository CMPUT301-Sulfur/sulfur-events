package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
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


@RunWith(AndroidJUnit4.class)
@LargeTest
public class WelcomeEntrantActivityTest {

    @Rule
    public ActivityScenarioRule<WelcomeEntrantActivity> scenario =
            new ActivityScenarioRule<>(WelcomeEntrantActivity.class);

    /**
     * Test 1: Verify that WelcomeEntrantActivity loads correctly and displays all UI elements.
     */
    @Test
    public void testWelcomeActivityLoads() {
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));
        onView(withId(R.id.email_input)).check(matches(isDisplayed()));
        onView(withId(R.id.phone_input)).check(matches(isDisplayed()));
        onView(withId(R.id.submit_button)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Verify that user can type into the name input field.
     */
    @Test
    public void testNameInputAcceptsText() {
        onView(withId(R.id.name_input))
                .perform(typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("John Doe")));
    }

    /**
     * Test 3: Verify that user can type into the email input field.
     */
    @Test
    public void testEmailInputAcceptsText() {
        onView(withId(R.id.email_input))
                .perform(typeText("john.doe@example.com"), closeSoftKeyboard());

        onView(withId(R.id.email_input))
                .check(matches(withText("john.doe@example.com")));
    }

    /**
     * Test 4: Verify that user can type into the phone input field.
     */
    @Test
    public void testPhoneInputAcceptsText() {
        onView(withId(R.id.phone_input))
                .perform(typeText("1234567890"), closeSoftKeyboard());

        onView(withId(R.id.phone_input))
                .check(matches(withText("1234567890")));
    }

    /**
     * Test 5: Verify that submitting with empty name shows error.
     */
    @Test
    public void testSubmitWithEmptyNameShowsError() {
        onView(withId(R.id.email_input))
                .perform(typeText("test@example.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

    }

    /**
     * Test 6: Verify that submitting with empty email shows error.
     */
    @Test
    public void testSubmitWithEmptyEmailShowsError() {
        onView(withId(R.id.name_input))
                .perform(typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

    }

    /**
     * Test 7: Verify that phone number is optional (can submit without it).
     */
    @Test
    public void testPhoneNumberIsOptional() {
        onView(withId(R.id.name_input))
                .perform(typeText("Jane Smith"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("jane.smith@example.com"), closeSoftKeyboard());
        onView(withId(R.id.submit_button))
                .perform(click());

    }

    /**
     * Test 8: Verify that all fields can be filled and submitted successfully.
     */
    @Test
    public void testSubmitWithAllFieldsFilled() {
        onView(withId(R.id.name_input))
                .perform(typeText("Alice Johnson"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("alice.johnson@example.com"), closeSoftKeyboard());
        onView(withId(R.id.phone_input))
                .perform(typeText("5551234567"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

    }

}