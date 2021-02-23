package com.example.wap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "WAP";
    private static final int MY_REQUEST_CODE = 123;

    Button wifiScan;
    Button mapPage;
    TextView wifiResults;

    WifiManager wifiManager;
    WifiBroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Write a message to the database

        // Link the variables to the XML elements
        wifiScan = findViewById(R.id.wifiScan);
        wifiResults = findViewById(R.id.wifiResults);
        mapPage = findViewById(R.id.mapPage);

        // Set up function to transit to mapping page
        mapPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MappingActivity.class);
                startActivity(intent);
            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askAndStartScanWifi();
            }
        });

    }

    private void askAndStartScanWifi()  {

        // With Android Level >= 23, you have to ask the user
        // for permission to Call.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // 23
            int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            // Check for permissions
            if (permission1 != PackageManager.PERMISSION_GRANTED) {

                Log.d(LOG_TAG, "Requesting Permissions");

                // Request permissions
                ActivityCompat.requestPermissions(this,
                        new String[] {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.ACCESS_NETWORK_STATE
                        }, MY_REQUEST_CODE);
                return;
            }
            Log.d(LOG_TAG, "Permissions Already Granted");
        }
        doStartScanWifi();
    }

    private void doStartScanWifi()  {
        wifiManager.startScan();
    }

    @Override
    protected void onStop()  {
        this.unregisterReceiver(this.wifiReceiver);
        super.onStop();
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver  {
        @Override
        public void onReceive(Context context, Intent intent)   {
            Log.d(LOG_TAG, "onReceive()");

            Toast.makeText(MainActivity.this, "Scan Complete!", Toast.LENGTH_SHORT).show();

            boolean ok = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (ok)  {
                Log.d(LOG_TAG, "Scan OK");

                List<ScanResult> list = wifiManager.getScanResults();
//                HashMap<String, Double> networkDistance = new HashMap<>();

                StringBuilder sb = new StringBuilder();

                for (ScanResult result: list) {
                    double distance = calculateDistance(result.level, result.frequency);
//                    networkDistance.put(result.SSID, distance);
                    System.out.println(result.SSID + " : " + distance + " m");
                    sb.append(result.SSID + ": " + distance + " m" + "\n");
                }

                wifiResults.setText(sb.toString());

            }  else {
                Log.d(LOG_TAG, "Scan not OK");
            }
        }
    }
}

