package com.example.wap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestingActivityTest {
    WifiManager wifiManager;
    WifiBroadcastReceiver wifiReceiver;
    // Wifi Scan
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;
    // wifi scan data from target location
    ArrayList<Double> targetDataOriginal;
    ArrayList<Double> targetData;
    ArrayList<Double> targetStdDev;
    ArrayList<String> targetMacAdd;
    final String locationID = "CCLvl2";

    Context appContext;

    @Test
    @Before
    public void useAppContext() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.wap", appContext.getPackageName());
    }

    //Instantiating Firebase Test
    @Test
    public void testInstantiateWAPFirebaseLocationNotNull() {
        WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
        assertNotNull(locationWAPFirebase);
    }

    // TODO: Update this testing as code has been changed to display map based on chosen location
    //Testing compound query
    @Test
    public void testCompoundQueryNotNull() {
        HashMap<String, ArrayList<String>> availableLocations = new HashMap<>();
        WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
        locationWAPFirebase.compoundQuery("locationID", "CCL2Z1").addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
            @Override
            public void onSuccess(ArrayList<Location> locations) {
                ArrayList<String> locationsNames = new ArrayList<>();
                locationsNames.add("No Selection");

                for (Location l: locations) {
                    ArrayList<String> info = new ArrayList<>();
                    info.add(l.getLocationID());
                    info.add(l.getMapImage());
                    info.add(Integer.toString(l.getMapPointCounts()));
                    // if location name is null, it will save the location ID instead
                    if (l.getName() != null) {
                        availableLocations.put(l.getName(), info);
                        locationsNames.add(l.getName());
                    }
                    else {
                        availableLocations.put(l.getLocationID(), info);
                        locationsNames.add(l.getLocationID());
                    }
                }
                assertNotNull(availableLocations);
            }
        });
    }

    @Test
    public void testDrawingMap() {
        String mapImage = "https://firebasestorage.googleapis.com/v0/b/wapsutd-e0016.appspot.com/o/maps%2FCCL2Z1?alt=media&token=76267543-f6e0-4a35-aec5-66a996253a5c";

        WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
        locationWAPFirebase.compoundQuery("locationID", "CCL2Z1").addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
            @Override
            public void onSuccess(ArrayList<Location> locations) {
                assertNotNull(locations);
                for (Location l : locations) {
                    if (l.getLocationID().equals(locationID)) {
                        String mapImageAdd = l.getMapImage();
                        assertNotNull(mapImageAdd);
                        assertEquals(mapImageAdd, mapImage);
                        if (Build.VERSION.SDK_INT > 9) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            try {
                                URL url = new URL(mapImageAdd);
                                Bitmap mapImage = Utils.getBitmap(url);
                                Bitmap bitmap = Bitmap.createBitmap((int) mapImage.getWidth(), (int) mapImage.getHeight(), Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                assertNotNull(canvas);
                                Path mPath = new Path();
                                assertNotNull(mPath);
                                canvas.drawBitmap(mapImage, 0, 0, null);
                                Paint paint = new Paint();
                                assertNotNull(paint);
                                paint.setColor(Color.RED);
                                assertTrue(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    //Testing setting up WifiManager
    @Test
    public void testWifiManagerSetup() {
        wifiManager = (WifiManager) appContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();
        assertNotNull(wifiReceiver);
    }

    //Testing scanning with wifi manager
    @Test
    public void testWifiManagerScan() {
        wifiManager = (WifiManager) appContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();
        wifiManager.startScan();
        assertTrue(true);
    }

    //Deregister the wifimanager
//    @After
//    public void deregisterWifiManager(){
//        if(wifiReceiver!=null){
//            appContext.unregisterReceiver(wifiReceiver);
//        }
//    }
    //Testing WAPFirebase Signal and Point Instantiation
    @Test
    public void testInstantiateSignalAndPointFirebase() {
        WAPFirebase<Signal> signalWAPFirebase = new WAPFirebase<>(Signal.class, "signals");
        assertNotNull(signalWAPFirebase);
        WAPFirebase<MapPoint> pointWAPFirebase = new WAPFirebase<>(MapPoint.class, "points");
        assertNotNull(pointWAPFirebase);
    }

    //Testing compoundquery for MapPoints
    //TODO: uncomment and change the location ID once we add the loca

    @Test
    public void testCompoundQueryMapPoints() {
        WAPFirebase<MapPoint> wapFirebasePoints = new WAPFirebase<>(MapPoint.class, "points");
        wapFirebasePoints.compoundQuery("locationID", "CCL2Z1").addOnSuccessListener(new OnSuccessListener<ArrayList<MapPoint>>() {
            @Override
            public void onSuccess(ArrayList<MapPoint> mapPoints) {
                assertFalse(mapPoints.isEmpty());
            }
        });
    }

    //TODO: Fix the URL

    @Test
    public void testUtilsGetBitmapNotNull() throws IOException {
        String URL = "https://firebasestorage.googleapis.com/v0/b/wapsutd-e0016.appspot.com/o/maps%2FCCLvl1?alt=media&token=d882eab4-c435-4cb7-8b82-e13fc298629c";
        Bitmap bitmap = Utils.getBitmap(new URL(URL));
        assertNotNull(bitmap);
    }


    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {

                List<ScanResult> list = wifiManager.getScanResults();

                for (ScanResult result : list) {
                    if (numOfScans == 0) {
                        ArrayList<Integer> signals = new ArrayList<>();
                        signals.add(result.level);
                        allSignals.put(result.BSSID, signals);
                        ssids.put(result.BSSID, result.SSID);
                    } else {
                        if (allSignals.containsKey(result.BSSID)) {
                            allSignals.get(result.BSSID).add(result.level);
                        }
                    }
                }

                // all scans completed
                if (numOfScans == 1) {
                    for (String macAddress : allSignals.keySet()) {

                        // get the average wifi signal if the BSSID exists
                        ArrayList<Integer> readings = allSignals.get(macAddress);
                        double averageSignal = WifiScan.calculateAverage(readings);
                        double stdDevSignal = WifiScan.calculateStandardDeviation(readings, averageSignal);
                        double averageSignalProcessed = WifiScan.calculateProcessedAverage(averageSignal);

                        // store these values into the data variables for wifi scan
                        targetDataOriginal.add(averageSignal);
                        targetData.add(averageSignalProcessed);
                        targetMacAdd.add(macAddress);
                        targetStdDev.add(stdDevSignal);
                    }
                }
            }
            // continue scanning if it has not reached 4 scans + increase numOfScans
            numOfScans++;
            if (numOfScans < 2) {
                wifiManager.startScan();
            }
        }
    }

    //UI testing
    @Test
    public void test_IsTestingInView(){
        ActivityScenario activityScenario = ActivityScenario.launch(TestingActivity.class);
        onView(withId(R.id.testingActivity)).check(matches(isDisplayed()));
    }
    //testing elements display correctly
    @Test
    public void test_IsItemsDisplayed() throws InterruptedException {
        ActivityScenario activityscenario = ActivityScenario.launch(TestingActivity.class);
        onView(withId(R.id.testingActivity)).check(matches(isDisplayed()));
        onView(withId(R.id.mapImageView)).check(matches(isDisplayed()));
        onView(withId(R.id.selectLocationText)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.locationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Campus Centre Lv 2"))).perform(click());
    }

    //test Navigation on bottomnavbar
    @Test
    public void test_navTestingactivity(){
        ActivityScenario activityscenario = ActivityScenario.launch(TestingActivity.class);
        onView(withId(R.id.testingActivity)).check(matches(isDisplayed()));
        onView(withContentDescription(R.string.mapping)).perform(click());
        onView(withId(R.id.mappingActivity)).check(matches(isDisplayed()));
    }
}
