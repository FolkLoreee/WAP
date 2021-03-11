package com.example.wap;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//This activity will display the small image chunks into a grid view
public class MapViewActivity extends AppCompatActivity {

    public static ArrayList<Bitmap> imageChunks = new ArrayList<Bitmap>(200);
    GridView grid;

    @Override
    public void onCreate(Bundle bundle){

        super.onCreate(bundle);
        setContentView(R.layout.acitivty_map_view);

        //Getting the grid view and setting an adapter to it
        grid = (GridView) findViewById(R.id.gridView);
        grid.setAdapter(new ImageAdapter(this, imageChunks));
        grid.setNumColumns((int) Math.sqrt(imageChunks.size()));

//        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MapViewActivity.this, position, Toast.LENGTH_SHORT).show();
//            }
//        });

        }

}