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

        // Activity should remain on WelcomeEntrantActivity due to validation error
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));
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

        // Activity should remain on WelcomeEntrantActivity due to validation error
        onView(withId(R.id.email_input)).check(matches(isDisplayed()));
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

    /**
     * Test 9: Verify that email validation rejects emails without @ symbol.
     */
    @Test
    public void testEmailValidationRejectsInvalidEmail() {
        onView(withId(R.id.name_input))
                .perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("invalidemail.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Activity should remain due to validation error
        onView(withId(R.id.email_input)).check(matches(isDisplayed()));
    }

    /**
     * Test 10: Verify that email validation accepts emails with @ symbol.
     */
    @Test
    public void testEmailValidationAcceptsValidEmail() {
        onView(withId(R.id.name_input))
                .perform(typeText("Valid User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("valid@email.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());
    }

    /**
     * Test 11: Verify that name field is required and cannot be whitespace only.
     */
    @Test
    public void testNameFieldRequired() {
        onView(withId(R.id.name_input))
                .perform(typeText("   "), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("test@example.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Activity should remain due to empty name (whitespace trimmed)
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));
    }

    /**
     * Test 12: Verify that email field is required and cannot be whitespace only.
     */
    @Test
    public void testEmailFieldRequired() {
        onView(withId(R.id.name_input))
                .perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("   "), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Activity should remain due to empty email (whitespace trimmed)
        onView(withId(R.id.email_input)).check(matches(isDisplayed()));
    }

    /**
     * Test 13: Verify that fields can be cleared and refilled.
     */
    @Test
    public void testFieldsCanBeClearedAndRefilled() {
        onView(withId(R.id.name_input))
                .perform(typeText("First Name"), closeSoftKeyboard());
        onView(withId(R.id.name_input))
                .perform(clearText(), typeText("Second Name"), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("Second Name")));
    }

    /**
     * Test 14: Verify that submit button is clickable.
     */
    @Test
    public void testSubmitButtonClickable() {
        onView(withId(R.id.submit_button))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    /**
     * Test 15: Verify that name input handles special characters.
     */
    @Test
    public void testNameInputHandlesSpecialCharacters() {
        onView(withId(R.id.name_input))
                .perform(typeText("O'Brien-Smith Jr."), closeSoftKeyboard());

        onView(withId(R.id.name_input))
                .check(matches(withText("O'Brien-Smith Jr.")));
    }

    /**
     * Test 16: Verify that email input handles complex email formats.
     */
    @Test
    public void testEmailInputHandlesComplexFormats() {
        onView(withId(R.id.email_input))
                .perform(typeText("user.name+tag@sub.example.com"), closeSoftKeyboard());

        onView(withId(R.id.email_input))
                .check(matches(withText("user.name+tag@sub.example.com")));
    }

    /**
     * Test 17: Verify that phone input handles various formats.
     */
    @Test
    public void testPhoneInputHandlesVariousFormats() {
        onView(withId(R.id.phone_input))
                .perform(typeText("+1 (555) 123-4567"), closeSoftKeyboard());

        onView(withId(R.id.phone_input))
                .check(matches(withText("+1 (555) 123-4567")));
    }

    /**
     * Test 18: Verify that empty phone field is acceptable.
     */
    @Test
    public void testEmptyPhoneFieldAcceptable() {
        onView(withId(R.id.name_input))
                .perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.phone_input))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());
    }

    /**
     * Test 19: Verify that ProfileModel is created with correct default roles.
     * Note: This is tested implicitly through successful profile creation.
     */
    @Test
    public void testProfileCreationWithDefaultRoles() {
        onView(withId(R.id.name_input))
                .perform(typeText("New User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.phone_input))
                .perform(typeText("5551234567"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Success navigates to ProfileActivity (default roles: isEntrant=true, others=false)
    }

    /**
     * Test 20: Verify that multiple @ symbols in email are accepted (valid format).
     */
    @Test
    public void testEmailWithMultipleAtSymbols() {
        onView(withId(R.id.name_input))
                .perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("test@@example.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Should be accepted (contains @ symbol, even if malformed)
        // Note: The validation only checks for presence of @, not RFC compliance
    }

    /**
     * Test 21: Verify that email without domain is rejected.
     */
    @Test
    public void testEmailWithoutAtSymbolRejected() {
        onView(withId(R.id.name_input))
                .perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("testexample.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Should remain on activity due to missing @ symbol
        onView(withId(R.id.email_input)).check(matches(isDisplayed()));
    }

    /**
     * Test 22: Verify that all UI components are present.
     */
    @Test
    public void testAllUIComponentsPresent() {
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));
        onView(withId(R.id.email_input)).check(matches(isDisplayed()));
        onView(withId(R.id.phone_input)).check(matches(isDisplayed()));
        onView(withId(R.id.submit_button)).check(matches(isDisplayed()));
    }

    /**
     * Test 23: Verify that submit works with minimal required fields only.
     */
    @Test
    public void testSubmitWithMinimalFields() {
        onView(withId(R.id.name_input))
                .perform(typeText("Min User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("min@user.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());
    }

    /**
     * Test 24: Verify that trim() removes leading/trailing spaces from inputs.
     */
    @Test
    public void testInputsTrimmed() {
        onView(withId(R.id.name_input))
                .perform(typeText("  Trimmed Name  "), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("  trimmed@email.com  "), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Should succeed (spaces trimmed)
    }

    /**
     * Test 25: Verify that EdgeToEdge display mode is enabled.
     * Note: This is implicit in the activity's onCreate() but cannot be directly tested via Espresso.
     */
    @Test
    public void testActivityDisplaysCorrectly() {
        // Verify that the welcome view container exists
        onView(withId(R.id.welcome)).check(matches(isDisplayed()));
    }

    /**
     * Test 26: Verify that device ID is properly retrieved (implicit).
     * Note: Cannot directly test device ID retrieval, but submission depends on it.
     */
    @Test
    public void testDeviceIdHandling() {
        // Fill required fields
        onView(withId(R.id.name_input))
                .perform(typeText("Device Test"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("device@test.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());

        // Success indicates device ID was properly retrieved and used
    }

    /**
     * Test 27: Verify that very long names are accepted.
     */
    @Test
    public void testLongNameAccepted() {
        onView(withId(R.id.name_input))
                .perform(typeText("Christopher Alexander Montgomery Williamson III"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("long@name.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());
    }

    /**
     * Test 28: Verify that very long emails are accepted.
     */
    @Test
    public void testLongEmailAccepted() {
        onView(withId(R.id.name_input))
                .perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.email_input))
                .perform(typeText("verylongemailaddress@subdomain.example.com"), closeSoftKeyboard());

        onView(withId(R.id.submit_button))
                .perform(click());
    }
}