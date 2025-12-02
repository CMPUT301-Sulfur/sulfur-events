package com.example.sulfurevents;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;


/**
 * UI sanity checks for {@link OrganizerEnrolledActivity}.
 *
 *
 * These tests do NOT hit Firestore; they only verify that the core
 * UI needed to view the final enrolled list is present when the
 * activity is launched with an eventId.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEnrolledActivityTest {

    private ActivityScenario<OrganizerEnrolledActivity> launchWithDummyEvent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                OrganizerEnrolledActivity.class
        );
        intent.putExtra("eventId", "dummyEventId");
        return ActivityScenario.launch(intent);
    }

    @Test
    public void enrolledScreen_hasCoreViews() {
        try (ActivityScenario<OrganizerEnrolledActivity> scenario = launchWithDummyEvent()) {

            // Back button is visible
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));

            // Title text matches layout ("Enrolled Entrants")
            onView(withId(R.id.tvTitle))
                    .check(matches(isDisplayed()))
                    .check(matches(withText("Enrolled Entrants")));

            // RecyclerView for enrolled entrants is visible
            onView(withId(R.id.rvEnrolled)).check(matches(isDisplayed()));

            // Progress bar exists (may be visible/gone depending on timing)
            onView(withId(R.id.progressBar)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void enrolledScreen_showsEmptyStateTextView() {
        try (ActivityScenario<OrganizerEnrolledActivity> scenario = launchWithDummyEvent()) {
            // Just assert the empty TextView is part of the layout.
            // Its visibility/content is controlled by Firestore, which we don't mock here.
            onView(withId(R.id.tvEmpty)).check(matches(isDisplayed()));
        }
    }
}

