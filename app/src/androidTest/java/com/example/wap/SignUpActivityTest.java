package com.example.wap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.assertion.ViewAssertions;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class SignUpActivityTest {
    @Test
    public void test_correctview() {
        ActivityScenario activityScenario = ActivityScenario.launch(SignUpActivity.class);
        onView(withId(R.id.etEmailSignup)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.etUsernameSignup)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.etPasswordSignup)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.authSpinner)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.UserType)).check(matches(isCompletelyDisplayed()));
    }

   /* @Test
    public void duplicate_Email() {
        ActivityScenario activityScenario = ActivityScenario.launch(SignUpActivity.class);
        onView(withId(R.id.etEmailSignup)).perform(click(), typeText("wes0903@gmail.com"));
        onView(withId(R.id.etUsernameSignup)).perform(click(),typeText("wes"));
        onView(withId(R.id.etPasswordSignup)).perform(click(),typeText("123456"));
//        onView(withId(R.id.signupButton)).perform(click());
        onView(withId(R.id.loginActivity)).check(ViewAssertions.doesNotExist());
    }*/
}