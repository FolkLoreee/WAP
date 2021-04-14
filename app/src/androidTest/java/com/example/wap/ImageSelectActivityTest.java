package com.example.wap;

//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//;import static androidx.test.espresso.Espresso.onData;
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static org.hamcrest.Matchers.allOf;
//import static org.hamcrest.Matchers.instanceOf;
//
//@RunWith(AndroidJUnit4.class)
//public class ImageSelectActivityTest extends TestCase {
//    @Test
//    public void test_imageSelectActivity(){
//        ActivityScenario activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
//        onView(withId(R.id.choosemapactivity)).check(matches(isDisplayed()));
//        onView(withId(R.id.tabLayout)).atPosition(1).perform(click());
//        onData(allOf(instanceOf(ImageSelectActivity.class))).atPosition(0).perform(click());
//        onView(withId(R.id.imageSelect)).check((matches(isDisplayed())));
//    }
//
//
//
//}