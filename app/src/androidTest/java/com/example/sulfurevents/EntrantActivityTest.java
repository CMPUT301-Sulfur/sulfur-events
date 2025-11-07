package com.example.sulfurevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantActivityTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> rule =
            new ActivityScenarioRule<>(EntrantActivity.class);

    @Test
    public void screen_starts_and_shows_guidelines_button() {
        onView(withId(R.id.btn_lottery_guidelines))
                .check(matches(isDisplayed()));
    }
}
