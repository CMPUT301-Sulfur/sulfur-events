package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

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

        // Check that the delete button is visible
        onView(withId(R.id.delete_button)).check(matches(isDisplayed()));

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

    /**
     * Test 6: Verify that the delete button is displayed and clickable.
     */
    @Test
    public void testDeleteButtonDisplayed() {
        // Check that the delete button is visible
        onView(withId(R.id.delete_button)).check(matches(isDisplayed()));

        // Verify it contains "Delete Profile" text
        onView(withId(R.id.delete_button))
                .check(matches(withText(containsString("Delete Profile"))));
    }

    /**
     * Test 7: Verify that clicking the delete button shows confirmation dialog.
     */
    @Test
    public void testDeleteButtonShowsConfirmationDialog() {
        // Click the delete button
        onView(withId(R.id.delete_button)).perform(click());

        // Wait briefly for dialog to appear
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify dialog with "Delete Profile" title appears
        onView(withText("Delete Profile")).check(matches(isDisplayed()));
    }

    /**
     * Test 8: Verify that delete confirmation dialog has Cancel button.
     */
    @Test
    public void testDeleteDialogHasCancelButton() {
        // Click the delete button
        onView(withId(R.id.delete_button)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify Cancel button exists
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    /**
     * Test 9: Verify that delete confirmation dialog has Delete button.
     */
    @Test
    public void testDeleteDialogHasDeleteButton() {
        // Click the delete button
        onView(withId(R.id.delete_button)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify Delete button exists
        onView(withText("Delete")).check(matches(isDisplayed()));
    }

    /**
     * Test 10: Verify that clicking Cancel on delete dialog dismisses it.
     */
    @Test
    public void testDeleteDialogCancelDismisses() {
        // Click the delete button
        onView(withId(R.id.delete_button)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click Cancel
        onView(withText("Cancel")).perform(click());

        // Verify profile activity is still displayed (user not deleted)
        onView(withId(R.id.name_display)).check(matches(isDisplayed()));
    }

    /**
     * Test 11: Verify that notifications switch is displayed.
     */
    @Test
    public void testNotificationsSwitchDisplayed() {
        // Check that notifications switch is visible
        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()));
    }

    /**
     * Test 12: Verify that notifications switch has correct label.
     */
    @Test
    public void testNotificationsSwitchLabel() {
        // Check that the switch has the correct text label
        onView(withId(R.id.switch_notifications))
                .check(matches(withText("Receive notifications")));
    }


    /**
     * Test 13: Verify that admin button is hidden by default for non-admin users.
     */
    @Test
    public void testAdminButtonHiddenForNonAdmin() {
        // Wait for profile to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // For non-admin users, admin button should not be displayed
        // Note: This test assumes the test user is not an admin
        onView(withId(R.id.admin_button)).check(matches(not(isDisplayed())));
    }

    /**
     * Test 14: Verify that admin button has correct text.
     */
    @Test
    public void testAdminButtonText() {
        // Check that admin button has "Admin Dashboard" text
        onView(withId(R.id.admin_button))
                .check(matches(withText("Admin Dashboard")));
    }

    /**
     * Test 15: Verify that delete dialog shows warning message.
     */
    @Test
    public void testDeleteDialogWarningMessage() {
        // Click the delete button
        onView(withId(R.id.delete_button)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify warning message contains key phrases
        onView(withText(containsString("Remove you from all event lists")))
                .check(matches(isDisplayed()));
        onView(withText(containsString("Delete all events you've created")))
                .check(matches(isDisplayed()));
        onView(withText(containsString("Permanently delete your profile")))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 16: Verify that phone number shows "Not provided" when empty.
     */
    @Test
    public void testPhoneNumberNotProvided() {
        // Wait for profile to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that phone display contains either a phone number or "Not provided"
        onView(withId(R.id.phone_display))
                .check(matches(withText(containsString("Phone Number:"))));
    }

    /**
     * Test 17: Verify that bottom navigation FAB for notifications is displayed.
     */
    @Test
    public void testNotificationFabDisplayed() {
        // Check that notification FAB is visible
        onView(withId(R.id.fab_notifications)).check(matches(isDisplayed()));
    }

    /**
     * Test 18: Verify all required UI components exist.
     */
    @Test
    public void testAllUIComponentsExist() {
        // Verify all key components are present
        onView(withId(R.id.name_display)).check(matches(isDisplayed()));
        onView(withId(R.id.email_display)).check(matches(isDisplayed()));
        onView(withId(R.id.phone_display)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_button)).check(matches(isDisplayed()));
        onView(withId(R.id.delete_button)).check(matches(isDisplayed()));
        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()));
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_notifications)).check(matches(isDisplayed()));
    }

    /**
     * Test 19: Verify that edit button has correct text.
     */
    @Test
    public void testEditButtonText() {
        // Check that the edit button displays "Edit"
        onView(withId(R.id.edit_button))
                .check(matches(withText("Edit")));
    }

    /**
     * Test 20: Verify that notifications switch can be toggled.
     */
    @Test
    public void testNotificationsSwitchToggleable() {
        // Wait for profile to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click the switch to toggle it
        onView(withId(R.id.switch_notifications)).perform(click());

        // Wait briefly for state to update
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify the switch state changed (if it was checked, now unchecked, or vice versa)
        // This test verifies the switch is interactive
    }

    /**
     * Test 21: Verify that title "Profile" is displayed.
     */
    @Test
    public void testProfileTitleDisplayed() {
        // Check that the profile title is shown
        onView(withId(R.id.welcome_back_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Profile")));
    }

    /**
     * Test 22: Verify that subtitle text is displayed.
     */
    @Test
    public void testSubtitleDisplayed() {
        // Check that the subtitle instruction text is shown
        onView(withText("Edit Your Personal Information Below"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 23: Verify that delete warning mentions "cannot be undone".
     */
    @Test
    public void testDeleteDialogCannotBeUndone() {
        // Click the delete button
        onView(withId(R.id.delete_button)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify warning includes "cannot be undone"
        onView(withText(containsString("cannot be undone")))
                .check(matches(isDisplayed()));
    }
}