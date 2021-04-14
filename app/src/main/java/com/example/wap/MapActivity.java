package com.example.wap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
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
    Button up;
    Button down;
    Button left;
    Button right;
    Button scan;
    TextView coordinatesText;

    //TODO: locationID will follow the locationID from the previous screen
    public static String locationID;
    public static String locationName;


    Location currentLocation;

    static Coordinate coordinate;

    //Firebase
    WAPFirebase<Signal> signalWAPFirebase;
    WAPFirebase<MapPoint> pointWAPFirebase;
    WAPFirebase<Location> locationWAPFirebase;
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    // Bitmap
    public static Bitmap bitmapImg;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    ArrayList<Path> paths = new ArrayList<Path>();
    ArrayList<Path> undonePaths = new ArrayList<Path>();

    public float[] pointToUpload = new float[2];

    Path mPath;
    boolean drag = false;
    boolean hasPath = false;
    int intrinsicHeight;
    int intrinsicWidth;
    int row = 20;
    int col = 20;
    int[] displaySize = new int[2];
    int squareWidth, squareHeight;
//    int floor = R.drawable.floor_wap_1;
//    Drawable drawable;

    // Wifi Data and Scans
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;


    @SuppressWarnings("deprecation")
    private static int[] getDisplaySizeV9(Display display) {
        int x = display.getWidth();
        int y = display.getHeight();
        int[] displaySize = new int[2];
        displaySize[0] = x;
        displaySize[1] = y;
        return displaySize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        signalWAPFirebase = new WAPFirebase<>(Signal.class, "signals");
        pointWAPFirebase = new WAPFirebase<>(MapPoint.class, "points");
        locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");

        //set up the XML items
        coordinatesText = (TextView) findViewById(R.id.coordinatesText);
        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        scan = (Button) findViewById(R.id.scan);

        //set coordinate as (0,0) on creation
        coordinate = new Coordinate(0, 0);


        // Set up the map of level 1 by default
        mapImage = (ImageView) findViewById(R.id.mapImage);
        mapImage.setImageBitmap(bitmapImg);
        mapImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

//        mapImage.setBackground(getResources().getDrawable(R.drawable.black));




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


//        mapImage.setImageBitmap(bitmap);

//        mPath.addRect(0,0,squareWidth, squareHeight, Path.Direction.CW);
//        canvas.drawPath(mPath, paint);
        float[] center = centerOfRect(coordinate, squareWidth, squareHeight);
        coordinatesText.setText("( " + String.valueOf(center[0]) + " ," + String.valueOf(center[1]) + ")");
        mPath.addCircle(center[0], center[1], 15, Path.Direction.CW);
        canvas.drawPath(mPath, paint);


        Intent intent = getIntent();
        locationID = intent.getStringExtra("locationID");
        Log.d(LOG_TAG, "LOCATION IS: " + locationID);

        currentLocation = new Location(locationID, locationName);
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
                if ((coordinate.getY() + squareHeight) > canvas.getHeight()) {
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
                if ((coordinate.getX() + 2 * squareWidth) >= canvas.getWidth()) {
                    Log.d("right", "out of screen alrdy");
                }
//                else if (c){
//
//                }

                else {
                    coordinate.setX(coordinate.getX() + squareWidth);
                    drawFunction(coordinate, squareHeight, squareWidth, intrinsicHeight, intrinsicWidth, mapImage, paths, coordinatesText);

                }

            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((coordinate.getX()) == 0 && coordinate.getY() == 0) {
                    Log.d("right", "out of screen alrdy");
                } else {
                    coordinate.setX(coordinate.getX() - squareWidth);
                    drawFunction(coordinate, squareHeight, squareWidth, intrinsicHeight, intrinsicWidth, mapImage, paths, coordinatesText);

                }
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String pointID = "MP-" + currentLocation.getLocationID() + "-" + (int)  pointToUpload[0] + "-" + (int) pointToUpload[1];
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

    public void drawFunction(Coordinate coordinate, int squareHeight, int squareWidth, int intrinsicHeight, int intrinsicWidth, ImageView mapImage, ArrayList<Path> paths, TextView coordinatesText) {

//        bitmap = bitmapImg.copy(bitmapImg.getConfig(), true);
        Bitmap bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path mPath = new Path();
        canvas.drawBitmap(bitmapImg, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        mapImage.setImageBitmap(bitmap);
//        mPath.addRect((float)coordinate.getX(), (float) coordinate.getY(), (float)coordinate.getX() + squareWidth, (float)coordinate.getY() + squareHeight, Path.Direction.CW);
        if (paths.size() != 0) {
            for (Path path : paths) {
                paint.setColor(Color.BLUE);
                canvas.drawPath(path, paint);
            }
        }
        float[] center = centerOfRect(coordinate, squareWidth, squareHeight);
        coordinatesText.setText("( " + String.valueOf(center[0]) + " ," + String.valueOf(center[1]) + ")");

        pointToUpload[0] = center[0];
        pointToUpload[1] = center[1];

        mPath.addCircle(center[0], center[1], 15, Path.Direction.CW);

        canvas.drawPath(mPath, paint);
//                    Toast.makeText(MapActivity.this, "drawn", Toast.LENGTH_SHORT).show();
        Log.d("right", "drawn: " + (float) coordinate.getX() + ", " + (float) coordinate.getY());
//                    canvas.drawPath(mPath, paint);

    }

    public float[] centerOfRect(Coordinate coordinate, int squareWidth, int squareHeight) {
//        ( (x1 + x2) / 2, (y1 + y2) / 2 )
        float[] center = new float[2];
        center[0] = (float) (2 * coordinate.getX() + squareWidth) / 2;
        center[1] = (float) (2 * coordinate.getY() + squareHeight) / 2;

        return center;

    }

/*
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);

        float relativeX;
        float relativeY;
        float[] values = new float[9];

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_UP:
                matrix.getValues(values);
                relativeX = (event.getX() - values[2]) / values[0];
                relativeY = (event.getY() - values[5]) / values[4];
                mPath.addCircle(relativeX, relativeY, 10, Path.Direction.CW);

                String pointID = "MP-" + currentLocation.getLocationID() + "-" + (int) (relativeX) + "-" + (int) (relativeY);
                point = new MapPoint(pointID, new Coordinate(relativeX, relativeY), currentLocation.getLocationID());

                if (drag) {
                    Log.i("path added", "path added");
                    mPath = new Path();
                    drag = false;
                } else {
                    if (hasPath) {
                        bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(bitmap);
                        mPath = new Path();
                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), floor), 0, 0, null);
                        paint = new Paint();
                        paint.setColor(Color.RED);
                        mapImage.setImageBitmap(bitmap);
                        paths.clear();
                        mPath.addCircle(relativeX, relativeY, 10, Path.Direction.CW);
                        Toast.makeText(MappingActivity.this, "drawn", Toast.LENGTH_SHORT).show();
                        canvas.drawPath(mPath, paint);
                        mPath = new Path();
                        hasPath = true;
                    }
                    Toast.makeText(MappingActivity.this, "drawn", Toast.LENGTH_SHORT).show();
                    canvas.drawPath(mPath, paint);
                    mPath = new Path();
//                    paths.add(mPath);
                    hasPath = true;

                }
//                mPath = new Path();
//                paths.add(mPath);

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) {
                    Log.i("become drag", "became drag");
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    drag = true;


                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        drag = true;
                    }
                }
                break;
        }

        StringBuilder sb = new StringBuilder();

        matrix.getValues(values);

        // values[2] and values[5] are the x,y coordinates of the top left corner of the drawable image, regardless of the zoom factor.
        // values[0] and values[4] are the zoom factors for the image's width and height respectively.
        // If you zoom at the same factor, these should both be the same value.
        // event is the touch event for MotionEvent.ACTION_UP
        relativeX = (event.getX() - values[2]) / values[0];
        relativeY = (event.getY() - values[5]) / values[4];
        sb.append("x: " + relativeX + ", y: " + relativeY);

        coordinatesText.setText(sb);

        //canvas.drawCircle(relativeX, relativeY, 10, paint);

        view.setImageMatrix(matrix);

        return true;
    }
*/
    /**
     * Determine the space between the first two fingers
     */

    /*
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

     */

    /**
     * Calculate the mid point of the first two fingers
     */

    /*
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    protected void onStop() {
        this.unregisterReceiver(this.wifiReceiver);
        super.onStop();
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

     */
    public void removeSignals(String pointID) {
        Log.d(LOG_TAG, "Attempting to remove signals");
        pointWAPFirebase.query(pointID).addOnSuccessListener(mapPoint -> {
            Log.d(LOG_TAG, "Map point is: " + mapPoint.getPointID());
            ArrayList<String> signalIDs = mapPoint.getSignalIDs();
            for (String signalID : signalIDs) {
                signalWAPFirebase.delete(signalID).addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Successfully removed signal: " + signalID)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "Failure to remove signal" + signalID);

                    }
                });
            }
            pointWAPFirebase.delete(pointID).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(LOG_TAG, "Successfully removed map point" + pointID);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(LOG_TAG, "Failure to remove map point" + pointID);
                }
            });
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(LOG_TAG, e.toString());
                Log.w(LOG_TAG, "Failure to fetch map point: " + pointID);
            }
        });

    }

    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Toast.makeText(MapActivity.this, "Scan " + (numOfScans+1) + " Complete!", Toast.LENGTH_SHORT).show();

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
                    Path mPath = new Path();
                    mPath.addCircle((float) pointToUpload[0], (float) pointToUpload[1], 15, Path.Direction.CW);
                    paths.add(mPath);
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
                        String signalID = "SG-" + locationID + "-" + (int) (pointToUpload[0]) + "-" + (int) (pointToUpload[1]) + "-" + signalCounter;
                        Signal signal = new Signal(signalID, locationID, macAddress, ssids.get(macAddress), stdDevSignal, averageSignal, averageSignalProcessed, 10);
                        signals.add(signal);
                        point.addSignalID(signalID);
                        signalCounter++;
                    }

                    pointWAPFirebase.create(point, point.getPointID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FIREBASE", "map point successfully posted");
                        }
                    });
                    for (Signal signal : signals) {
                        Log.d("FIREBASE", "signalID: " + signal.getSignalID());
                        signalWAPFirebase.create(signal, signal.getSignalID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MapActivity.this, "Successfully created a point", Toast.LENGTH_SHORT).show();
                                currentLocation.incrementSignalCounter();
                                Log.d("FIREBASE", "signal successfully posted");
                                Log.d("Location ID", locationID);
                                Log.d("FIREBASE", "location: " + locationID);
                                locationWAPFirebase.update(currentLocation, locationID);
                            }
                        });
                    }
                }
            } else {
                Toast.makeText(MapActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
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
