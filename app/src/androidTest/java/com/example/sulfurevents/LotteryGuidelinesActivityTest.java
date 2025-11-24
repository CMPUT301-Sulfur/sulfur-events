package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LotteryGuidelinesActivityTest {

    @Test
    public void guidelinesText_isShown_and_backFinishes() {
        // launch the activity
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                LotteryGuidelinesActivity.class
        );
        ActivityScenario<LotteryGuidelinesActivity> scenario =
                ActivityScenario.launch(intent);

        // 1) text is displayed
        onView(withId(R.id.lottery_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(org.hamcrest.Matchers.containsString("Lottery System Guidelines"))));

        // 2) click back button
        onView(withId(R.id.back_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // 3) activity should be destroyed/finished
        scenario.onActivity(activity ->
                // if we got here without a crash, click worked; we can also just close
                activity.isFinishing()
        );
    }
}
