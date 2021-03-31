package com.example.wap;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestingActivity extends AppCompatActivity {

    Button locateBtn;
    TextView calculatedPointData;

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

    // wifi scan data from target location
    ArrayList<Double> targetDataOriginal;
    ArrayList<Double> targetData;
    ArrayList<Double> targetStdDev;
    ArrayList<String> targetMacAdd;

    private final String locationID = "TestLocation5";

    // data from firebase
    HashMap<String, ArrayList<String>> pointsFB;
    HashMap<String, Coordinate> pointsCoordinatesFB;
    HashMap<String, Integer> signalStrengthFB;
    HashMap<String, Integer> signalStrengthOriginalFB;
    HashMap<String, String> signalBSSIDFB;
    HashMap<String, Double> signalStrengthSDFB;

    // pre-matching data format
    ArrayList<Integer> fingerprintData;
    ArrayList<Coordinate> fingerprintCoordinate;

    // NEWLY ADDED FOR DATA CREATED AFTER PRE-MATCHING
    HashMap<String, HashMap<String, Integer>> fingerprintOriginalAvgSignal;
    HashMap<String, HashMap<String, Integer>> fingerprintAvgSignal;
    HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal;

    // create an arraylist to store the euclidean distance di values
    ArrayList<Double> euclideanArray = new ArrayList<>();
    // create an arraylist to store the jointprob i values
    ArrayList<Double> jointProbArray = new ArrayList<>();

    //list of fingerprint mac addresses
    ArrayList<Coordinate> CoorFingerprintKey = new ArrayList<>();
    // Weights for each algorithm in the weighted fusion algorithm
    private final double weightEuclidDist = 0.5;
    private final double weightJointProb = 0.5;
    private final double weightCosineSim = 1/3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        /*
        //comment out since the other codes are not in my branch yet
        bottomNavigationView.setSelectedItemId(R.id.testingActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.testingActivity:

                        return true;
                    case R.id.mappingActivity:
                        startActivity(new Intent(getApplicationContext(),MappingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.mainActivity:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

         */

        // Initialise XML elements
        locateBtn = (Button) findViewById(R.id.locateBtn);
        calculatedPointData = (TextView) findViewById(R.id.calculatedPointData);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialise hashmaps
                pointsFB = new HashMap<>();
                pointsCoordinatesFB = new HashMap<>();
                signalStrengthFB = new HashMap<>();
                signalBSSIDFB = new HashMap<>();
                signalStrengthSDFB = new HashMap<>();
                signalStrengthOriginalFB = new HashMap<>();

                fingerprintData = new ArrayList<>();
                fingerprintCoordinate = new ArrayList<>();

                fingerprintOriginalAvgSignal = new HashMap<>();
                fingerprintAvgSignal = new HashMap<>();
                fingerprintStdDevSignal = new HashMap<>();
                targetMacAdd = new ArrayList<>();
                targetData = new ArrayList<>();
                targetStdDev = new ArrayList<>();
                targetDataOriginal = new ArrayList<>();

                // retrieve data from firebase
                retrievefromFirebase();

                // collect wifi signals at target location
                numOfScans = 0;
                // re-initialise hash map each time the button is pressed
                allSignals = new HashMap<>();
                ssids = new HashMap<>();
                WifiScan.askAndStartScanWifi(LOG_TAG, MY_REQUEST_CODE, TestingActivity.this);
                wifiManager.startScan();
            }
        });
    }

    private void retrievefromFirebase() {
        WAPFirebase<MapPoint> wapFirebasePoints = new WAPFirebase<>(MapPoint.class,"points");
        WAPFirebase<Signal> wapFirebaseSignal = new WAPFirebase<>(Signal.class,"signals");

        wapFirebasePoints.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<MapPoint>>() {
            @Override
            public void onSuccess(ArrayList<MapPoint> mapPoints) {
                for (MapPoint point: mapPoints) {
                    String pointID = point.getPointID();
                    ArrayList<String> signalsIDs = point.getSignalIDs();
                    pointsFB.put(pointID, signalsIDs);
                    pointsCoordinatesFB.put(pointID, point.getCoordinate());
                    // Log.d("FIREBASE POINTS", pointID + "(" + point.getCoordinate().getX() + ", " + point.getCoordinate().getY() + ")" + " - " + signalsIDs.toString());
                }
            }
        });

        wapFirebaseSignal.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
            @Override
            public void onSuccess(ArrayList<Signal> signals) {
                for (Signal signal: signals) {
                    String signalID = signal.getSignalID();
                    String bssid = signal.getWifiBSSID();
                    double signalStrengthSD = signal.getSignalStrengthSD();
                    int signalStrength = signal.getSignalStrength();
                    signalStrengthFB.put(signalID, signalStrength);
                    signalStrengthOriginalFB.put(signalID, signalStrength);
                    // Log.d("FIREBASE SIGNAL", signalID + " - " + signalStrength);
                    signalStrengthSDFB.put(signalID, signalStrengthSD);
                    signalBSSIDFB.put(signalID, bssid);
                }
            }
        });
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
                Toast.makeText(TestingActivity.this, "Processing Wifi Scan " + numOfScans+1, Toast.LENGTH_SHORT).show();

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
                    // Log.d("WIFI SCAN", "MAC Address: " + result.BSSID + " , SSID: " + result.SSID + " , Wifi Signal: " + result.level);
                }

                // all scans completed
                if (numOfScans == 3) {
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

                        // Log.d("WIFI SCAN (FINAL)", "MAC Address: " + macAddress + " , Wifi Signal: " + averageSignal + " , Wifi Signal (SD): " + stdDevSignal);
                    }

                    Toast.makeText(TestingActivity.this, "Calculating Position...", Toast.LENGTH_SHORT).show();
                    String position = calculatePosition();
                    calculatedPointData.setText(position);
                }
            } else {
                Toast.makeText(TestingActivity.this, "Wifi scan failed. Please try again in 2 minutes.", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Wifi scan failed");
            }

            // continue scanning if it has not reached 4 scans + increase numOfScans
            numOfScans++;
            if (numOfScans < 4) {
                // Log.d(LOG_TAG, String.valueOf(numOfScans));
                // Log.d(LOG_TAG, "Started another scan");
                wifiManager.startScan();
            }
        }
    }

    private String calculatePosition() {
        // pre-matching fingerprints
        preMatching();

        // weighted fusion
        Coordinate calculatedPoint1 = euclideanDistance();
        Coordinate calculatedPoint2 = jointProbability();
        Coordinate finalPoint = weightedFusion(calculatedPoint1, calculatedPoint2);

        StringBuilder sb = new StringBuilder();
        sb.append("Euclidean Distance results: x = ");
        sb.append(calculatedPoint1.getX());
        sb.append(", y = ");
        sb.append(calculatedPoint1.getY());
        sb.append("\n");

        sb.append("Joint Probability results: x = ");
        sb.append(calculatedPoint2.getX());
        sb.append(", y = ");
        sb.append(calculatedPoint2.getY());
        sb.append("\n");

        sb.append("Weighted Fusion results: x = ");
        sb.append(finalPoint.getX());
        sb.append(", y = ");
        sb.append(finalPoint.getY());

        return sb.toString();
    }

    private void preMatching() {

        double threshold = 0.4;

        // get FLAG value
        double total = 0;
        for (double strength: targetData) {
            total += strength;
        }

        final double FLAG = total / targetData.size();

        // get a list of mac address where the signal strength pass the FLAG value
        ArrayList<String> filteredMac = new ArrayList<>();
        for (int i = 0; i < targetData.size(); i++) {
            double strength = targetData.get(i);
            if (Math.abs(strength) > Math.abs(FLAG)) {
                filteredMac.add(targetMacAdd.get(i));
            }
        }

        // compare bssid in each fingerprint with the list of bssid from wifi scan at target location
        for (String pointID: pointsFB.keySet()) {
            // System.out.println("Size of each fingerprint: " + pointsFB.get(pointID).size());
            int count = 0;
            boolean check = true;
            ArrayList<String> allSignals = pointsFB.get(pointID);
            ArrayList<String> listOfBSSID = new ArrayList<>();

            // for each signal, retrieve the corresponding bssid
            for (String signalID: allSignals) {
                listOfBSSID.add(signalBSSIDFB.get(signalID));
            }

            // compare the bssid between fingerprint and target location
            // if the bssid of the target location is not in the fingerprint, eliminate the fingerprint
            for (String bssid : filteredMac) {
                if (listOfBSSID.contains(bssid)) {
                    count++;
                }
            }

            // System.out.println("filtered mac: " + filteredMac.toString());
            // System.out.println("list of bssid: " + listOfBSSID.toString());

            // calculating the percentage match
            double percentMatch = (double) count / filteredMac.size();
            System.out.println(percentMatch);
            if (percentMatch < threshold) {
                check = false;
            }

            // if all the bssids are in the fingerprint, add fingerprint
            if (check) {
                // get coordinates of this fingerprint
                Coordinate coordinates = pointsCoordinatesFB.get(pointID);
                fingerprintCoordinate.add(coordinates);

                // convert coordinates to string to store as key values for the hashmaps
                StringBuilder str = new StringBuilder();
                str.append(coordinates.getX());
                str.append(", ");
                str.append(coordinates.getY());
                String coordinatesStr = str.toString();

                // create a hashmap for each mac address and corresponding signal strength at this fingerprint
                HashMap<String, Integer> avgSignalFingerprint = new HashMap<>();
                HashMap<String, Double> stdDevSignalFingerprint = new HashMap<>();
                HashMap<String, Integer> originalAvgSignalFingerprint = new HashMap<>();
                for (String signalID: allSignals) {
                    originalAvgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthOriginalFB.get(signalID));
                    avgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthFB.get(signalID));
                    stdDevSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthSDFB.get(signalID));
                }
                fingerprintOriginalAvgSignal.put(coordinatesStr, originalAvgSignalFingerprint);
                fingerprintAvgSignal.put(coordinatesStr, avgSignalFingerprint);
                fingerprintStdDevSignal.put(coordinatesStr, stdDevSignalFingerprint);
            }
        }

        // System.out.println("size of filtered fingerprints: " + fingerprintAvgSignal.keySet().size());
    }

    /**
     * fingerprintOriginalAvgSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, originalAverageWifiSignal>>
     * fingerprintAvgSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, averageWifiSignal>>
     * fingerprintStdDevSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, standardDeviationSignal>>
     * fingerprintCoordinate = list of filtered fingerprints
     *
     * targetMacAdd = list of mac address received at target location
     * targetStdDev = list of wifi signal standard deviation values for each mac address
     * targetData = list of average wifi signal values for each mac address
     * targetDataOriginal = list of original average wifi signal values for each mac address (FOR NOW, THESE HAVE THE SAME VALUES AS THE PROCESSED ONES)
     * Retrieve the corresponding average and standard deviation values for each mac address by the index value
     * */

    public Coordinate euclideanDistance() {
        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;

        //euclidean distance
        double euclideanDis = 0;
        ArrayList<String> coordinateKey = new ArrayList<>();

        //retrieve the keys of the fingerprint
        for (String coorStr : fingerprintAvgSignal.keySet()){
            coordinateKey.add(coorStr);
        }

        //the number of coordinates
        for (int i = 1; i <= fingerprintCoordinate.size() ; i++) {
            HashMap<String, Integer> subFingerprintAvgSignal = fingerprintAvgSignal.get(coordinateKey.get(i-1));
            HashMap<String, Double> subFingerprintStdDevSignal = fingerprintStdDevSignal.get(coordinateKey.get(i-1));

            //the number of the values of mac addresses
            for (int k = 1; k < targetData.size() + 1; k++) {
                //PAVG, DEV of k-th wifi signals at the target place
                Double pavgTarget = targetData.get(k - 1);
                Double devTarget = targetStdDev.get(k - 1);
                if (subFingerprintAvgSignal.containsKey(targetMacAdd.get(k-1))){
                    //PAVG, DEV of k-th wifi signals at the i-th fingerprint
                    Integer pavgFingerprint = subFingerprintAvgSignal.get(targetMacAdd.get(k - 1));
                    Double devFingerprint = subFingerprintStdDevSignal.get(targetMacAdd.get(k - 1));
                    //find the absolute value of pavg
                    Double absPavg = Math.abs(pavgTarget - pavgFingerprint);
                    double sqauredValue = Math.pow(absPavg + devTarget + devFingerprint, 2);
                    //sum it
                    euclideanDis += sqauredValue;
                }
            }
            euclideanDis = Math.sqrt(euclideanDis);
            euclideanArray.add(euclideanDis);
        }

        //calculate the coordinate
        for (int j = 1; j <= fingerprintCoordinate.size(); j++) {

            //find x and y multiplied by omega and sum all
            numeratorX += calculateXEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            numeratorY += calculateYEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            //omega
            denominatorPart += (1 / euclideanArray.get(j - 1));
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;

        Coordinate position = new Coordinate(x, y);
        euclideanArray.clear();

        return position;
    }

    public Coordinate jointProbability() {
        double Pik = 1;
        double jointProbi = 1;

        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;

        ArrayList<String> coordinateKeyJP = new ArrayList<>();

        for (int i = 1; i <fingerprintCoordinate.size()+1 ; i++){
            HashMap<String, Integer> subFingerprintOriginalAvgSignalJP = fingerprintOriginalAvgSignal.get(coordinateKeyJP.get(i-1));
            HashMap<String, Double> subFingerprintStdDevSignalJP = fingerprintStdDevSignal.get(coordinateKeyJP.get(i-1));

            for (int k = 1; k < targetDataOriginal.size() + 1; k++) {
                //AVGk, DEV of k-th wifi signals at the target place
                //x value
                Double avgTarget = targetDataOriginal.get(k - 1);
                System.out.println("avgTarget: " + avgTarget);
                if (subFingerprintOriginalAvgSignalJP.containsKey(targetMacAdd.get(k - 1))){
                    //mu value
                    Integer avgFingerprint = subFingerprintOriginalAvgSignalJP.get(targetMacAdd.get(k - 1));
                    //sigma
                    Double devFingerprint = subFingerprintStdDevSignalJP.get(targetMacAdd.get(k - 1));
                    //calculate Pik
                    Pik = calculateJointProb(avgTarget, avgFingerprint, devFingerprint);
                    //Pi = Pi1 * Pi2 * Pi3 * ... *Pik
                    //when standard deviation == 0, they have exact match on original wifi signal strength
                    jointProbi = jointProbi * Pik;
                    System.out.println("avgFingerprint: " + avgFingerprint + ", devFingerprint: " + devFingerprint + ", Pik: " + Pik + ", Joint Prob: " + jointProbi);
                }
            }
            jointProbArray.add(jointProbi);
        }

        System.out.println("Joint Probability Array: " + jointProbArray.toString());

        //calculate the coordinate
        for (int j = 1; j <= fingerprintCoordinate.size(); j++) {
            if (jointProbArray.get(j-1) != 0.0) {
                //find x and y multiplied by omega and sum all
                numeratorX += calculateXJointProb(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
                numeratorY += calculateYJointProb(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
                //omega
                denominatorPart += omegaJointProb(jointProbArray.get(j-1));
                System.out.println("numeratorX: " + numeratorX + ", numeratorY: " + numeratorY + "denominatorPart: " + denominatorPart);
            }
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;

        System.out.println("Final X: " + x + ", Final Y: " + y);

        //clear the array to be reusable
        jointProbArray.clear();
        return new Coordinate(x, y);
    }

    public Coordinate cosineSimilarity() {
        // TODO: cosine similarity positioning algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    public Coordinate weightedFusion(Coordinate euclidDistPosition, Coordinate jointProbPosition) {
        // Coordinate cosineSimPosition

        // Calculate the final X and Y
        // + weightCosineSim * cosineSimPosition.getX()
        // + weightCosineSim * cosineSimPosition.getY()
        double finalX = weightEuclidDist * euclidDistPosition.getX() + weightJointProb * jointProbPosition.getX();
        double finalY = weightEuclidDist * euclidDistPosition.getY() + weightJointProb * jointProbPosition.getY();
        System.out.println("euclidean x: " + euclidDistPosition.getX() + ", euclidean y: " + euclidDistPosition.getY());
        System.out.println("joint prob x: " + jointProbPosition.getX() + ", joint prob y: " + jointProbPosition.getY());
        System.out.println("Final coordinates: " + finalX + ", " + finalY);

        // return the calculated X and Y values
        return new Coordinate(finalX,finalY);
    }

    //helper method to calculate joint probability
    private double calculateJointProb(Double x, Integer mu, double sigma){

        if (sigma == 0.0) {
            sigma = 1.0;
        }
        double p1 = (sigma * Math.sqrt(2* Math.PI));
        double numerator = Math.pow(x-mu, 2);
        double denominator = 2* Math.pow(sigma, 2);
        double p2 = Math.exp(-numerator/denominator);
        System.out.println("p1: "+ p1 + ", numerator: " + numerator + ", denominator: " + denominator + ", p2: " + p2 + ", p1*p2: " + p1*p2);
        return p2 / p1;
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
        //no standard deviation means their matches are exact
        //hence, we don't need to add any coordinate value to the existing coordinate
        //Math.log10(1) = 0
        if (probability == 0){
            return Math.log10(1);
        }
        return Math.log10(probability);
    }

}