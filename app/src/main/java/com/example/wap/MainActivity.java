package com.example.wap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.models.Location;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "WAP";
    private static final int MY_REQUEST_CODE = 123;

    ImageButton testPage;
    ImageButton mapPage;
    TextView wifiResults;
    BottomNavigationView bottomNavigationView;
    Button tracking;

    Location currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.mainActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.testingActivity:
                        startActivity(new Intent(getApplicationContext(),TestingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.mappingActivity:
//                        startActivity(new Intent(getApplicationContext(),MappingActivity.class));
//                        overridePendingTransition(0,0);
                        return true;
                    case R.id.mainActivity:

                        return true;
                }
                return false;
            }
        });


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
                    Intent intent = new Intent(MainActivity.this, ChooseMapActivity.class);
//                Intent intent = new Intent(MainActivity.this, ImageUploadAcitivity.class);
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

