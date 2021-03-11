package com.example.wap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TestingActivity extends AppCompatActivity {

    Button retrieveDB;
    Button locateBtn;
    TextView pointsData;
    TextView signalsData;

    // Wifi
    private static final int MY_REQUEST_CODE = 123;
    private final static String LOG_TAG = "Testing Activity";
    WifiManager wifiManager;
    TestingActivity.WifiBroadcastReceiver wifiReceiver;
    MapPoint point;

    // Wifi Scan
    ArrayList<String> macAddresses;
    int numOfScans;
    HashMap<String, ArrayList> allSignals;
    HashMap<String, String> ssids;

    private final String locationID = "DebugLocation1";
    Location currentLocation = new Location("DebugLocation1", "Debug Location");
    //retrieve from the database
    ArrayList<Integer> fingerprintData;
    ArrayList<Integer> targetData;
    ArrayList<Coordinate> fingerprintCoordinate;
    //for average, process-average, standard deviation calculations
    List<Integer> fingerprintDataIK;
    List<Integer> targetDataK;
    //create an arraylist to store the euclidean distance di values
    ArrayList<Double> euclideanArray = new ArrayList<>();
    //create an arraylist to store the jointprob i values
    ArrayList<Double> jointProbArray = new ArrayList<>();
    //firebase
    FirebaseFirestore db;
    String TAG = "i";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        // Initialise XML elements
        retrieveDB = (Button) findViewById(R.id.retrieveDB);
        locateBtn = (Button) findViewById(R.id.locateBtn);
        pointsData = (TextView) findViewById(R.id.pointsData);
        signalsData = (TextView) findViewById(R.id.signalsData);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numOfScans = 0;
                // re-initialise hash map each time the button is pressed
                allSignals = new HashMap<>();
                ssids = new HashMap<>();
                WifiScan.askAndStartScanWifi(LOG_TAG, MY_REQUEST_CODE, TestingActivity.this);
                wifiManager.startScan();
            }
        });

        // TODO: Retrieve data from Firebase
        WAPFirebase<MapPoint> wapFirebasePoints = new WAPFirebase<>(MapPoint.class,"points");
        WAPFirebase<Signal> wapFirebaseSignal = new WAPFirebase<>(Signal.class,"signals");

        // display data from DB
        retrieveDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringBuilder str = new StringBuilder();

                wapFirebasePoints.compoundQuery("locationID", "CampusCentre1").addOnSuccessListener(new OnSuccessListener<ArrayList<MapPoint>>() {
                    @Override
                    public void onSuccess(ArrayList<MapPoint> mapPoints) {
                        for (MapPoint point: mapPoints) {
                            Log.d("FIREBASE: ", point.toString());
                            str.append(point.toString());
                            str.append("\n");
                        }
                        // set text to display data
                        pointsData.setText(str);
                        Log.d("DISPLAY ", "TEXT");
                    }
                });
            }
        });

        //real-time for target data

        //firestore for fingerprint
        db = FirebaseFirestore.getInstance();
        //if the user is in level 1 (Have to add if-else condition)

        //Hard-code
        db.collection("signals")
                .whereEqualTo("locationID", "CampusCenter1")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot docu : task.getResult()) {
                                Log.d(TAG, docu.getId() + " => " + docu.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        // TODO: Pre-matching of fingerprints?
        preMatching();
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
            Log.d(LOG_TAG, "onReceive()");

            boolean resultsReceived = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (resultsReceived) {
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
                    for (String macAddress : allSignals.keySet()) {

                        // get the average wifi signal if the BSSID exists
                        ArrayList<Integer> readings = allSignals.get(macAddress);
                        int averageSignal = WifiScan.calculateAverage(readings);
                        int stdDevSignal = WifiScan.calculateStandardDeviation(readings, averageSignal);
                        int averageSignalProcessed = WifiScan.calculateProcessedAverage(averageSignal);

                        Log.d(LOG_TAG, "MAC Address: " + macAddress + " , Wifi Signal: " + averageSignal + " , Wifi Signal (SD): " + stdDevSignal);
                        Toast.makeText(TestingActivity.this, "Scan Complete!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
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

    private ArrayList<String> preMatching() {
        ArrayList<String> fingerprints = new ArrayList<>();

        return fingerprints;

    }

    public Coordinate euclideanDistance() {
        // TODO: Euclidean distance positioning algorithm (Hannah)
        //Coordinate position = new Coordinate(0,0);
        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;

        //euclidean distance
        double euclideanDis = 0;


        for (int i = 1; i <fingerprintData.size()+1 ; i++) {
            //System.out.println(i);

            for (int k = 1; k < targetData.size() + 1; k++) {
                targetDataK = targetData.subList(0, k);
                fingerprintDataIK = fingerprintData.subList(0, i);

                //PAVG, DEV of k-th wifi signals at the target place
                Integer avgTarget = calculateAverage(targetDataK);
                Integer pavgTarget = calculateProcessedAverage(avgTarget);
                Integer devTarget = calculateStandardDeviation(targetDataK, avgTarget);

                //PAVG, DEV of k-th wifi signals at the i-th fingerprint
                Integer avgFingerprint = calculateAverage(fingerprintDataIK);
                Integer pavgFingerprint = calculateProcessedAverage(avgFingerprint);
                Integer devFingerprint = calculateStandardDeviation(fingerprintDataIK, avgFingerprint);
                //find the absolute value of pavg
                Integer absPavg = Math.abs(pavgTarget - pavgFingerprint);
                double sqauredValue = Math.pow(absPavg + devTarget + devFingerprint, 2);
                //sum it
                euclideanDis += sqauredValue;

            }

            euclideanDis = Math.sqrt(euclideanDis);
            euclideanArray.add(euclideanDis);
        }

        //calculate the coordinate
        for (int j = 1; j < fingerprintCoordinate.size()+1 ; j++) {

            //find x and y multiplied by omega and sum all
            numeratorX += calculateXEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            numeratorY += calculateYEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            //omega
            denominatorPart += (1 / euclideanArray.get(j - 1));
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;

        Coordinate position = new Coordinate(x, y);

        return position;


    }

    public Coordinate jointProbability() {
        // TODO: joint probability positioning algorithm (Hannah)
        //Coordinate position = new Coordinate(0,0);
        double Pik = 1;
        double jointProbi = 1;

        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;

        for (int i = 1; i <fingerprintData.size()+1 ; i++){
            for (int k = 1; k < targetData.size()+1; k++ ){
                targetDataK = targetData.subList(0, k);
                fingerprintDataIK = fingerprintData.subList(0, i);
                //AVGk, DEV of k-th wifi signals at the target place
                //x value
                Integer avgTarget = calculateAverage(targetDataK);
                //mu value
                Integer avgFingerprint = calculateAverage(fingerprintDataIK);
                //sigma
                Integer devFingerprint = calculateStandardDeviation(fingerprintDataIK, avgFingerprint);
                //calculate Pik
                Pik = calculateJointProb(avgTarget, avgFingerprint, devFingerprint);
                //Pi = Pi1 * Pi2 * Pi3 * ... *Pik
                jointProbi = jointProbi * Pik;
            }
            jointProbArray.add(jointProbi);
        }
        System.out.println(jointProbArray);

        //calculate the coordinate
        for (int j = 1; j < fingerprintCoordinate.size()+1 ; j++) {
            //find x and y multiplied by omega and sum all
            numeratorX += calculateXJointProb(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            numeratorY += calculateYJointProb(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            //omega
            denominatorPart += omegaJointProb(jointProbArray.get(j-1));
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;
        //clear the array to be reusable
        jointProbArray.clear();
        return new Coordinate(x, y);
    }

    public Coordinate cosineSimilarity() {
        // TODO: cosine similarity positioning algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    public Coordinate weightedFusion() {
        // TODO: add different weights to each algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }
    //helper method to calculate joint probability
    private double calculateJointProb(Integer x, Integer mu, Integer sigma){
        //further improvement >> exception when sigma is zero
        double p1 = 1 / (sigma * Math.sqrt(2* Math.PI));
        double numerator = Math.pow(x- mu, 2);
        double denominator = 2* Math.pow(sigma, 2);
        double p2 = Math.exp(-numerator/denominator);

        return p1*p2;
    }

    //helper method to calculate numerator of coordinate
    private double calculateXEuclidean(double distance, Coordinate coordinate){
        //omega value
        double omega = 1 / distance;
        //x
        double x = coordinate.getX();
        x = omega * x;
        return x;
    }

    //helper method to calculate numerator of coordinate
    private double calculateYEuclidean(double distance, Coordinate coordinate){
        //omega value
        double omega = 1 / distance;
        //y
        double y = coordinate.getY();
        y = omega *y;

        return y;
    }

    //helper method to calculate numerator of coordinate
    private double calculateXJointProb(double probability, Coordinate coordinate){
        //omega value
        double omega = omegaJointProb(probability);
        //x
        double x = coordinate.getX();
        x = omega * x;
        return x;
    }

    //helper method to calculate numerator of coordinate
    private double calculateYJointProb(double probability, Coordinate coordinate){
        //omega value
        double omega = omegaJointProb(probability);
        //y
        double y = coordinate.getY();
        y = omega *y;

        return y;
    }

    private double omegaJointProb(double probability){
        return Math.log10(probability);
    }

    private Integer calculateAverage (List<Integer> readings) {
        Integer sum = 0;
        for (Integer reading: readings) {
            sum += reading;
        }
        Integer average = sum / readings.size();
        return average;
    }

    private Integer calculateStandardDeviation(List<Integer> readings, int average) {
        Integer sum = 0;
        for (Integer reading: readings) {
            sum += (reading - average);
        }
        sum /= readings.size();
        double sd = Math.sqrt((double) sum);
        return (int) sd;
    }

    // error handling on the original average wifi signal
    private Integer calculateProcessedAverage (Integer average) {
        int offset = 0;
        // systematic error
        int result = average + offset;
        // gross error

        // random error
        return result;
    }

}