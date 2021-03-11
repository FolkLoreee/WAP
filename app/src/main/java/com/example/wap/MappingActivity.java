package com.example.wap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//firebase stuff
import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.reflect.Array;
import java.util.List;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class MappingActivity extends AppCompatActivity implements View.OnTouchListener {
    //WIFI Stuff
    private static final int MY_REQUEST_CODE = 123;
    private final static String LOG_TAG = "Mapping Activity";
    WifiManager wifiManager;
    WifiBroadcastReceiver wifiReceiver;
    MapPoint point;
    // XML Elements
    ImageView mapImage;
    Button level1Btn;
    Button level2Btn;
    Button undo;
    Button submit;
    TextView coordinatesText;

    //TODO: locationID will follow the locationID from the previous screen
    private final String locationID = "DebugLocation1";
    Location currentLocation = new Location("DebugLocation1", "Debug Location");

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
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    ArrayList<Path> paths = new ArrayList<Path>();
    ArrayList<Path> undonePaths = new ArrayList<Path>();

    Path mPath;
    boolean drag = false;
    boolean hasPath = false;
    int intrinsicHeight;
    int intrinsicWidth;
    int floor = R.drawable.floor_wap_1;
    Drawable drawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        signalWAPFirebase = new WAPFirebase<>(Signal.class, "signals");
        pointWAPFirebase = new WAPFirebase<>(MapPoint.class, "points");
        locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
        coordinatesText = (TextView) findViewById(R.id.coordinatesText);
        level1Btn = (Button) findViewById(R.id.level1Btn);
        level2Btn = (Button) findViewById(R.id.level2Btn);

        undo = (Button) findViewById(R.id.undo);
        submit = (Button) findViewById(R.id.submit);

        // Set up the map of level 1 by default
        mapImage = (ImageView) findViewById(R.id.mapImage);
        mapImage.setBackground(getResources().getDrawable(R.drawable.black));
        drawable = getResources().getDrawable(floor);

        //original height and width of the bitmap
        intrinsicHeight = drawable.getIntrinsicHeight();
        intrinsicWidth = drawable.getIntrinsicWidth();

        // Set up bitmap for ImageView
        bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        mPath = new Path();
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.floor_wap_1), 0, 0, null);
        paint = new Paint();
        paint.setColor(Color.RED);
        mapImage.setImageBitmap(bitmap);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Set up buttons
        level1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change after upload img funciton done
                floor = R.drawable.floor_wap_1;
                drawable = getResources().getDrawable(floor);

                //original height and width of the bitmap
                intrinsicHeight = drawable.getIntrinsicHeight();
                intrinsicWidth = drawable.getIntrinsicWidth();

                bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                mPath = new Path();
                canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), floor), 0, 0, null);
                paint = new Paint();
                paint.setColor(Color.RED);
                mapImage.setImageBitmap(bitmap);

                level1Btn.setBackgroundColor(getResources().getColor(R.color.grey));
                level2Btn.setBackgroundColor(getResources().getColor(R.color.purple_500));

            }
        });

        level2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change after upload img funciton done
                floor = R.drawable.floor_wap_2;
                drawable = getResources().getDrawable(floor);

                //original height and width of the bitmap
                intrinsicHeight = drawable.getIntrinsicHeight();
                intrinsicWidth = drawable.getIntrinsicWidth();

                bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                mPath = new Path();
                paths.add(mPath);
                canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), floor), 0, 0, null);
                paint = new Paint();
                paint.setColor(Color.RED);
                mapImage.setImageBitmap(bitmap);

                //mapImage.setBackground(getResources().getDrawable(R.drawable.floor_wap_2));
                level2Btn.setBackgroundColor(getResources().getColor(R.color.grey));
                level1Btn.setBackgroundColor(getResources().getColor(R.color.purple_500));
            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("undo", "undo");
                if (paths.size() > 0) {
                    undonePaths.add(paths.remove(paths.size() - 1));
                } else {
                    Toast.makeText(MappingActivity.this, "nothing to undo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askAndStartScanWifi();
            }
        });

        mapImage.setOnTouchListener(this);
    }


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

                        canvas.drawPath(mPath, paint);
                        mPath = new Path();
                        hasPath = true;
                    }

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

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void askAndStartScanWifi() {

        // With Android Level >= 23, you have to ask the user
        // for permission to Call.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // 23
            int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            // Check for permissions
            if (permission1 != PackageManager.PERMISSION_GRANTED) {

                Log.d(LOG_TAG, "Requesting Permissions");

                // Request permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.ACCESS_NETWORK_STATE
                        }, MY_REQUEST_CODE);
                return;
            }
            Log.d(LOG_TAG, "Permissions Already Granted");
        }
        doStartScanWifi();
    }

    private void doStartScanWifi() {
        wifiManager.startScan();
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
                Log.e(LOG_TAG,e.toString());
                Log.w(LOG_TAG, "Failure to fetch map point: " + pointID);
            }
        });

    }

    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            Toast.makeText(MappingActivity.this, "Scan Complete!", Toast.LENGTH_SHORT).show();

            boolean ok = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (ok) {
                Log.d(LOG_TAG, "Scan OK");

                List<ScanResult> list = wifiManager.getScanResults();
//                HashMap<String, Double> networkDistance = new HashMap<>();

                StringBuilder sb = new StringBuilder();
                ArrayList<Signal> signals = new ArrayList<>();
                int signalCounter = 0;

                for (ScanResult result : list) {
                    double distance = calculateDistance(result.level, result.frequency);
                    //                    networkDistance.put(result.SSID, distance);
                    System.out.println(result.SSID + " : " + distance + " m");
                    sb.append(result.SSID + ": " + distance + " m" + "\n");
                    //posting the result to firebase:
                    String signalID = "SG-" + locationID + "-" + signalCounter;
                    Signal signal = new Signal(signalID, locationID, result.SSID, result.frequency, result.level, 10);
                    Log.d(LOG_TAG, "LEVEL :" + result.level);
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
                    signalWAPFirebase.create(signal, signal.getSignalID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            currentLocation.incrementSignalCounter();
                            Log.d("FIREBASE", "signal successfully posted");
                            locationWAPFirebase.update(currentLocation, locationID);
                        }
                    });
                }
                Toast.makeText(MappingActivity.this, "Successfully created points", Toast.LENGTH_SHORT).show();


            } else {
                Log.d(LOG_TAG, "Scan not OK");
            }
        }
    }
}