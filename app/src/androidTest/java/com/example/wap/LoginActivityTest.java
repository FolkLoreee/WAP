package com.example.wap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

;

@RunWith(AndroidJUnit4.class)


public class LoginActivityTest extends TestCase {
//! Instrumented Tests

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void test_instantiating_firebase_auth(){
//        ActivityScenario activityScenario = ActivityScenario.launch(LoginActivity.class);

    }
//? UI Tests
    @Test
    public void test_IsLoginInView(){
        onView(withId(R.id.loginActivity)).check(matches(isDisplayed()));
    }

    @Test
    public void test_IsItemsDisplayed(){
        onView(withId(R.id.applogo)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.passwordfield)).check(matches(isDisplayed()));
        onView(withId(R.id.loginfield)).check(matches(isDisplayed()));
        onView(withId(R.id.textView3)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
    }

    @Test
    public void test_navMainActivity() throws InterruptedException {
        ActivityScenario activityscenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.loginfield)).perform(click(), typeText("wes0903@gmail.com"),closeSoftKeyboard());
        onView(withId(R.id.passwordfield)).perform(click(), typeText("123456"),closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(1500);
        onView(withId(R.id.choosemapactivity)).check(matches(isDisplayed()));
    }
//
   @Test
    public void test_navMainActivityFailId() {
        onView(withId(R.id.loginfield)).perform(click(), typeText("Hi prof"), closeSoftKeyboard());
        onView(withId(R.id.passwordfield)).perform(click(), typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        onView(withId(R.id.choosemapactivity)).check(ViewAssertions.doesNotExist());
    }

    @Test
    public void test_navMainActivityFailPw() {
        ActivityScenario activityscenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.loginfield)).perform(click(), typeText("wes0903@gmail.com"),closeSoftKeyboard());
        onView(withId(R.id.passwordfield)).perform(click(), typeText("ESC rules"),closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        onView(withId(R.id.choosemapactivity)).check(ViewAssertions.doesNotExist());
    }

}