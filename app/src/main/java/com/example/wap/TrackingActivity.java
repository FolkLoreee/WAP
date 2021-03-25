package com.example.wap;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.wap.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class TrackingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);
//        bottomNavigationView.setSelectedItemId(R.id.trackingActivity);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch(item.getItemId()){
//                    case R.id.trackingActivity:
//
//                        return true;
//                    case R.id.mappingActivity:
//                        startActivity(new Intent(getApplicationContext(),MappingActivity.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                    case R.id.mainActivity:
//                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                }
//                return false;
//            }
//        });


        }
    }
