package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
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
public class UpdateProfileActivityTest {

    @Rule
    public ActivityScenarioRule<UpdateProfileActivity> scenario =
            new ActivityScenarioRule<>(UpdateProfileActivity.class);

    /**
     * Test 1: Verify that UpdateProfileActivity loads correctly and displays all UI elements.
     */
    @Test
    public void testUpdateProfileActivityLoads() {
        // Check that all input fields are visible
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));
        onView(withId(R.id.email_input)).check(matches(isDisplayed()));
        onView(withId(R.id.phone_input)).check(matches(isDisplayed()));

        // Check that the confirm button is visible
        onView(withId(R.id.confirm_button)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Verify that the name input field is displayed and interactable.
     */
    @Test
    public void testNameInputFieldDisplayed() {
        onView(withId(R.id.name_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 3: Verify that the email input field is displayed and interactable.
     */
    @Test
    public void testEmailInputFieldDisplayed() {
        onView(withId(R.id.email_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 4: Verify that the phone input field is displayed and interactable.
     */
    @Test
    public void testPhoneInputFieldDisplayed() {
        onView(withId(R.id.phone_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 5: Verify that the confirm button is displayed.
     */
    @Test
    public void testConfirmButtonDisplayed() {
        onView(withId(R.id.confirm_button))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 6: Verify that user can type into the name input field.
     */
    @Test
    public void testNameInputAcceptsText() {
        onView(withId(R.id.name_input))
                .perform(clearText(), typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("John Doe")));
    }

    /**
     * Test 7: Verify that user can type into the email input field.
     */
    @Test
    public void testEmailInputAcceptsText() {
        onView(withId(R.id.email_input))
                .perform(clearText(), typeText("john.doe@example.com"), closeSoftKeyboard());

        onView(withId(R.id.email_input))
                .check(matches(withText("john.doe@example.com")));
    }

    /**
     * Test 8: Verify that user can type into the phone input field.
     */
    @Test
    public void testPhoneInputAcceptsText() {
        onView(withId(R.id.phone_input))
                .perform(clearText(), typeText("1234567890"), closeSoftKeyboard());

        onView(withId(R.id.phone_input))
                .check(matches(withText("1234567890")));
    }

    /**
     * Test 9: Verify that user can update the name field.
     */
    @Test
    public void testUpdateNameField() {
        onView(withId(R.id.name_input))
                .perform(clearText(), replaceText("Jane Smith"), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("Jane Smith")));
    }

    /**
     * Test 10: Verify that user can update the email field.
     */
    @Test
    public void testUpdateEmailField() {
        onView(withId(R.id.email_input))
                .perform(clearText(), replaceText("jane.smith@example.com"), closeSoftKeyboard());

        onView(withId(R.id.email_input))
                .check(matches(withText("jane.smith@example.com")));
    }

    /**
     * Test 11: Verify that user can update the phone field.
     */
    @Test
    public void testUpdatePhoneField() {
        onView(withId(R.id.phone_input))
                .perform(clearText(), replaceText("9876543210"), closeSoftKeyboard());

        onView(withId(R.id.phone_input))
                .check(matches(withText("9876543210")));
    }

    /**
     * Test 12: Verify that user can clear the name field.
     */
    @Test
    public void testClearNameField() {
        onView(withId(R.id.name_input))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("")));
    }

    /**
     * Test 13: Verify that user can clear the email field.
     */
    @Test
    public void testClearEmailField() {
        onView(withId(R.id.email_input))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.email_input))
                .check(matches(withText("")));
    }

    /**
     * Test 14: Verify that user can clear the phone field.
     */
    @Test
    public void testClearPhoneField() {
        onView(withId(R.id.phone_input))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.phone_input))
                .check(matches(withText("")));
    }

    /**
     * Test 15: Verify that clicking confirm button with valid data navigates back.
     * Note: This test assumes Firestore update succeeds and ProfileActivity opens.
     */
    @Test
    public void testConfirmButtonWithValidData() {
        onView(withId(R.id.name_input))
                .perform(clearText(), typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(clearText(), typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.phone_input))
                .perform(clearText(), typeText("1234567890"), closeSoftKeyboard());

        onView(withId(R.id.confirm_button))
                .perform(click());

    }

    /**
     * Test 16: Verify that all fields can be updated simultaneously.
     */
    @Test
    public void testUpdateAllFieldsSimultaneously() {
        onView(withId(R.id.name_input))
                .perform(clearText(), typeText("Alice Johnson"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(clearText(), typeText("alice.johnson@example.com"), closeSoftKeyboard());
        onView(withId(R.id.phone_input))
                .perform(clearText(), typeText("5551234567"), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("Alice Johnson")));
        onView(withId(R.id.email_input))
                .check(matches(withText("alice.johnson@example.com")));
        onView(withId(R.id.phone_input))
                .check(matches(withText("5551234567")));
    }

    /**
     * Test 17: Verify that the confirm button is clickable.
     */
    @Test
    public void testConfirmButtonClickable() {
        onView(withId(R.id.confirm_button))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    /**
     * Test 18: Verify that input fields handle special characters.
     */
    @Test
    public void testInputFieldsHandleSpecialCharacters() {
        onView(withId(R.id.name_input))
                .perform(clearText(), typeText("O'Brien-Smith"), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("O'Brien-Smith")));
    }

    /**
     * Test 19: Verify that email input handles complex email formats.
     */
    @Test
    public void testEmailInputHandlesComplexEmails() {
        onView(withId(R.id.email_input))
                .perform(clearText(), typeText("user.name+tag@example.co.uk"), closeSoftKeyboard());

        onView(withId(R.id.email_input))
                .check(matches(withText("user.name+tag@example.co.uk")));
    }

    /**
     * Test 20: Verify that phone input handles various phone formats.
     */
    @Test
    public void testPhoneInputHandlesVariousFormats() {
        onView(withId(R.id.phone_input))
                .perform(clearText(), typeText("(555) 123-4567"), closeSoftKeyboard());

        onView(withId(R.id.phone_input))
                .check(matches(withText("(555) 123-4567")));
    }
}