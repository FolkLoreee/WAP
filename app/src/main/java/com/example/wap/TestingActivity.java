package com.example.wap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestingActivity extends AppCompatActivity {

    Button retrieveDB;
    Button locateBtn;
    TextView pointsData;
    TextView signalsData;

    // Wifi
    private static final int MY_REQUEST_CODE = 123;
    private final static String LOG_TAG = "Testing Activity";
    WifiManager wifiManager;
    TestingActivity.WifiBroadcastReceiver wifiReceiver;
    MapPoint point;

    // Wifi Scan
    ArrayList<String> macAddresses;
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;

    private final String locationID = "DebugLocation1";
    Location currentLocation = new Location("DebugLocation1", "Debug Location");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        // Initialise XML elements
        retrieveDB = (Button) findViewById(R.id.retrieveDB);
        locateBtn = (Button) findViewById(R.id.locateBtn);
        pointsData = (TextView) findViewById(R.id.pointsData);
        signalsData = (TextView) findViewById(R.id.signalsData);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numOfScans = 0;
                // re-initialise hash map each time the button is pressed
                allSignals = new HashMap<>();
                ssids = new HashMap<>();
                WifiScan.askAndStartScanWifi(LOG_TAG, MY_REQUEST_CODE, TestingActivity.this);
                wifiManager.startScan();
            }
        });

        // TODO: Retrieve data from Firebase
        WAPFirebase<MapPoint> wapFirebasePoints = new WAPFirebase<>(MapPoint.class,"points");
        WAPFirebase<Signal> wapFirebaseSignal = new WAPFirebase<>(Signal.class,"signals");

        // display data from DB
        retrieveDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringBuilder str = new StringBuilder();

                wapFirebasePoints.compoundQuery("locationID", "CampusCentre1").addOnSuccessListener(new OnSuccessListener<ArrayList<MapPoint>>() {
                    @Override
                    public void onSuccess(ArrayList<MapPoint> mapPoints) {
                        for (MapPoint point: mapPoints) {
                            Log.d("FIREBASE: ", point.toString());
                            str.append(point.toString());
                            str.append("\n");
                        }
                        // set text to display data
                        pointsData.setText(str);
                        Log.d("DISPLAY ", "TEXT");
                    }
                });
            }
        });

        // TODO: Pre-matching of fingerprints?
        preMatching();
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
            Log.d(LOG_TAG, "onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Log.d(LOG_TAG, "Result of Scan " + numOfScans);

                List<ScanResult> list = wifiManager.getScanResults();

                for (ScanResult result : list) {
                    if (numOfScans == 0) {
                        // TODO: should have a list of approved signals
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
                    Log.d(LOG_TAG, "MAC Address: " + result.BSSID + " , SSID: " + result.SSID + " , Wifi Signal: " + result.level);
                }

                Log.d(LOG_TAG, allSignals.toString());

                // all scans completed, send data to firebase
                if (numOfScans == 3) {
                    for (String macAddress : allSignals.keySet()) {

                        // get the average wifi signal if the BSSID exists
                        ArrayList<Integer> readings = allSignals.get(macAddress);
                        int averageSignal = WifiScan.calculateAverage(readings);
                        int stdDevSignal = WifiScan.calculateStandardDeviation(readings, averageSignal);
                        int averageSignalProcessed = WifiScan.calculateProcessedAverage(averageSignal);

                        Log.d(LOG_TAG, "MAC Address: " + macAddress + " , Wifi Signal: " + averageSignal + " , Wifi Signal (SD): " + stdDevSignal);
                        Toast.makeText(TestingActivity.this, "Scan Complete!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.d(LOG_TAG, "Scan has issues");
            }

            // continue scanning if it has not reached 10 scans + increase numOfScans
            numOfScans++;
            if (numOfScans < 4) {
                Log.d(LOG_TAG, String.valueOf(numOfScans));
                Log.d(LOG_TAG, "Started another scan");
                wifiManager.startScan();
            }
        }
    }

    private ArrayList<String> preMatching() {
        ArrayList<String> fingerprints = new ArrayList<>();

        return fingerprints;
    }

    private Coordinate euclideanDistance() {
        // TODO: Euclidean distance positioning algorithm (Hannah)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    private Coordinate jointProbability() {
        // TODO: joint probability positioning algorithm (Hannah)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    private Coordinate cosineSimilarity() {
        // TODO: cosine similarity positioning algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    private Coordinate weightedFusion() {
        // TODO: add different weights to each algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

}