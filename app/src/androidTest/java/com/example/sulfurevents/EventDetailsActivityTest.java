package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Small, stable UI test suite for EventDetailsActivity.
 * These tests only verify that important views are present
 * and show the expected text. No clicks, permissions, or
 * Firestore expectations.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    private Intent testIntent;

    @Before
    public void setup() {
        testIntent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        );
        testIntent.putExtra("eventId", "test_event_123");
        testIntent.putExtra("eventName", "Test Conference 2025");
        testIntent.putExtra("description", "A test event for unit testing");
        testIntent.putExtra("organizerEmail", "organizer@test.com");
        // no start/end dates -> no date restriction logic triggered
    }

    /**
     * Test 1: Event name TextView is visible with correct text.
     */
    @Test
    public void testEventNameVisible() {
        try (ActivityScenario<EventDetailsActivity> ignored =
                     ActivityScenario.launch(testIntent)) {

            onView(withId(R.id.event_name_detail))
                    .check(matches(isDisplayed()))
                    .check(matches(withText("Test Conference 2025")));
        }
    }

    /**
     * Test 2: Description TextView is visible with correct text.
     */
    @Test
    public void testDescriptionVisible() {
        try (ActivityScenario<EventDetailsActivity> ignored =
                     ActivityScenario.launch(testIntent)) {

            onView(withId(R.id.event_description))
                    .check(matches(isDisplayed()))
                    .check(matches(withText("A test event for unit testing")));
        }
    }

    /**
     * Test 3: Organizer TextView is visible with correct text.
     */
    @Test
    public void testOrganizerVisible() {
        try (ActivityScenario<EventDetailsActivity> ignored =
                     ActivityScenario.launch(testIntent)) {

            onView(withId(R.id.event_organizer))
                    .check(matches(isDisplayed()))
                    .check(matches(withText("Organizer: organizer@test.com")));
        }
    }

    /**
     * Test 4: Event poster ImageView exists and is displayed.
     * We don't assert on the actual image content.
     */
    @Test
    public void testEventPosterImageViewVisible() {
        try (ActivityScenario<EventDetailsActivity> ignored =
                     ActivityScenario.launch(testIntent)) {

            onView(withId(R.id.EntrantEventImage))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test 5: Total entrants TextView exists and is displayed.
     * We do not check the text because it depends on Firestore.
     */
    @Test
    public void testTotalEntrantsTextViewVisible() {
        try (ActivityScenario<EventDetailsActivity> ignored =
                     ActivityScenario.launch(testIntent)) {

            onView(withId(R.id.total_entrants))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * If the event dates are clearly in the past, the join button
     * should say "Cannot join (Registration closed)".
     */
    @Test
    public void pastEvent_showsRegistrationClosedText() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        )
                .putExtra("eventId", "pastEvent")
                .putExtra("eventName", "Past Event")
                .putExtra("description", "Already finished")
                .putExtra("organizerEmail", "org@test.com")
                // way in the past
                .putExtra("startDate", "01/01/2000")
                .putExtra("endDate", "01/02/2000");

        try (var scenario = androidx.test.core.app.ActivityScenario.launch(intent)) {
            onView(withId(R.id.join_leave_button))
                    .check(matches(withText("Cannot join (Registration closed)")));
        }
    }

    /**
     * If the start/end date extras are malformed, the button should show
     * "Invalid event dates" (the defensive branch in applyDateRestrictions()).
     */
    @Test
    public void invalidDates_showsInvalidEventDatesText() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        )
                .putExtra("eventId", "badDates")
                .putExtra("eventName", "Bad Date Event")
                .putExtra("description", "Broken dates")
                .putExtra("organizerEmail", "org@test.com")
                .putExtra("startDate", "not-a-date")
                .putExtra("endDate", "also-bad");

        try (var scenario = androidx.test.core.app.ActivityScenario.launch(intent)) {
            onView(withId(R.id.join_leave_button))
                    .check(matches(withText("Invalid event dates")));
        }
    }

}
