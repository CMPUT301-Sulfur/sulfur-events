package com.example.sulfurevents;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantHistoryActivityTest {

    /**
     * Launches EntrantHistoryActivity and checks that all key views from
     * entrant_history_activity.xml exist in the hierarchy.
     */
    @Test
    public void testViewsArePresentOnLaunch() {
        try (ActivityScenario<EntrantHistoryActivity> scenario =
                     ActivityScenario.launch(EntrantHistoryActivity.class)) {

            scenario.onActivity(activity -> {
                // These IDs all come from your XML
                assertNotNull(activity.findViewById(R.id.historyRoot));
                assertNotNull(activity.findViewById(R.id.btnBack));
                assertNotNull(activity.findViewById(R.id.titleHistory));
                assertNotNull(activity.findViewById(R.id.rvHistory));
                assertNotNull(activity.findViewById(R.id.tvEmptyHistory));
                assertNotNull(activity.findViewById(R.id.progressBar));
            });
        }
    }

    /**
     * Calls onViewEvent(...) with a fake NotificationItem and just verifies
     * that it does not crash (it should start EventDetailsActivity internally).
     * No Espresso / Intents – keeps things simple and stable.
     */
    @Test
    public void testOnViewEventDoesNotCrash() {
        try (ActivityScenario<EntrantHistoryActivity> scenario =
                     ActivityScenario.launch(EntrantHistoryActivity.class)) {

            scenario.onActivity(activity -> {
                NotificationItem item = new NotificationItem();
                item.eventId = "TEST_EVENT_ID";
                item.eventName = "Test Event";

                try {
                    activity.onViewEvent(item);
                } catch (Exception e) {
                    fail("onViewEvent threw an exception: " + e);
                }
            });
        }
    }
}
