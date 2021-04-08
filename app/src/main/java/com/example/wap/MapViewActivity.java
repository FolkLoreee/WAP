package com.example.wap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;

//This activity will display the small image chunks into a grid view
public class MapViewActivity extends AppCompatActivity {

    public static ArrayList<Bitmap> imageChunks = new ArrayList<Bitmap>(300);
    public static ArrayList<Bitmap> imageChunksCopy;
    public static ArrayList<Coordinate> imageCoords = new ArrayList<Coordinate>(300);
    GridView grid;
    ImageAdapter imageAdapter;
//    private final static String LOG_TAG = "Mapview Activity";
    Button up;
    Button down;
    Button left;
    Button right;
    Button submit;

    int position = 0;
    int y = 0;
    Drawable drawable;

    // Wifi Stuff
    private static final int MY_REQUEST_CODE = 123;
    private final static String LOG_TAG = "Mapping Activity";
    WifiManager wifiManager;
    WifiBroadcastReceiver wifiReceiver;
    MapPoint point;

    // Wifi Data and Scans
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;

    //Firebase
    WAPFirebase<Signal> signalWAPFirebase;
    WAPFirebase<MapPoint> pointWAPFirebase;
    WAPFirebase<Location> locationWAPFirebase;

    //TODO: locationID will follow the locationID from the previous screen
    public static String locationID;
    public static String locationName;


    Location currentLocation;


    @Override
    public void onCreate(Bundle bundle){

        super.onCreate(bundle);
        setContentView(R.layout.acitivty_map_view);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        right = findViewById(R.id.right);
        left = findViewById(R.id.left);

        submit = findViewById(R.id.submit);

        //Getting the grid view and setting an adapter to it
        imageAdapter = new ImageAdapter(this, imageChunks, imageCoords);
        grid = (GridView) findViewById(R.id.gridView);
        grid.setAdapter(imageAdapter);
        grid.setNumColumns((int) Math.sqrt(imageChunks.size()));

        Intent intent = getIntent();
        locationID =intent.getStringExtra("locationID");
        Log.d(LOG_TAG,"LOCATION IS: "+locationID);

        currentLocation = new Location(locationID, locationName);

        signalWAPFirebase = new WAPFirebase<>(Signal.class, "signals");
        pointWAPFirebase = new WAPFirebase<>(MapPoint.class, "points");
        locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));


        View v1 = imageAdapter.getView(0,null, grid);
        v1.callOnClick();
        Bitmap bitmap = BitmapFactory. decodeResource(getResources(), R. drawable.black);
        imageChunks.set(0, bitmap);
//                    imageAdapter = new ImageAdapter(getApplicationContext(), imageChunks, imageCoords);
//                    grid = (GridView) findViewById(R.id.gridView);
//                    grid.setAdapter(imageAdapter);
//                    grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
        imageAdapter.notifyDataSetChanged();

        v1.callOnClick();


        up.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                if (position == 0){
                    Toast.makeText(getApplicationContext(), String.valueOf(0), Toast.LENGTH_SHORT).show();
                }

                else if(position >= 17){
                    Bitmap bitmap = imageChunksCopy.get(position);
                    imageChunks.set(position, bitmap);

                    position -= 17;

                    View v1 = imageAdapter.getView(position,null, grid);
                    v1.callOnClick();
                    Bitmap bitmap2 = BitmapFactory. decodeResource(getResources(), R. drawable.black);
                    imageChunks.set(position, bitmap2);
//                    imageAdapter = new ImageAdapter(getApplicationContext(), imageChunks, imageCoords);
//                    grid = (GridView) findViewById(R.id.gridView);
//                    grid.setAdapter(imageAdapter);
//                    grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
                    imageAdapter.notifyDataSetChanged();
                    v1.callOnClick();

//                    bitmap = imageChunksCopy.get(position);
//                    imageChunks.set(position, bitmap);

                }

                else{
                    Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (position + 17 <= 17*17){
                    Bitmap bitmap = imageChunksCopy.get(position);
                    imageChunks.set(position, bitmap);
//                    View v1 = imageAdapter.getView(0,null, grid);
//                    Object v2 = imageAdapter.getItem(0);
//                    Log.d("Help me again", String.valueOf(v2));
//
//                    drawable = getResources().getDrawable(R.drawable.black);
//                        v1.setForeground(drawable);
//                        Log.d("Help again", String.valueOf(v1));
                    position += 17;
                    View v1 = imageAdapter.getView(position,null, grid);
                    v1.callOnClick();
                    Bitmap bitmap2 = BitmapFactory. decodeResource(getResources(), R. drawable.black);
                    imageChunks.set(position, bitmap2);
//                    imageAdapter = new ImageAdapter(getApplicationContext(), imageChunks, imageCoords);
//                    grid = (GridView) findViewById(R.id.gridView);
//                    grid.setAdapter(imageAdapter);
//                    grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
                    imageAdapter.notifyDataSetChanged();
                    v1.callOnClick();

//                    bitmap = imageChunksCopy.get(position);
//                    imageChunks.set(position, bitmap);
                }

                else{
                    Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (position != 0){
                    Bitmap bitmap = imageChunksCopy.get(position);
                    imageChunks.set(position, bitmap);
//                    View v1 = imageAdapter.getView(0,null, grid);
//                    Object v2 = imageAdapter.getItem(0);
//                    Log.d("Help me again", String.valueOf(v2));
//
//                    drawable = getResources().getDrawable(R.drawable.black);
//                        v1.setForeground(drawable);
//                        Log.d("Help again", String.valueOf(v1));
                    position -= 1;
                    View v1 = imageAdapter.getView(position,null, grid);
                    v1.callOnClick();
                    Bitmap bitmap2 = BitmapFactory. decodeResource(getResources(), R. drawable.black);
                    imageChunks.set(position, bitmap2);
//                    imageAdapter = new ImageAdapter(getApplicationContext(), imageChunks, imageCoords);
//                    grid = (GridView) findViewById(R.id.gridView);
//                    grid.setAdapter(imageAdapter);
//                    grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
                    imageAdapter.notifyDataSetChanged();
                    v1.callOnClick();
//
//                    bitmap = imageChunksCopy.get(position);
//                    imageChunks.set(position, bitmap);
                }
                else{
                    Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (position != 17*17-1){

                    Bitmap bitmap = imageChunksCopy.get(position);
                    imageChunks.set(position, bitmap);
//                    View v1 = imageAdapter.getView(0,null, grid);
//                    Object v2 = imageAdapter.getItem(0);
//                    Log.d("Help me again", String.valueOf(v2));
//
//                    drawable = getResources().getDrawable(R.drawable.black);
//                        v1.setForeground(drawable);
//                        Log.d("Help again", String.valueOf(v1));
                    position += 1;
                    View v1 = imageAdapter.getView(position,null, grid);
                    v1.callOnClick();
                    Bitmap bitmap2 = BitmapFactory. decodeResource(getResources(), R. drawable.black);
                    imageChunks.set(position, bitmap2);
//                    imageAdapter = new ImageAdapter(getApplicationContext(), imageChunks, imageCoords);
//                    grid = (GridView) findViewById(R.id.gridView);
//                    grid.setAdapter(imageAdapter);
//                    grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
                    imageAdapter.notifyDataSetChanged();

                    v1.callOnClick();

//                    bitmap = imageChunksCopy.get(position);
//                    imageChunks.set(position, bitmap);

                }
                else{
                    Bitmap bitmap = imageChunksCopy.get(position);
                    imageChunks.set(position, bitmap);
//                    View v1 = imageAdapter.getView(0,null, grid);
//                    Object v2 = imageAdapter.getItem(0);
//                    Log.d("Help me again", String.valueOf(v2));
//
//                    drawable = getResources().getDrawable(R.drawable.black);
//                        v1.setForeground(drawable);
//                        Log.d("Help again", String.valueOf(v1));
                    position = 0;
                    View v1 = imageAdapter.getView(position,null, grid);
                    v1.callOnClick();
                    Bitmap bitmap2 = BitmapFactory. decodeResource(getResources(), R. drawable.black);
                    imageChunks.set(position, bitmap2);
//                    imageAdapter = new ImageAdapter(getApplicationContext(), imageChunks, imageCoords);
//                    grid = (GridView) findViewById(R.id.gridView);
//                    grid.setAdapter(imageAdapter);
//                    grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
                    imageAdapter.notifyDataSetChanged();

                    v1.callOnClick();

                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), String.valueOf(imageCoords.get(position).getX()) + ", " + String.valueOf(imageCoords.get(position).getY()) + ", Submitted", Toast.LENGTH_SHORT).show();
                String pointID = "MP-" + currentLocation.getLocationID() + "-" + (int) (imageCoords.get(position).getX()) + "-" + (int) (imageCoords.get(position).getY());
                point = new MapPoint(pointID, new Coordinate(imageCoords.get(position).getX(), imageCoords.get(position).getY()), currentLocation.getLocationID());

                numOfScans = 0;
                // re-initialise hash map each time the button is pressed
                allSignals = new HashMap<>();
                ssids = new HashMap<>();
                WifiScan.askAndStartScanWifi(LOG_TAG, MY_REQUEST_CODE, MapViewActivity.this);
                wifiManager.startScan();
            }
        });

//        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MapViewActivity.this, position, Toast.LENGTH_SHORT).show();
//            }
//        });

        }


    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Toast.makeText(MapViewActivity.this, "Scan " + numOfScans + " Complete!", Toast.LENGTH_SHORT).show();

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
                    // initialise for firebase
                    WAPFirebase<Signal> signalWAPFirebase = new WAPFirebase<>(Signal.class, "signals");
                    WAPFirebase<MapPoint> pointWAPFirebase = new WAPFirebase<>(MapPoint.class, "points");
                    ArrayList<Signal> signals = new ArrayList<>();
                    WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
                    int signalCounter = 0;

                    for (String macAddress : allSignals.keySet()) {

                        // get the average wifi signal if the BSSID exists
                        ArrayList<Integer> readings = allSignals.get(macAddress);
                        double averageSignal = WifiScan.calculateAverage(readings);
                        double stdDevSignal = WifiScan.calculateStandardDeviation(readings, averageSignal);
                        double averageSignalProcessed = WifiScan.calculateProcessedAverage(averageSignal);

                        Log.d(LOG_TAG, "MAC Address: " + macAddress + " , Wifi Signal: " + averageSignal + " , Wifi Signal (SD): " + stdDevSignal);

                        // posting the result to firebase
                        String signalID = "SG-" + locationID + "-" + (int) (imageCoords.get(position).getX()) + "-" + (int) (imageCoords.get(position).getY()) + "-" + signalCounter;
                        Signal signal = new Signal(signalID, locationID, macAddress, ssids.get(macAddress), stdDevSignal, averageSignal, averageSignalProcessed, 10);
                        signals.add(signal);
                        point.addSignalID(signalID);
                        signalCounter++;
                    }

                    pointWAPFirebase.create(point,point.getPointID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FIREBASE","map point successfully posted");
                        }
                    });
                    for (Signal signal : signals) {
                        Log.d("FIREBASE", "signalID: "+signal.getSignalID());
                        signalWAPFirebase.create(signal, signal.getSignalID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MapViewActivity.this, "Successfully created a point", Toast.LENGTH_SHORT).show();
                                currentLocation.incrementSignalCounter();
                                Log.d("FIREBASE", "signal successfully posted");
                                Log.d("Location ID", locationID);
                                Log.d("FIREBASE", "location: "+locationID);
                                locationWAPFirebase.update(currentLocation, locationID);
                            }
                        });
                    }
                }
            } else {
                Toast.makeText(MapViewActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
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

}