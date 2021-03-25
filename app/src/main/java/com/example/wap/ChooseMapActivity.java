package com.example.wap;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
