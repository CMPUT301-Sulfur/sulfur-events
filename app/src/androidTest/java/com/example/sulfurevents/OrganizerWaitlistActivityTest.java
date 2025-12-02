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
 * Very small UI sanity checks for OrganizerWaitlistActivity.
 *
 * These don't touch Firestore; they just assert that the
 * waitlist screen and the "Send Selected Invites" button exist.
 *
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerWaitlistActivityTest {

    private ActivityScenario<OrganizerWaitlistActivity> launchWithDummyEvent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                OrganizerWaitlistActivity.class
        );
        intent.putExtra("eventId", "dummyEventId");
        return ActivityScenario.launch(intent);
    }

    @Test
    public void waitlistScreen_hasCoreViews() {
        try (var scenario = launchWithDummyEvent()) {
            // back arrow
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            // recycler for entrants
            onView(withId(R.id.rvWaitlist)).check(matches(isDisplayed()));
            // "Send Selected Invites" button for lottery winners
            onView(withId(R.id.btnSendSelectedInvites)).check(matches(isDisplayed()));
        }
    }
}

