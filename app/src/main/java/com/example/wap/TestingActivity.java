package com.example.wap;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestingActivity extends AppCompatActivity {

    Button locateBtn;
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

    private final String locationID = "Bldg2ThinkTank";

    // data from firebase
    HashMap<String, ArrayList<String>> pointsFB;
    HashMap<String, Coordinate> pointsCoordinatesFB;
    HashMap<String, Double> signalStrengthFB;
    HashMap<String, Double> signalStrengthOriginalFB;
    HashMap<String, String> signalBSSIDFB;
    HashMap<String, Double> signalStrengthSDFB;

    // pre-matching data format
    ArrayList<Integer> fingerprintData;
    ArrayList<Coordinate> fingerprintCoordinate;

    // NEWLY ADDED FOR DATA CREATED AFTER PRE-MATCHING
    HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal;
    HashMap<String, HashMap<String, Double>> fingerprintAvgSignal;
    HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal;

    // create an arraylist to store the euclidean distance di values
    ArrayList<Double> euclideanArray = new ArrayList<>();
    // create an arraylist to store the jointprob i values
    ArrayList<Double> jointProbArray = new ArrayList<>();

    // Weights for each algorithm in the weighted fusion algorithm
    private final double weightEuclidDist = 0.5;
    private final double weightJointProb = 0.5;

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
                switch (item.getItemId()) {
                    case R.id.testingActivity:
                        return true;
                    case R.id.mappingActivity:
                        startActivity(new Intent(getApplicationContext(), MappingActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.mainActivity:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        // Initialise XML elements
        locateBtn = findViewById(R.id.locateBtn);
        calculatedPointData = findViewById(R.id.calculatedPointData);
        mapImageView = findViewById(R.id.mapImageView);

        // delete old records
//        WAPFirebase<Signal> signalsWAPFirebase = new WAPFirebase<Signal>(Signal.class,"signals");
//        for (int i = 6411; i < 9999; i++) {
//            String uuid = "SG-CampusCentre1-" + String.valueOf(i);
//            signalsWAPFirebase.delete(uuid);
//        }

        // TODO: Map should not be hardcoded; NEED TO CHANGE
        WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
        locationWAPFirebase.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
            @Override
            public void onSuccess(ArrayList<Location> locations) {
                for (Location l : locations) {
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
                                Log.d("Help", String.valueOf(e));
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
                // Initialise hashmaps
                // Re-initialise these maps upon each click
                pointsFB = new HashMap<>();
                pointsCoordinatesFB = new HashMap<>();
                signalStrengthFB = new HashMap<>();
                signalBSSIDFB = new HashMap<>();
                signalStrengthSDFB = new HashMap<>();
                signalStrengthOriginalFB = new HashMap<>();

                fingerprintData = new ArrayList<>();
                fingerprintCoordinate = new ArrayList<>();

                fingerprintOriginalAvgSignal = new HashMap<>();
                fingerprintAvgSignal = new HashMap<>();
                fingerprintStdDevSignal = new HashMap<>();
                targetMacAdd = new ArrayList<>();
                targetData = new ArrayList<>();
                targetStdDev = new ArrayList<>();
                targetDataOriginal = new ArrayList<>();

                // retrieve data from firebase
                retrievefromFirebase();

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

    private void retrievefromFirebase() {
        WAPFirebase<MapPoint> wapFirebasePoints = new WAPFirebase<>(MapPoint.class, "points");
        WAPFirebase<Signal> wapFirebaseSignal = new WAPFirebase<>(Signal.class, "signals");

        wapFirebasePoints.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<MapPoint>>() {
            @Override
            public void onSuccess(ArrayList<MapPoint> mapPoints) {
                for (MapPoint point : mapPoints) {
                    String pointID = point.getPointID();
                    ArrayList<String> signalsIDs = point.getSignalIDs();
                    pointsFB.put(pointID, signalsIDs);
                    pointsCoordinatesFB.put(pointID, point.getCoordinate());
                }
            }
        });

        wapFirebaseSignal.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
            @Override
            public void onSuccess(ArrayList<Signal> signals) {
                for (Signal signal : signals) {
                    String signalID = signal.getSignalID();
                    String bssid = signal.getWifiBSSID();
                    double signalStrengthSD = signal.getSignalStrengthSD();
                    double signalStrength = signal.getSignalStrength();
                    signalStrengthFB.put(signalID, signalStrength);
                    signalStrengthOriginalFB.put(signalID, signalStrength);
                    signalStrengthSDFB.put(signalID, signalStrengthSD);
                    signalBSSIDFB.put(signalID, bssid);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        this.unregisterReceiver(this.wifiReceiver);
        super.onStop();
    }

    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Toast.makeText(TestingActivity.this, "Processing Wifi Scan " + numOfScans, Toast.LENGTH_SHORT).show();

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
                    calculatedPointData.setText("Wifi Scan Complete");
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

                    calculatedPointData.setText("Calculating Position...");
                    Coordinate position = calculatePosition();
                    if (Double.isNaN(position.getX()) || Double.isNaN(position.getY())) {
                        calculatedPointData.setText("You are out of the area. Please move to this location shown in the map.");
                    } else {
                        calculatedPointData.setText(stringifyPosition(position));
                        // clear previous dot if there is
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        // redraw the bitmap
                        canvas.drawBitmap(mapImage, 0, 0, null);

                        // draw the dot on the bitmap
                        canvas.drawCircle(doubleToFloat(position.getX()), doubleToFloat(position.getY()), 10, paint);
                    }
                } else {
                    Toast.makeText(TestingActivity.this, "Wifi scan failed. Please try again in 2 minutes.", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Wifi scan failed");
                }

                // continue scanning if it has not reached 4 scans + increase numOfScans
                numOfScans++;
                if (numOfScans < 2) {
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
            preMatching();
        /*
        // pre-matching fingerprints
        DataPrematching dataPrematch = new DataPrematching();
        dataPrematch.preMatching(targetData, targetMacAdd, pointsFB, pointsCoordinatesFB,  signalStrengthFB,  signalStrengthOriginalFB, signalBSSIDFB, signalStrengthSDFB);

         */


            //create the Algorithm object
            Algorithm algorithm = new Algorithm(fingerprintOriginalAvgSignal, fingerprintAvgSignal, fingerprintStdDevSignal, fingerprintCoordinate);


            // weighted fusion
            Coordinate calculatedPoint1 = algorithm.euclideanDistance(targetData, targetStdDev, targetMacAdd);
            Coordinate calculatedPoint2 = algorithm.jointProbability(targetDataOriginal, targetMacAdd);
            Coordinate finalPoint = algorithm.weightedFusion(calculatedPoint1, calculatedPoint2);

         /*

        preMatching();
        Coordinate calculatedPoint1 = euclideanDistance();
        Coordinate calculatedPoint2 = jointProbability();
        Coordinate finalPoint = weightedFusion(calculatedPoint1, calculatedPoint2);

          */


            StringBuilder sb = new StringBuilder();
            sb.append("Euclidean Distance results: x = ");
            sb.append(calculatedPoint1.getX());
            sb.append(", y = ");
            sb.append(calculatedPoint1.getY());
            sb.append("\n");

            sb.append("Joint Probability results: x = ");
            sb.append(calculatedPoint2.getX());
            sb.append(", y = ");
            sb.append(calculatedPoint2.getY());
            sb.append("\n");

            System.out.println("Euclidean Distance results: " + stringifyPosition(calculatedPoint1));
            System.out.println("Joint Probability results: " + stringifyPosition(calculatedPoint2));


            //Coordinate finalPoint = new Coordinate(0, 0);
            return finalPoint;
        }


        private void preMatching() {

            double threshold = 0.4;

            // get FLAG value
            double total = 0;
            for (double strength : targetData) {
                total += strength;
            }

            final double FLAG = total / targetData.size();

            // get a list of mac address where the signal strength pass the FLAG value
            ArrayList<String> filteredMac = new ArrayList<>();
            for (int i = 0; i < targetData.size(); i++) {
                double strength = targetData.get(i);
                if (Math.abs(strength) > Math.abs(FLAG)) {
                    filteredMac.add(targetMacAdd.get(i));
                }
            }


            // compare bssid in each fingerprint with the list of bssid from wifi scan at target location
            for (String pointID : pointsFB.keySet()) {
                // System.out.println("Size of each fingerprint: " + pointsFB.get(pointID).size());
                int count = 0;
                boolean check = true;
                ArrayList<String> allSignals = pointsFB.get(pointID);
                ArrayList<String> listOfBSSID = new ArrayList<>();

                // for each signal, retrieve the corresponding bssid
                for (String signalID : allSignals) {
                    listOfBSSID.add(signalBSSIDFB.get(signalID));
                }

                // compare the bssid between fingerprint and target location
                // if the bssid of the target location is not in the fingerprint, eliminate the fingerprint
                for (String bssid : targetMacAdd) {
                    if (listOfBSSID.contains(bssid)) {
                        count++;
                    }
                }

                // calculating the percentage match
                double percentMatch = (double) count / targetMacAdd.size();
                System.out.println(percentMatch);
                if (percentMatch < threshold) {
                    check = false;
                }

                // if all the bssids are in the fingerprint, add fingerprint
                if (check) {
                    // get coordinates of this fingerprint
                    Coordinate coordinates = pointsCoordinatesFB.get(pointID);
                    fingerprintCoordinate.add(coordinates);

                    // convert coordinates to string to store as key values for the hashmaps
                    StringBuilder str = new StringBuilder();
                    str.append(coordinates.getX());
                    str.append(", ");
                    str.append(coordinates.getY());
                    String coordinatesStr = str.toString();

                    // create a hashmap for each mac address and corresponding signal strength at this fingerprint
                    HashMap<String, Double> avgSignalFingerprint = new HashMap<>();
                    HashMap<String, Double> stdDevSignalFingerprint = new HashMap<>();
                    HashMap<String, Double> originalAvgSignalFingerprint = new HashMap<>();
                    for (String signalID : allSignals) {
                        originalAvgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthOriginalFB.get(signalID));
                        avgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthFB.get(signalID));
                        stdDevSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthSDFB.get(signalID));
                    }
                    fingerprintOriginalAvgSignal.put(coordinatesStr, originalAvgSignalFingerprint);
                    fingerprintAvgSignal.put(coordinatesStr, avgSignalFingerprint);
                    fingerprintStdDevSignal.put(coordinatesStr, stdDevSignalFingerprint);
                }
            }
        }
    }
}