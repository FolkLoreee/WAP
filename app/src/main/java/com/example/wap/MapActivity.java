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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//firebase stuff
public class MapActivity extends AppCompatActivity {

    // Wifi Stuff
    private static final int MY_REQUEST_CODE = 123;
    private final static String LOG_TAG = "Mapping Activity";
    WifiManager wifiManager;
    WifiBroadcastReceiver wifiReceiver;
    MapPoint point;

    // XML Elements
    ImageView mapImage;
    ImageButton up;
    ImageButton down;
    ImageButton left;
    ImageButton right;
    ImageButton scan;
    ImageButton mappinghelp;
    TextView coordinatesText;

    //TODO: locationID will follow the locationID from the previous screen

    String locationID;
    String locationName;
    String locationURL;

    Location currentLocation;
    static Coordinate coordinate;

    //Firebase
    WAPFirebase<MapPoint> pointWAPFirebase;
     WAPFirebase<Location> locationWAPFirebase;

    // We can be in one of these 3 states

    // Bitmap
    public static Bitmap bitmapImg;
    Canvas canvas;
    Paint paint;
    ArrayList<Path> paths = new ArrayList<Path>();

    public float[] pointToUpload = new float[2];

    Path mPath;
    int intrinsicHeight;
    int intrinsicWidth;
    int row = 20;
    int col = 20;
    int squareWidth, squareHeight;


    // Wifi Data and Scans
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;
    private ArrayList<String> approvedWifiSignals = new ArrayList<>(Arrays.asList(new String[]{"eduroam", "SUTD_Wifi", "SUTD_Lab", "SUTD_Guest", "SUTD_Test"}));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        pointWAPFirebase = new WAPFirebase<>(MapPoint.class, "points");
        locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");

        //set up the XML items
        coordinatesText = (TextView) findViewById(R.id.coordinatesText);
        up = (ImageButton) findViewById(R.id.up);
        down = (ImageButton) findViewById(R.id.down);
        left = (ImageButton) findViewById(R.id.left);
        right = (ImageButton) findViewById(R.id.right);
        scan = (ImageButton) findViewById(R.id.scan);
        mappinghelp = (ImageButton) findViewById(R.id.mappinghelp);

        //set coordinate as (0,0) on creation
        coordinate = new Coordinate(0, 0);

        // Set up the map
        mapImage = (ImageView) findViewById(R.id.mapImage);

        //original height and width of the bitmap
        intrinsicHeight = bitmapImg.getHeight();
        intrinsicWidth = bitmapImg.getWidth();

        //get the height and width of the square drawn
        squareHeight = intrinsicHeight / row;
        squareWidth = intrinsicWidth / col;


        // Set up bitmap for ImageView
        Bitmap bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);
        mPath = new Path();

        canvas.drawBitmap(bitmapImg, 0, 0, null);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);

        float[] center = centerOfRect(coordinate, squareWidth, squareHeight);
        coordinatesText.setText("( " + center[0] + " ," + center[1] + ")");
        mPath.addCircle(center[0], center[1], 15, Path.Direction.CW);
        canvas.drawPath(mPath, paint);

        mapImage.setImageBitmap(bitmap);
        mapImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Intent intent = getIntent();
        locationID = intent.getStringExtra(ImageSelectActivity.LOCATION_ID_KEY);
        locationName = intent.getStringExtra(ImageSelectActivity.LOCATION_NAME_KEY);
        locationURL = intent.getStringExtra(ImageSelectActivity.LOCATION_URL_KEY);

        Log.d(LOG_TAG, "LOCATION IS: " + locationID);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Set up buttons
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coordinate.getY() == 0) {
                    Log.d("right", "out of screen alrdy");
                } else {
                    coordinate.setY(coordinate.getY() - squareHeight);
                    drawFunction(coordinate, squareHeight, squareWidth, intrinsicHeight, intrinsicWidth, mapImage, paths, coordinatesText);

                }
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((coordinate.getY() + 2 * squareHeight) > canvas.getHeight()) {
                    Log.d("right", "out of screen alrdy");
                } else {
                    coordinate.setY(coordinate.getY() + squareHeight);
                    drawFunction(coordinate, squareHeight, squareWidth, intrinsicHeight, intrinsicWidth, mapImage, paths, coordinatesText);
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((coordinate.getX() + squareWidth) >= canvas.getWidth()) {
                    Log.d("right", "out of screen alrdy");
                } else {
                    coordinate.setX(coordinate.getX() + squareWidth);
                    drawFunction(coordinate, squareHeight, squareWidth, intrinsicHeight, intrinsicWidth, mapImage, paths, coordinatesText);
                }

            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coordinate.setX(coordinate.getX() - squareWidth);
                if (centerOfRect(coordinate, squareWidth, squareHeight)[0] < 0) {
                    coordinate.setX(coordinate.getX() + squareWidth);
                    Log.d("right", "out of screen alrdy");
                } else {
                    drawFunction(coordinate, squareHeight, squareWidth, intrinsicHeight, intrinsicWidth, mapImage, paths, coordinatesText);
                }
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Update the currentLocation details every time user wants to scan
                //This ensures one-source-of-truth
                locationWAPFirebase.query(locationID).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;

                        String pointID = "MP-" + currentLocation.getLocationID() + "-" + (int) pointToUpload[0] + "-" + (int) pointToUpload[1];
                        point = new MapPoint(pointID, new Coordinate(pointToUpload[0], pointToUpload[1]), currentLocation.getLocationID());

                        numOfScans = 0;
                        // re-initialise hash map each time the button is pressed

                        allSignals = new HashMap<>();
                        ssids = new HashMap<>();
                        WifiScan.askAndStartScanWifi(LOG_TAG, MY_REQUEST_CODE, MapActivity.this);
                        wifiManager.startScan();
                    }
                });
            }
        });
        mappinghelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapActivity.this, Popupactivity.class));

            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.choosemapactivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.testingActivity:
                        startActivity(new Intent(getApplicationContext(), TestingActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.mappingActivity:
                        startActivity(new Intent(getApplicationContext(), ChooseMapActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }


    public void drawFunction(Coordinate coordinate, int squareHeight, int squareWidth, int intrinsicHeight, int intrinsicWidth, ImageView mapImage, ArrayList<Path> paths, TextView coordinatesText) {

        Bitmap bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path mPath = new Path();
        canvas.drawBitmap(bitmapImg, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.RED);

        paint.setStrokeWidth(10);
        mapImage.setImageBitmap(bitmap);
        if (paths.size() != 0) {
            for (Path path : paths) {
                paint.setColor(Color.BLUE);
                canvas.drawPath(path, paint);
            }
        }
        paint.setColor(Color.RED);
        float[] center = centerOfRect(coordinate, squareWidth, squareHeight);
        coordinatesText.setText("( " + center[0] + " ," + center[1] + ")");

        pointToUpload[0] = center[0];
        pointToUpload[1] = center[1];

        mPath.addCircle(center[0], center[1], 15, Path.Direction.CW);

        canvas.drawPath(mPath, paint);
        Log.d("right", "drawn: " + (float) coordinate.getX() + ", " + (float) coordinate.getY());

    }

    public float[] centerOfRect(Coordinate coordinate, int squareWidth, int squareHeight) {
//        ( (x1 + x2) / 2, (y1 + y2) / 2 )
        float[] center = new float[2];
        center[0] = (float) (2 * coordinate.getX() + squareWidth) / 2;
        center[1] = (float) (2 * coordinate.getY() + squareHeight) / 2;

        return center;

    }

    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Toast.makeText(MapActivity.this, "Scan " + (numOfScans + 1) + " Complete!", Toast.LENGTH_SHORT).show();

                Log.d(LOG_TAG, "Result of Scan " + numOfScans);

                List<ScanResult> list = wifiManager.getScanResults();

                for (ScanResult result : list) {
                    if (numOfScans == 0) {
                        // TODO: should have a list of approved signals
                        ArrayList<Integer> signals = new ArrayList<>();
                        signals.add(result.level);
                        allSignals.put(result.BSSID, signals);
                        ssids.put(result.BSSID, result.SSID);
                    } else {
                        if (allSignals.containsKey(result.BSSID)) {
                            allSignals.get(result.BSSID).add(result.level);
                        }
                    }
                    Log.d(LOG_TAG, "MAC Address: " + result.BSSID + " , SSID: " + result.SSID + " , Wifi Signal: " + result.level);
                }

                Log.d(LOG_TAG, allSignals.toString());

                // all scans completed, send data to firebase
                if (numOfScans == 3) {
                    Toast.makeText(MapActivity.this, "All scans complete!", Toast.LENGTH_SHORT).show();
                    Path mPath = new Path();
                    mPath.addCircle((float) pointToUpload[0], (float) pointToUpload[1], 15, Path.Direction.CW);
                    paths.add(mPath);

                    // initialise for firebase
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
                        // only store wifi signals that are approved, filters out random hotspots
                        if (approvedWifiSignals.contains(ssids.get(macAddress))) {
                            String signalID = "SG-" + locationID + "-" + (int) (pointToUpload[0]) + "-" + (int) (pointToUpload[1]) + "-" + signalCounter;
                            Signal signal = new Signal(signalID, locationID, macAddress, ssids.get(macAddress), stdDevSignal, averageSignal, averageSignalProcessed, 10);
                            signals.add(signal);
                            point.addSignal(signal);
                            currentLocation.incrementSignalCounts();
                            signalCounter++;
                        }
                    }

                    pointWAPFirebase.create(point, point.getPointID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FIREBASE", "map point successfully posted");
                            currentLocation.incrementMapPointCounts();
                            currentLocation.addMapPointID(point.getPointID());
                            locationWAPFirebase.update(currentLocation, currentLocation.getLocationID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("FIREBASE", "location updated");
                                }
                            });
                        }
                    });
//                    for (Signal signal : signals) {
//                        Log.d("FIREBASE", "signalID: " + signal.getSignalID());
//                        signalWAPFirebase.create(signal, signal.getSignalID()).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Log.d("FIREBASE", "signal successfully posted");
//                                Log.d("Location ID", locationID);
//                                Log.d("FIREBASE", "location: " + locationID);
//                            }
//                        });
//                    }
                }
            } else {
                Toast.makeText(MapActivity.this, "Scan failed! Wait for a while and try again.", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStop() {
        this.unregisterReceiver(this.wifiReceiver);
        super.onStop();
    }
}
