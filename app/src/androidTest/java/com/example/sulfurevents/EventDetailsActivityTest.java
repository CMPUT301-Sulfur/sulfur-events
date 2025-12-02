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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for {@link EventDetailsActivity}.
 *
 * These tests only check the UI that comes from the Intent extras
 * (name, description, organizer, and the presence of the join button),
 * because Firestore is created inside the activity and is harder to stub
 * without changing the production code.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    // build an Intent with the same keys the activity expects
    private static Intent buildIntent() {
        return new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class)
                .putExtra("eventId", "event123")
                .putExtra("eventName", "Test Event")
                .putExtra("description", "This is only a test.")
                .putExtra("organizerEmail", "organizer@test.com");
    }

    @Rule
    public ActivityScenarioRule<EventDetailsActivity> activityRule =
            new ActivityScenarioRule<>(buildIntent());

    @Test
    public void screen_shows_event_info_from_intent() {
        // title
        onView(withId(R.id.event_name_detail))
                .check(matches(isDisplayed()))
                .check(matches(withText("Test Event")));

        // description
        onView(withId(R.id.event_description))
                .check(matches(isDisplayed()))
                .check(matches(withText("This is only a test.")));

        // organizer line
        onView(withId(R.id.event_organizer))
                .check(matches(isDisplayed()))
                .check(matches(withText("Organizer: organizer@test.com")));
    }

    @Test
    public void join_button_is_visible_initially() {
        onView(withId(R.id.join_leave_button))
                .check(matches(isDisplayed()));
    }

    @Test
    public void back_button_is_visible() {
        onView(withId(R.id.back_button_details))
                .check(matches(isDisplayed()));
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
