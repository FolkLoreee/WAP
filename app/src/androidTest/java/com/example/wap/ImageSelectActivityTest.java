package com.example.wap;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.tabs.TabLayout;

import junit.framework.TestCase;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class ImageSelectActivityTest extends TestCase {
    private ImageSelectActivity imageSelectActivity;
    private ImageSelectAdapter imageSelectAdapter;

    @Before
    public void runBeforeEachTest() {
        System.out.println("setting up ImageUploadAcitivityTest");
        imageSelectActivity = new ImageSelectActivity();
    }

    @Test
    public void imageSelectFailNoImage(){
        try{
            imageSelectActivity.select.callOnClick();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //test that fragments are transitioning properly
    @Test
    public void tabPass_imageSelectActivity(){
        ActivityScenario activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
        onView(withId(R.id.choosemapactivity)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayout)).perform(selectTabAtPosition(1));
        onView(withId(R.id.imageSelect)).check(matches(isDisplayed()));
    }

    @Test
    public void tabPass_imageUploadActivity(){
        ActivityScenario activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
        onView(withId(R.id.choosemapactivity)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayout)).perform(selectTabAtPosition(0));
        onView(withId(R.id.imageUpload)).check(matches(isDisplayed()));
    }

    //testing that item clicked return correct image
    @Test
    public void imageUploadActivityImageSelectedPass(){

        //TODO: click list view and verify that the image matches the firebase

        ActivityScenario activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
        onView(withId(R.id.choosemapactivity)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayout)).perform(selectTabAtPosition(1));
        onView(withId(R.id.imageSelect)).check(matches(isDisplayed()));
//        onView(withId(R.id.imageSelect)).perform();
    }

    @NonNull
    public static ViewAction selectTabAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
            }

            @Override
            public String getDescription() {
                return "with tab at index" + String.valueOf(position);
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof TabLayout) {
                    TabLayout tabLayout = (TabLayout) view;
                    TabLayout.Tab tab = tabLayout.getTabAt(position);

                    if (tab != null) {
                        tab.select();
                    }
                }
            }
        };
    }

    @NonNull
    private static ViewAction selectListAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(ListView.class));
            }

            @Override
            public String getDescription() {
                return "with tab at index" + String.valueOf(position);
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof ListView) {
                    ListView tabLayout = (ListView) view;
                    tabLayout.performItemClick(view, position, android.R.id.list);
                }
            }
            
        };
    }

//    @Test
//    public void test_NavButtons(){
//        ActivityScenario activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
//        onView(withId(R.id.tabLayout)).perform(selectTabAtPosition(1));
//        onView(withId(android.R.id.list)).perform(selectListAtPosition(2));
////        onView(withContentDescription("CCLvl2")).perform(click());
//        onView(withId(R.id.right)).perform(click());
//        onView(withContentDescription("(96.0,21.5)")).check(matches(isDisplayed()));
//
//    }


}