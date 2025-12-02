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
import static androidx.test.espresso.assertion.ViewAssertions.matches;


/**
 * UI sanity checks for OrganizerInvitedActivity.
 *
 * testing, Draw replacement from waiting list
 *  and Cancel entrants who didn't sign up
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerInvitedActivityTest {

    private ActivityScenario<OrganizerInvitedActivity> launchWithDummyEvent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                OrganizerInvitedActivity.class
        );
        intent.putExtra("eventId", "dummyEventId");
        return ActivityScenario.launch(intent);
    }

    @Test
    public void invitedScreen_hasCoreViewsAndButtons() {
        try (var scenario = launchWithDummyEvent()) {
            // list of invited entrants
            onView(withId(R.id.rvInvited)).check(matches(isDisplayed()));
            // cancel-selected button
            onView(withId(R.id.btnCancelSelected)).check(matches(isDisplayed()));
            // draw-replacement button
            onView(withId(R.id.btnDrawOneReplacement)).check(matches(isDisplayed()));
        }
    }
}

