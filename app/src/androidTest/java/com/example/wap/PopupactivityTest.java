package com.example.wap;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class PopupactivityTest {
    @Test
    public void test_CorrectlyDisplayedPopUp() {
        ActivityScenario activityScenario = ActivityScenario.launch(Popupactivity.class);
        onView(withId(R.id.popup)).check(matches(isCompletelyDisplayed()));
    }
}