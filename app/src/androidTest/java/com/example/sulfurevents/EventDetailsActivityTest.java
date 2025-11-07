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
}
