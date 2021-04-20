package com.example.wap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class ChooseMapActivityTest {
    // ensure correct page is viewed
    @Test
    public void test_IsChooseMapInView(){
    ActivityScenario activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
    onView(withId(R.id.choosemapactivity)).check(matches(isDisplayed()));
    }
    //testing items display correctly
    @Test
    public void test_IsItemsDisplayed(){
        ActivityScenario activityscenario = ActivityScenario.launch(ChooseMapActivity.class);
        onView(withId(R.id.fragment)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.bottom_nav_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayout)).check(matches(isDisplayed()));
       ;
    }
    //test Navigation on bottomnavbar
    @Test
    public void test_navTestingactivity(){
        ActivityScenario activityscenario = ActivityScenario.launch(ChooseMapActivity.class);
        onView(withId(R.id.choosemapactivity)).check(matches(isDisplayed()));
        onView(withContentDescription(R.string.Testing)).perform(click());
        onView(withId(R.id.testingActivity)).check(matches(isDisplayed()));
    }

}