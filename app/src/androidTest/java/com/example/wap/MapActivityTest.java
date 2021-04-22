package com.example.wap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.internal.inject.InstrumentationContext;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasBackground;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


public class MapActivityTest {

    /*@Before
    public void setup(){
        signalWAPFirebase = new WAPFirebase<>(Signal.class,"signals");
    }
*/
    static Intent intent;
    static Bitmap myLogo;
    static {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        intent = new Intent(context, MapActivity.class);
        myLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftarrowbutton);
        assertNotNull(myLogo);
        intent.putExtra("BitmapImage", myLogo);
    }

    @Rule
    public ActivityScenarioRule<MapActivity> activityScenarioRule = new ActivityScenarioRule<>(intent);

    @Test
    public void test_signal_indexing(){
        int counter = 0;
        String pointID = "MP-LOCATION-";
        MapPoint point = new MapPoint();
        ArrayList<String> signalIDs = new ArrayList<>();
        String signalID = "SG-"+pointID+"-"+counter;
        Signal signal = new Signal();
        signal.setSignalID(signalID);
        signalIDs.add(signalID);
    }

    @Test
    public void drawFunctionTestPass(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            ImageView imageViewTest = new ImageView(activity.getApplicationContext());;
            imageViewTest.setImageBitmap(myLogo);
            Coordinate coordinateTest = new Coordinate(0,0);
            ArrayList<Path> pathsTest = new ArrayList<>();
            TextView textViewTest = new TextView(activity.getApplicationContext());
            MapActivity.drawFunction(coordinateTest, 5,5, 100, 100, imageViewTest, pathsTest, textViewTest);
        });
    }

    @Test
    public void drawFunctionTestMapImageNullFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            ImageView imageViewTest = null;
            Coordinate coordinateTest = new Coordinate(0,0);
            ArrayList<Path> pathsTest = new ArrayList<>();
            TextView textViewTest = new TextView(activity.getApplicationContext());
            MapActivity.drawFunction(coordinateTest, 5,5, 100, 100, imageViewTest, pathsTest, textViewTest);
        });
    }

    /*@Test
    public void centerOfRectTest(){

        Coordinate coordinateTest = new Coordinate(0,0);

        float centerTest[] = MapActivity.centerOfRect(coordinateTest, 5,5);
        assertEquals(2.5, centerTest[0], 1e-15);
        assertEquals(2.5, centerTest[1], 1e-15);
    }*/

    @Test
    public void drawFunctionTestMapCoordinateNullFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            ImageView imageViewTest = new ImageView(activity.getApplicationContext());
            imageViewTest.setImageBitmap(myLogo);
            Coordinate coordinateTest = null;
            ArrayList<Path> pathsTest = new ArrayList<>();
            TextView textViewTest = new TextView(activity.getApplicationContext());
            MapActivity.drawFunction(coordinateTest, 5,5, 100, 100, imageViewTest, pathsTest, textViewTest);
        });

    }

    @Test
    public void upButtonOutOfBoundTestFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            MapActivity.coordinate = new Coordinate(0,0);
            activity.findViewById(R.id.up).performClick();
        });
    }

    @Test
    public void leftButtonOutOfBoundTestFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            MapActivity.coordinate = new Coordinate(0,0);
            activity.findViewById(R.id.left).performClick();
        });
    }

    @Test
    public void downButtonOutOfBoundTestFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            int bitmapImgHeight = MapActivity.bitmapImg.getHeight();
            MapActivity.coordinate = new Coordinate(0,bitmapImgHeight);
            activity.findViewById(R.id.down).performClick();
        });
    }



    @Test
    public void rightButtonOutOfBoundTestFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            int bitmapImgWidth = MapActivity.bitmapImg.getWidth();
            MapActivity.coordinate = new Coordinate(bitmapImgWidth,0);
            activity.findViewById(R.id.right).performClick();

        });
    }
    @Test
    public void drawFunctionTestsquareHeightZeroFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            ImageView imageViewTest = new ImageView(activity.getApplicationContext());
            imageViewTest.setImageBitmap(myLogo);
            Coordinate coordinateTest = new Coordinate(0,0);
            ArrayList<Path> pathsTest = new ArrayList<>();
            TextView textViewTest = new TextView(activity.getApplicationContext());
            MapActivity.drawFunction(coordinateTest, 0,5, 100, 100, imageViewTest, pathsTest, textViewTest);
        });

    }

    @Test
    public void drawFunctionTestsquareWidthZeroFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            ImageView imageViewTest = new ImageView(activity.getApplicationContext());
            imageViewTest.setImageBitmap(myLogo);
            Coordinate coordinateTest = new Coordinate(0,0);
            ArrayList<Path> pathsTest = new ArrayList<>();
            TextView textViewTest = new TextView(activity.getApplicationContext());
            MapActivity.drawFunction(coordinateTest, 5,0, 100, 100, imageViewTest, pathsTest, textViewTest);
        });
    }

    @Test
    public void drawFunctionTestintrinsicHeightZeroFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            ImageView imageViewTest = new ImageView(activity.getApplicationContext());
            imageViewTest.setImageBitmap(myLogo);
            Coordinate coordinateTest = new Coordinate(0,0);
            ArrayList<Path> pathsTest = new ArrayList<>();
            TextView textViewTest = new TextView(activity.getApplicationContext());
            MapActivity.drawFunction(coordinateTest, 5,5, 0, 100, imageViewTest, pathsTest, textViewTest);
        });
    }

    @Test
    public void drawFunctionTestintrinsicWidthZeroFail(){
        ActivityScenario activityScenario = activityScenarioRule.getScenario();
        activityScenario.onActivity(activity -> {
            ImageView imageViewTest = new ImageView(activity.getApplicationContext());
            imageViewTest.setImageBitmap(myLogo);
            Coordinate coordinateTest = new Coordinate(0,0);
            ArrayList<Path> pathsTest = new ArrayList<>();
            TextView textViewTest = new TextView(activity.getApplicationContext());
            MapActivity.drawFunction(coordinateTest, 5,5, 100, 0, imageViewTest, pathsTest, textViewTest);
        });
    }


    /*ui Tests
    @Test
    public void test_IsItemsDisplayed(){
        ActivityScenario activityscenario = ActivityScenario.launch(MapActivity.class);
        onView(withId(R.id.bottom)).check(matches(isDisplayed()));
        onView(withId(R.id.right)).check(matches(isDisplayed()));
        onView(withId(R.id.left)).check(matches(isDisplayed()));
        onView(withId(R.id.down)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.scan)).check(matches(isDisplayed()));
        onView(withId(R.id.coordinatesText)).check(matches(isDisplayed()));
        onView(withId(R.id.mapImageView)).check(matches(isDisplayed()));
    }*/
}
