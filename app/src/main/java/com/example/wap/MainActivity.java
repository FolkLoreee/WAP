package com.example.wap;

import androidx.annotation.NonNull;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Location;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "WAP";
    private static final int MY_REQUEST_CODE = 123;

    Button testPage;
    Button mapPage;
    TextView wifiResults;
    Button tracking;

    Location currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
//        bottomNavigationView.setSelectedItemId(R.id.mainActivity);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch(item.getItemId()){
//                    case R.id.trackingActivity:
//                        startActivity(new Intent(getApplicationContext(),TrackingActivity.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                    case R.id.mappingActivity:
//                        startActivity(new Intent(getApplicationContext(),MappingActivity.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                    case R.id.mainActivity:
//
//                        return true;
//                }
//                return false;
//            }
//        });


        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Write a message to the database

        // Link the variables to the XML elements
        testPage = findViewById(R.id.testPage);
        wifiResults = findViewById(R.id.wifiResults);
        mapPage = findViewById(R.id.mapPage);

        // Set up function to transit to mapping page
        mapPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ImageUploadAcitivity.class);
                startActivity(intent);
            }
        });

        // Set up function to transit to testing page
        testPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TestingActivity.class);
                startActivity(intent);
            }
        });

    }
}

