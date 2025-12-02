package com.example.sulfurevents;

import android.content.Intent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented tests for EventFilterActivity.
 * Tests core filter selection, input handling, and UI interaction functionality.
 *
 * NOTE: These tests must run on an Android device or emulator.
 * Make sure you have a device connected or emulator running before running these tests.
 *
 * Add these dependencies to build.gradle:
 * androidTestImplementation 'androidx.test.ext:junit:1.1.5'
 * androidTestImplementation 'androidx.test:core:1.5.0'
 * androidTestImplementation 'androidx.test:runner:1.5.2'
 */
@RunWith(AndroidJUnit4.class)
public class EventFilterActivityTest {

    private ActivityScenario<EventFilterActivity> scenario;

    @Before
    public void setup() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                EventFilterActivity.class);
        scenario = ActivityScenario.launch(intent);

        // Give activity time to fully render
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Test 1: Activity launches and all views are initialized
     */
    @Test
    public void testActivityLaunchesWithAllViews() {
        scenario.onActivity(activity -> {
            assertNotNull("Activity should not be null", activity);
            assertNotNull("Keyword edit should be initialized",
                    activity.findViewById(R.id.search_keyword_edit));
            assertNotNull("Location edit should be initialized",
                    activity.findViewById(R.id.location_filter_edit));
            assertNotNull("Start date edit should be initialized",
                    activity.findViewById(R.id.start_date_edit));
            assertNotNull("End date edit should be initialized",
                    activity.findViewById(R.id.end_date_edit));
            assertNotNull("Keyword checkbox should be initialized",
                    activity.findViewById(R.id.filter_by_keyword_checkbox));
            assertNotNull("Apply button should be initialized",
                    activity.findViewById(R.id.apply_filters_button));
        });
    }

    /**
     * Test 2: Initial state - checkboxes unchecked and edit fields disabled
     */
    @Test
    public void testInitialStateCheckboxesAndFields() {
        scenario.onActivity(activity -> {
            CheckBox keywordCheckbox = activity.findViewById(R.id.filter_by_keyword_checkbox);
            CheckBox locationCheckbox = activity.findViewById(R.id.filter_by_location_checkbox);
            CheckBox dateCheckbox = activity.findViewById(R.id.filter_by_date_checkbox);

            EditText keywordEdit = activity.findViewById(R.id.search_keyword_edit);
            EditText locationEdit = activity.findViewById(R.id.location_filter_edit);
            EditText startDateEdit = activity.findViewById(R.id.start_date_edit);

            // Verify checkboxes are unchecked
            assertFalse("Keyword checkbox should be unchecked", keywordCheckbox.isChecked());
            assertFalse("Location checkbox should be unchecked", locationCheckbox.isChecked());
            assertFalse("Date checkbox should be unchecked", dateCheckbox.isChecked());

            // Verify edit fields are disabled
            assertFalse("Keyword edit should be disabled", keywordEdit.isEnabled());
            assertFalse("Location edit should be disabled", locationEdit.isEnabled());
            assertFalse("Start date edit should be disabled", startDateEdit.isEnabled());
        });
    }

    /**
     * Test 3: Checking keyword checkbox enables edit field
     */
    @Test
    public void testKeywordCheckboxEnablesEditField() {
        scenario.onActivity(activity -> {
            CheckBox keywordCheckbox = activity.findViewById(R.id.filter_by_keyword_checkbox);
            EditText keywordEdit = activity.findViewById(R.id.search_keyword_edit);

            // Initially disabled
            assertFalse("Keyword edit should start disabled", keywordEdit.isEnabled());

            // Check the checkbox
            keywordCheckbox.setChecked(true);

            // Verify edit field is enabled
            assertTrue("Keyword checkbox should be checked", keywordCheckbox.isChecked());
            assertTrue("Keyword edit should be enabled", keywordEdit.isEnabled());
        });
    }

    /**
     * Test 4: Unchecking checkbox clears and disables edit field
     */
    @Test
    public void testUncheckingCheckboxClearsField() {
        scenario.onActivity(activity -> {
            CheckBox keywordCheckbox = activity.findViewById(R.id.filter_by_keyword_checkbox);
            EditText keywordEdit = activity.findViewById(R.id.search_keyword_edit);

            // Check checkbox and enter text
            keywordCheckbox.setChecked(true);
            keywordEdit.setText("test");

            // Uncheck checkbox
            keywordCheckbox.setChecked(false);

            // Verify field is disabled and cleared
            assertFalse("Keyword checkbox should be unchecked", keywordCheckbox.isChecked());
            assertFalse("Keyword edit should be disabled", keywordEdit.isEnabled());
            assertEquals("Keyword edit should be empty", "", keywordEdit.getText().toString());
        });
    }

    /**
     * Test 5: Date checkbox enables both start and end date fields
     */
    @Test
    public void testDateCheckboxEnablesBothDateFields() {
        scenario.onActivity(activity -> {
            CheckBox dateCheckbox = activity.findViewById(R.id.filter_by_date_checkbox);
            EditText startDateEdit = activity.findViewById(R.id.start_date_edit);
            EditText endDateEdit = activity.findViewById(R.id.end_date_edit);

            // Check the date checkbox
            dateCheckbox.setChecked(true);

            // Verify both date fields are enabled
            assertTrue("Date checkbox should be checked", dateCheckbox.isChecked());
            assertTrue("Start date edit should be enabled", startDateEdit.isEnabled());
            assertTrue("End date edit should be enabled", endDateEdit.isEnabled());
        });
    }

    /**
     * Test 6: Multiple filters can be enabled simultaneously
     */
    @Test
    public void testMultipleFiltersSimultaneously() {
        scenario.onActivity(activity -> {
            CheckBox keywordCheckbox = activity.findViewById(R.id.filter_by_keyword_checkbox);
            CheckBox locationCheckbox = activity.findViewById(R.id.filter_by_location_checkbox);
            CheckBox dateCheckbox = activity.findViewById(R.id.filter_by_date_checkbox);

            EditText keywordEdit = activity.findViewById(R.id.search_keyword_edit);
            EditText locationEdit = activity.findViewById(R.id.location_filter_edit);
            EditText startDateEdit = activity.findViewById(R.id.start_date_edit);

            // Enable all three filters
            keywordCheckbox.setChecked(true);
            locationCheckbox.setChecked(true);
            dateCheckbox.setChecked(true);

            // Set values
            keywordEdit.setText("concert");
            locationEdit.setText("Toronto");

            // Verify all are checked and enabled
            assertTrue("Keyword checkbox should be checked", keywordCheckbox.isChecked());
            assertTrue("Location checkbox should be checked", locationCheckbox.isChecked());
            assertTrue("Date checkbox should be checked", dateCheckbox.isChecked());

            assertTrue("Keyword edit should be enabled", keywordEdit.isEnabled());
            assertTrue("Location edit should be enabled", locationEdit.isEnabled());
            assertTrue("Start date edit should be enabled", startDateEdit.isEnabled());

            assertEquals("Keyword should be 'concert'", "concert", keywordEdit.getText().toString());
            assertEquals("Location should be 'Toronto'", "Toronto", locationEdit.getText().toString());
        });
    }

    /**
     * Test 7: Apply filters button finishes activity
     */
    @Test
    public void testApplyFiltersFinishesActivity() {
        scenario.onActivity(activity -> {
            CheckBox keywordCheckbox = activity.findViewById(R.id.filter_by_keyword_checkbox);
            EditText keywordEdit = activity.findViewById(R.id.search_keyword_edit);
            Button applyButton = activity.findViewById(R.id.apply_filters_button);

            // Enable keyword filter and enter text
            keywordCheckbox.setChecked(true);
            keywordEdit.setText("festival");

            // Click apply button
            applyButton.performClick();

            // Verify activity is finishing
            assertTrue("Activity should be finishing after apply",
                    activity.isFinishing());
        });
    }

    /**
     * Test 8: Clear filters button finishes activity
     */
    @Test
    public void testClearFiltersFinishesActivity() {
        scenario.onActivity(activity -> {
            CheckBox keywordCheckbox = activity.findViewById(R.id.filter_by_keyword_checkbox);
            EditText keywordEdit = activity.findViewById(R.id.search_keyword_edit);
            Button clearButton = activity.findViewById(R.id.clear_filters_button);

            // Set some filters first
            keywordCheckbox.setChecked(true);
            keywordEdit.setText("test");

            // Click clear button
            clearButton.performClick();

            // Verify activity is finishing
            assertTrue("Activity should be finishing after clear",
                    activity.isFinishing());
        });
    }

    /**
     * Test 9: Back button finishes activity
     */
    @Test
    public void testBackButtonFinishesActivity() {
        scenario.onActivity(activity -> {
            ImageButton backButton = activity.findViewById(R.id.btnBackFilters);

            // Click back button
            backButton.performClick();

            // Verify activity is finishing
            assertTrue("Activity should be finishing after back button",
                    activity.isFinishing());
        });
    }

    /**
     * Test 10: Text input with special characters
     */
    @Test
    public void testTextInputWithSpecialCharacters() {
        scenario.onActivity(activity -> {
            CheckBox keywordCheckbox = activity.findViewById(R.id.filter_by_keyword_checkbox);
            CheckBox locationCheckbox = activity.findViewById(R.id.filter_by_location_checkbox);
            EditText keywordEdit = activity.findViewById(R.id.search_keyword_edit);
            EditText locationEdit = activity.findViewById(R.id.location_filter_edit);

            // Enable filters
            keywordCheckbox.setChecked(true);
            locationCheckbox.setChecked(true);

            // Enter text with special characters
            String specialKeyword = "Rock & Roll Festival!";
            String specialLocation = "St. John's, NL";

            keywordEdit.setText(specialKeyword);
            locationEdit.setText(specialLocation);

            // Verify text is preserved
            assertEquals("Keyword should match", specialKeyword, keywordEdit.getText().toString());
            assertEquals("Location should match", specialLocation, locationEdit.getText().toString());
        });
    }
}
