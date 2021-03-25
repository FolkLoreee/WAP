package com.example.wap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;

//The adapter class associated with the ChunkedImageActivity class
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Bitmap> imageChunks;
    private ArrayList<ImageUploadAcitivity.CoordImages> imageCoords;
    private int imageWidth, imageHeight;
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;
    private final static String LOG_TAG = "Mapview Activity";
    WifiManager wifiManager;
    MappingActivity.WifiBroadcastReceiver wifiReceiver;
    private static final int MY_REQUEST_CODE = 123;
    private final String locationID = "DebugLocation1";
    Location currentLocation = new Location("DebugLocation1", "Debug Location");
    MapPoint point;

    //constructor
    public ImageAdapter(Context c, ArrayList<Bitmap> images, ArrayList<ImageUploadAcitivity.CoordImages> coords ) {
        mContext = c;
        imageChunks = images;
        imageWidth = images.get(0).getWidth();
        imageHeight = images.get(0).getHeight();
        imageCoords = coords;
    }

    @Override
    public int getCount() {
        return imageChunks.size();
    }

    @Override
    public Object getItem(int position) {
        return imageChunks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setImageChunks(int position){

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;
        int xCoord;
        int yCoord;

        gridView = new View(mContext);

        gridView = inflater.inflate(R.layout.map_button, null);

        ImageView image = (ImageView) gridView.findViewById(R.id.map);
        image.setLayoutParams(new GridView.LayoutParams(imageWidth+100, imageHeight+25));
        image.setPadding(0, 0, -10, 10);

        //set image of each grid
        image.setImageBitmap(imageChunks.get(position));
        xCoord = imageCoords.get(position).xcoord;
        yCoord = imageCoords.get(position).ycoord;

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                v.setBackgroundResource();
//                image.setImageResource(R.drawable.black);

//                Toast.makeText(mContext.getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                Log.v("Help", String.valueOf(xCoord) + ", " + String.valueOf(yCoord));

            }
        });

        return gridView;
    }

    //    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView image;
//
//        if(convertView == null){
//            image = new ImageView(mContext);
//
//            /*
//             * NOTE: I have set imageWidth - 10 and imageHeight
//             * as arguments to LayoutParams class.
//             * But you can take anything as per your requirement
//             */
//            image.setLayoutParams(new GridView.LayoutParams(imageWidth , imageHeight));
//            image.setPadding(0, 0, 0, 10);
//        }else{
//            image = (ImageView) convertView;
//        }
//        image.setImageBitmap(imageChunks.get(position));
//        return image;
//    }
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
                Toast.makeText(mContext.getApplicationContext(), "Scan " + numOfScans + " Complete!", Toast.LENGTH_SHORT).show();

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
                    // initialise for firebase
                    WAPFirebase<Signal> signalWAPFirebase = new WAPFirebase<>(Signal.class, "signals");
                    WAPFirebase<MapPoint> pointWAPFirebase = new WAPFirebase<>(MapPoint.class, "points");
                    ArrayList<Signal> signals = new ArrayList<>();
                    WAPFirebase<Location> locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
                    int signalCounter = 0;

                    for (String macAddress : allSignals.keySet()) {

                        // get the average wifi signal if the BSSID exists
                        ArrayList<Integer> readings = allSignals.get(macAddress);
                        int averageSignal = WifiScan.calculateAverage(readings);
                        double stdDevSignal = WifiScan.calculateStandardDeviation(readings, averageSignal);
                        int averageSignalProcessed = WifiScan.calculateProcessedAverage(averageSignal);

                        Log.d(LOG_TAG, "MAC Address: " + macAddress + " , Wifi Signal: " + averageSignal + " , Wifi Signal (SD): " + stdDevSignal);

                        // posting the result to firebase
                        String signalID = "SG-" + locationID + "-" + signalCounter;
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
                        signalWAPFirebase.create(signal, signal.getSignalID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(mContext.getApplicationContext(), "Successfully created a point", Toast.LENGTH_SHORT).show();
                                currentLocation.incrementSignalCounter();
                                Log.d("FIREBASE", "signal successfully posted");
                                locationWAPFirebase.update(currentLocation, locationID);
                            }
                        });
                    }
                }
            } else {
                Toast.makeText(mContext.getApplicationContext(), "Scan failed!", Toast.LENGTH_SHORT).show();
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
