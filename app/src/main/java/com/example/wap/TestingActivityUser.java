package com.example.wap;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestingActivityUser extends AppCompatActivity {

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
    TestingActivityUser.WifiBroadcastReceiver wifiReceiver;

    // Wifi Scan
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;

    // wifi scan data from target location
    ArrayList<Double> targetDataOriginal;
    ArrayList<Double> targetData;
    ArrayList<Double> targetStdDev;
    ArrayList<String> targetMacAdd;

    // private final String locationID = "CCLvl1";

    Algorithm algorithm;

    // Populate locations dropdown list
    TextView selectLocationText;
    Spinner locationSpinner;
    private String locationID = ""; // variable based on the image selected
    HashMap<String, ArrayList<String>> availableLocations;

    // Locating at regular interval
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 30000;
    boolean mapNotFound;
    boolean locationMapped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_user);

        // Initialise XML elements
        calculatedPointData = findViewById(R.id.calculatedPointData);
        mapImageView = findViewById(R.id.mapImageView);
        locationSpinner = findViewById(R.id.locationSpinner);
        selectLocationText = findViewById(R.id.selectLocationText);

        // Initialise hashmaps
        availableLocations = new HashMap<>();


        // Retrieve all the locations
        WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class,"locations");
        locationWAPFirebase.getCollection().addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
            @Override
            public void onSuccess(ArrayList<Location> locations) {
                ArrayList<String> locationsNames = new ArrayList<>();
                locationsNames.add("No Selection");

                for (Location l: locations) {
                    ArrayList<String> info = new ArrayList<>();
                    info.add(l.getLocationID());
                    info.add(l.getMapImage());
                    info.add(Integer.toString(l.getPointCounter()));
                    info.add(Integer.toString(l.getSignalCounter()));
                    // if location name is null, it will save the location ID instead
                    if (l.getName() != null) {
                        availableLocations.put(l.getName(), info);
                        locationsNames.add(l.getName());
                    }
                    else {
                        availableLocations.put(l.getLocationID(), info);
                        locationsNames.add(l.getLocationID());
                    }
                }

                // Populate the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(TestingActivityUser.this, android.R.layout.simple_spinner_item, locationsNames);

                locationSpinner.setAdapter(adapter);

                locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // set the image to be invisible
                        mapImageView.setVisibility(View.INVISIBLE);

                        // retrieve the selection location
                        String selectedLocation = (String) parent.getItemAtPosition(position);

                        // if the selected item is the default item
                        // set to the default drawable
                        if (selectedLocation.equals("No Selection")) {
                            mapImageView.setImageBitmap(null);
                            mapImageView.setImageDrawable(getResources().getDrawable(R.drawable.image_here));
                        }
                        // else, if it is a valid location from db,
                        // proceed get the image link of the map for that location
                        else {
                            locationID = availableLocations.get(selectedLocation).get(0);
                            System.out.println("selectedLocationID: " + locationID);
                            String mapImageAdd = availableLocations.get(selectedLocation).get(1);

                            if (android.os.Build.VERSION.SDK_INT > 9) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                try {
                                    // if there is a valid map for the location, load the map onto the imageView
                                    if (mapImageAdd != null) {
                                        mapImageView.setImageDrawable(null);
                                        URL url = new URL(mapImageAdd);
                                        mapImage = Utils.getBitmap(url);
                                        Bitmap bitmap = Bitmap.createBitmap((int) mapImage.getWidth(), (int) mapImage.getHeight(), Bitmap.Config.ARGB_8888);
                                        canvas = new Canvas(bitmap);
                                        mPath = new Path();
                                        canvas.drawBitmap(mapImage, 0, 0, null);
                                        paint = new Paint();
                                        paint.setColor(Color.RED);
                                        mapImageView.setImageBitmap(bitmap);
                                        calculatedPointData.setVisibility(View.VISIBLE);
                                    }
                                    // else, if there is no map, load the default drawble and indicate to user that there is no map
                                    else {
                                        mapNotFound = true;
                                        Toast.makeText(TestingActivityUser.this, "No map found, please upload a map for this location", Toast.LENGTH_SHORT).show();
                                        mapImageView.setImageBitmap(null);
                                        mapImageView.setImageDrawable(getResources().getDrawable(R.drawable.image_here));
                                    }
                                } catch (IOException e) {
                                    Log.d("Map cannot be displayed", String.valueOf(e));
                                }
                            }
                            // check if location has already mapped
                            // TODO: Need to edit once pointCounter is updated
                            if (availableLocations.get(selectedLocation).get(3).equals("0")) {
                                locationMapped = false;
                                Toast.makeText(TestingActivityUser.this, "Location has not been mapped, unable to locate user", Toast.LENGTH_SHORT).show();
                            } else {
                                locationMapped = true;
                            }
                        }
                        mapImageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                System.out.println("Checking boolean: mapNotFound - " + mapNotFound + ", locationMapped - " + locationMapped);
                // only if map is found and the location has been mapped before then it will proceed to relocate user
                if (!mapNotFound && locationMapped) {
                    handler.postDelayed(runnable, delay);
                    Toast.makeText(TestingActivityUser.this, "Locating user, please do not click anything...", Toast.LENGTH_SHORT).show();

                    // Initialise hashmaps
                    targetMacAdd = new ArrayList<>();
                    targetData = new ArrayList<>();
                    targetStdDev = new ArrayList<>();
                    targetDataOriginal = new ArrayList<>();

                    // re-initialise algorithm object for every scan
                    algorithm = new Algorithm();

                    // retrieve data from firebase
                    algorithm.retrievefromFirebase(locationID);

                    // collect wifi signals at target location
                    numOfScans = 0;

                    // re-initialise hash map each time the button is pressed
                    allSignals = new HashMap<>();
                    ssids = new HashMap<>();
                    WifiScan.askAndStartScanWifi(LOG_TAG, MY_REQUEST_CODE, TestingActivityUser.this);
                    wifiManager.startScan();
                }
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
        super.onPause();
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
            Log.d(LOG_TAG, "Wifi Scan: onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Toast.makeText(TestingActivityUser.this, "Processing Wifi Scan " + (numOfScans+1), Toast.LENGTH_SHORT).show();

                List<ScanResult> list = wifiManager.getScanResults();

                for (ScanResult result : list) {
                    if (numOfScans == 0) {
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
                }

                // all scans completed
                if (numOfScans == 3) {
                    Toast.makeText(TestingActivityUser.this, "Wifi Scan Complete", Toast.LENGTH_SHORT).show();
                    for (String macAddress: allSignals.keySet()) {

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

                    Coordinate position = calculatePosition();

                    // coordinates will only be displayed if both x and y are not NaN and not -1
                    if (position.getX() != -1 && position.getY() != -1 && !Double.isNaN(position.getX()) && !Double.isNaN(position.getY())) {
                        // clear previous dot if there is
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        // redraw the bitmap
                        canvas.drawBitmap(mapImage, 0, 0, null);

                        // draw the dot on the bitmap
                        canvas.drawCircle(doubleToFloat(position.getX()), doubleToFloat(position.getY()), 10, paint);

                        // display the calculated coordinates
                        calculatedPointData.setText(stringifyPosition(position));
                    }
                }
            } else {
                Toast.makeText(TestingActivityUser.this, "Wifi scan failed", Toast.LENGTH_SHORT).show();
            }

            // continue scanning if it has not reached 12 scans + increase numOfScans
            numOfScans++;
            if (numOfScans < 4) {
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

        Coordinate finalPoint = new Coordinate(-1, -1);

        // pre-matching fingerprints
        algorithm.preMatchingK(targetData, targetMacAdd);

        if (!algorithm.filteredFailed) {
            Coordinate calculatedPoint1 = algorithm.euclideanDistance(targetData, targetStdDev, targetMacAdd);
            Coordinate calculatedPoint2 = algorithm.jointProbability(targetDataOriginal, targetMacAdd);
            finalPoint = algorithm.weightedFusion(calculatedPoint1, calculatedPoint2);
        }
        else {
            Toast.makeText(TestingActivityUser.this, "You are likely out of the area, please move closer to the location shown in the map and try again", Toast.LENGTH_SHORT).show();
            calculatedPointData.setText("Coordinates cannot be calculated");
        }

        return finalPoint;
    }
}