package com.example.wap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class ChooseMapActivity extends AppCompatActivity {
    TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_map);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new ImageUploadAcitivity());
        ft.commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.choosemapactivity);
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
                        startActivity(new Intent(getApplicationContext(),ChooseMapActivity.class));
                        overridePendingTransition(0,0);
                        return true;
//                    case R.id.mainActivity:
//                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
//                        overridePendingTransition(0,0);
//                        return true;
                }
                return false;
            }
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
               int position = tab.getPosition();
               if (position == 0){
                   FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                   ft.replace(R.id.fragment, new ImageUploadAcitivity());
                   ft.commit();
               }
               else if (position == 1){
                   FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                   ft.replace(R.id.fragment, new ImageSelectActivity());
                   ft.commit();
               }
//                ft.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
