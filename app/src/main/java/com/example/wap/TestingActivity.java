package com.example.wap;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestingActivity extends AppCompatActivity {

    ImageButton locateBtn;
    TextView calculatedPointData;
    ImageView mapImageView;

    // Bitmap
    Bitmap mapImage;
    Canvas canvas;
    Paint paint;
    Path mPath;

    // Wifi
    private static final int MY_REQUEST_CODE = 123;
    private final static String LOG_TAG = "Testing Activity";
    WifiManager wifiManager;
    TestingActivity.WifiBroadcastReceiver wifiReceiver;

    // Wifi Scan
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;

    // wifi scan data from target location
    ArrayList<Double> targetDataOriginal;
    ArrayList<Double> targetData;
    ArrayList<Double> targetStdDev;
    ArrayList<String> targetMacAdd;

    private final String locationID = "CCLvl1";

    Algorithm algorithm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.testingActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.testingActivity:
                        return true;
                    case R.id.mappingActivity:
                        startActivity(new Intent(getApplicationContext(),MappingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.mainActivity:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        // Initialise XML elements
        locateBtn = findViewById(R.id.locateBtn);
        calculatedPointData = findViewById(R.id.calculatedPointData);
        mapImageView = findViewById(R.id.mapImageView);

        // delete signal records
//        WAPFirebase<Signal> deleteSignals = new WAPFirebase<>(Signal.class,"signals");
//        deleteSignals.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
//            @Override
//            public void onSuccess(ArrayList<Signal> signals) {
//                for (Signal s: signals) {
//                    deleteSignals.delete(s.getSignalID());
//                }
//            }
//        });

        // TODO: Map should not be hardcoded; NEED TO CHANGE
        WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class,"locations");
        locationWAPFirebase.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
            @Override
            public void onSuccess(ArrayList<Location> locations) {
                for (Location l: locations) {
                    if (l.getLocationID().equals(locationID)) {
                        String mapImageAdd = l.getMapImage();
                        System.out.println("mapImage: " + mapImageAdd);
                        if (android.os.Build.VERSION.SDK_INT > 9) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            try {
                                URL url = new URL(mapImageAdd);
                                mapImage = Utils.getBitmap(url);
                                Bitmap bitmap = Bitmap.createBitmap((int) mapImage.getWidth(), (int) mapImage.getHeight(), Bitmap.Config.ARGB_8888);
                                canvas = new Canvas(bitmap);
                                mPath = new Path();
                                canvas.drawBitmap(mapImage, 0, 0, null);
                                paint = new Paint();
                                paint.setColor(Color.RED);
                                mapImageView.setImageBitmap(bitmap);
                                locateBtn.setVisibility(View.VISIBLE);
                                calculatedPointData.setVisibility(View.VISIBLE);
                            } catch (IOException e) {
                                Log.d("Map cannot be displayed", String.valueOf(e));
                            }
                        }
                    }
                }
            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String device1 = "SG-Bldg2ThinkTank-495-31-";
//                String device2 = "SG-null-448-72-";
//
//                WifiScan.compareDeviceWifiValues(device1, device2);

                // Initialise hashmaps
                targetMacAdd = new ArrayList<>();
                targetData = new ArrayList<>();
                targetStdDev = new ArrayList<>();
                targetDataOriginal = new ArrayList<>();

                // initialise algorithm object
                algorithm = new Algorithm();

                // retrieve data from firebase
                algorithm.retrievefromFirebase(locationID);

                // collect wifi signals at target location
                numOfScans = 0;

                // re-initialise hash map each time the button is pressed
                allSignals = new HashMap<>();
                ssids = new HashMap<>();
                WifiScan.askAndStartScanWifi(LOG_TAG, MY_REQUEST_CODE, TestingActivity.this);
                wifiManager.startScan();
            }
        });
    }

    @Override
    protected void onStop()  {
        this.unregisterReceiver(this.wifiReceiver);
        super.onStop();
    }

    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Wifi Scan: onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Toast.makeText(TestingActivity.this, "Processing Wifi Scan " + (numOfScans+1), Toast.LENGTH_SHORT).show();

                List<ScanResult> list = wifiManager.getScanResults();

                for (ScanResult result : list) {
                    if (numOfScans == 0) {
                        ArrayList<Integer> signals = new ArrayList<>();
                        signals.add(result.level);
                        allSignals.put(result.BSSID, signals);
                        ssids.put(result.BSSID, result.SSID);
                    }
                    else {
                        if (allSignals.containsKey(result.BSSID)) {
                            allSignals.get(result.BSSID).add(result.level);
                        }
                    }
                }

                // all scans completed
                if (numOfScans == 11) {
                    Toast.makeText(TestingActivity.this, "Wifi Scan Complete" + (numOfScans+1), Toast.LENGTH_SHORT).show();
                    for (String macAddress: allSignals.keySet()) {

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

                    Coordinate position = calculatePosition();
                    calculatedPointData.setText(stringifyPosition(position));

                    // clear previous dot if there is
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    // redraw the bitmap
                    canvas.drawBitmap(mapImage, 0, 0, null);

                    // draw the dot on the bitmap
                    canvas.drawCircle(doubleToFloat(position.getX()), doubleToFloat(position.getY()), 10, paint);
                }
            } else {
                Toast.makeText(TestingActivity.this, "Wifi scan failed", Toast.LENGTH_SHORT).show();
            }

            // continue scanning if it has not reached 12 scans + increase numOfScans
            numOfScans++;
            if (numOfScans < 12) {
                wifiManager.startScan();
            }
        }
    }

    private float doubleToFloat(double d) {
        Double D = Double.valueOf(d);
        float f = D.floatValue();
        return f;
    }

    private String stringifyPosition(Coordinate finalPoint) {
        StringBuilder sb = new StringBuilder();
        sb.append("x = ");
        sb.append(finalPoint.getX());
        sb.append(", y = ");
        sb.append(finalPoint.getY());

        return sb.toString();
    }

    private Coordinate calculatePosition() {
        // pre-matching fingerprints
        algorithm.preMatching(targetData, targetMacAdd);

        Coordinate calculatedPoint1 = algorithm.euclideanDistance(targetData, targetStdDev, targetMacAdd);
        Coordinate calculatedPoint2 = algorithm.jointProbability(targetDataOriginal, targetMacAdd);
        Coordinate finalPoint = algorithm.weightedFusion(calculatedPoint1, calculatedPoint2);

        return finalPoint;
    }
}