package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI test for AdminEntrantNotificationsActivity.
 * Verifies that core UI components load and display the
 * entrant-specific header and empty state.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminEntrantNotificationsActivityTest {

    private static final String TEST_ENTRANT_ID = "testUser123";
    private static final String TEST_ENTRANT_NAME = "Test User";

    @Rule
    public ActivityScenarioRule<AdminEntrantNotificationsActivity> scenario =
            new ActivityScenarioRule<>(createIntent());

    /**
     * Creates an intent containing fake entrant data.
     */
    private static Intent createIntent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                AdminEntrantNotificationsActivity.class
        );
        intent.putExtra("entrantId", TEST_ENTRANT_ID);
        intent.putExtra("entrantName", TEST_ENTRANT_NAME);
        return intent;
    }

    /**
     * Test 1: Verify that header text correctly displays the entrant's name.
     */
    @Test
    public void testHeaderDisplaysCorrectName() throws InterruptedException {
        Thread.sleep(500); // allow UI + snapshot listener to initialize
        onView(withId(R.id.tvTitle))
                .check(matches(withText("Notifications for " + TEST_ENTRANT_NAME)));
    }

    /**
     * Test 2: Verify the RecyclerView is visible.
     */
    @Test
    public void testRecyclerViewVisible() throws InterruptedException {
        Thread.sleep(500);
        onView(withId(R.id.rvNotificationLogs))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 3: Verify the empty message is visible when no logs exist.
     * Firestore will return empty by default in test environment.
     */
    @Test
    public void testEmptyMessageVisible() throws InterruptedException {
        Thread.sleep(500);
        onView(withId(R.id.tvEmptyLogs))
                .check(matches(isDisplayed()));
    }
}
