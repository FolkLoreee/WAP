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
    ArrayList<Integer> targetDataOriginal;
    ArrayList<Integer> targetData;
    ArrayList<Integer> targetStdDev;
    ArrayList<String> targetMacAdd;

    private final String locationID = "CampusCentre1";

    // data from firebase
    HashMap<String, ArrayList<String>> pointsFB;
    HashMap<String, Coordinate> pointsCoordinatesFB;
    HashMap<String, Integer> signalStrengthFB;
    HashMap<String, Integer> signalStrengthOriginalFB;
    HashMap<String, String> signalBSSIDFB;
    HashMap<String, Integer> signalStrengthSDFB;

    // pre-matching data format
    ArrayList<Integer> fingerprintData;
    ArrayList<Coordinate> fingerprintCoordinate;

    // NEWLY ADDED FOR DATA CREATED AFTER PRE-MATCHING
    HashMap<String, HashMap<String, Integer>> fingerprintOriginalAvgSignal;
    HashMap<String, HashMap<String, Integer>> fingerprintAvgSignal;
    HashMap<String, HashMap<String, Integer>> fingerprintStdDevSignal;

    // for average, process-average, standard deviation calculations
    List<Integer> fingerprintDataIK;
    List<Integer> targetDataK;

    // create an arraylist to store the euclidean distance di values
    ArrayList<Double> euclideanArray = new ArrayList<>();
    // create an arraylist to store the jointprob i values
    ArrayList<Double> jointProbArray = new ArrayList<>();

    // Weights for each algorithm in the weighted fusion algorithm
    private final double weightEuclidDist = 1/3;
    private final double weightJointProb = 1/3;
    private final double weightCosineSim = 1/3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);
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

        // Initialise XML elements
        locateBtn = (Button) findViewById(R.id.locateBtn);
        calculatedPointData = (TextView) findViewById(R.id.calculatedPointData);

        // Initialise hashmaps
        pointsFB = new HashMap<>();
        pointsCoordinatesFB = new HashMap<>();
        signalStrengthFB = new HashMap<>();
        signalBSSIDFB = new HashMap<>();
        signalStrengthSDFB = new HashMap<>();
        signalStrengthOriginalFB = new HashMap<>();

        fingerprintOriginalAvgSignal = new HashMap<>();
        fingerprintAvgSignal = new HashMap<>();
        fingerprintStdDevSignal = new HashMap<>();
        targetMacAdd = new ArrayList<>();
        targetData = new ArrayList<>();
        targetStdDev = new ArrayList<>();
        targetDataOriginal = new ArrayList<>();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();

        // Register the receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    Log.d("FIREBASE POINTS", pointID + "(" + point.getCoordinate().getX() + ", " + point.getCoordinate().getY() + ")" + " - " + signalsIDs.toString());
                }
            }
        });

        wapFirebaseSignal.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
            @Override
            public void onSuccess(ArrayList<Signal> signals) {
                for (Signal signal: signals) {
                    String signalID = signal.getSignalID();
                    // String bssid = signal.getWifiBSSID();
                    // Integer signalStrengthSD = signal.getSignalStrengthSD();
                    int signalStrength = signal.getSignalStrength();
                    signalStrengthFB.put(signalID, signalStrength);
                    signalStrengthOriginalFB.put(signalID, signalStrength); // TODO: change accordingly after database is set up properly
                    Log.d("FIREBASE SIGNAL", signalID + " - " + signalStrength);
                    // signalStrengthSDFB.put(signalID, signalStrengthSD);
                    // signalBSSIDFB.put(signalID, bssid);
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
                Log.d(LOG_TAG, "Result of Scan " + numOfScans);

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
                    Log.d("WIFI SCAN", "MAC Address: " + result.BSSID + " , SSID: " + result.SSID + " , Wifi Signal: " + result.level);
                }

                Log.d(LOG_TAG, allSignals.toString());

                // all scans completed, send data to firebase
                if (numOfScans == 3) {
                    for (String macAddress: allSignals.keySet()) {

                        // get the average wifi signal if the BSSID exists
                        ArrayList<Integer> readings = allSignals.get(macAddress);
                        int averageSignal = WifiScan.calculateAverage(readings);
                        int stdDevSignal = WifiScan.calculateStandardDeviation(readings, averageSignal);
                        int averageSignalProcessed = WifiScan.calculateProcessedAverage(averageSignal);

                        // store these values into the data variables for wifi scan
                        targetDataOriginal.add(averageSignal);
                        targetData.add(averageSignalProcessed);
                        targetMacAdd.add(macAddress);
                        targetStdDev.add(stdDevSignal);

                        Log.d("WIFI SCAN (FINAL)", "MAC Address: " + macAddress + " , Wifi Signal: " + averageSignal + " , Wifi Signal (SD): " + stdDevSignal);
                        Toast.makeText(TestingActivity.this, "Scan Complete!", Toast.LENGTH_SHORT).show();

                        // pre-matching fingerprints
                        preMatching();

                        // weighted fusion
                        Coordinate calculatedPoint1 = euclideanDistance();
                        Coordinate calculatedPoint2 = jointProbability();
                        Coordinate finalPoint = weightedFusion();

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

                        calculatedPointData.setText(sb);
                    }
                }
            } else {
                Log.d(LOG_TAG, "Scan has issues");
            }

            // continue scanning if it has not reached 4 scans + increase numOfScans
            numOfScans++;
            if (numOfScans < 4) {
                Log.d(LOG_TAG, String.valueOf(numOfScans));
                Log.d(LOG_TAG, "Started another scan");
                wifiManager.startScan();
            }
        }
    }

    private void preMatching() {
        boolean check = true;

        // get FLAG value
        int total = 0;
        for (int strength: targetData) {
            total += strength;
        }

        final int FLAG = total / targetData.size();

        // get a list of mac address where the signal strength pass the FLAG value
        ArrayList<String> filteredMac = new ArrayList<>();
        for (int i = 0; i < targetData.size(); i++) {
            int strength = targetData.get(i);
            if (Math.abs(strength) > Math.abs(FLAG)) {
                filteredMac.add(targetMacAdd.get(i));
            }
        }

        // compare bssid in each fingerprint with the list of bssid from wifi scan at target location
        for (String pointID: pointsFB.keySet()) {
            ArrayList<String> allSignals = pointsFB.get(pointID);
            ArrayList<String> listOfBSSID = new ArrayList<>();

            // for each signal, retrieve the corresponding bssid
            for (String signalID: allSignals) {
                listOfBSSID.add(signalBSSIDFB.get(signalID));
            }

            // compare the bssid between fingerprint and target location
            // if the bssid of the target location is not in the fingerprint, eliminate the fingerprint
            for (String bssid : filteredMac) {
                if (!listOfBSSID.contains(bssid)) {
                    check = false;
                    break;
                }
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
                HashMap<String, Integer> stdDevSignalFingerprint = new HashMap<>();
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


        for (int i = 1; i <fingerprintData.size()+1 ; i++) {
            //System.out.println(i);

            for (int k = 1; k < targetData.size() + 1; k++) {
                targetDataK = targetData.subList(0, k);
                fingerprintDataIK = fingerprintData.subList(0, i);

                //PAVG, DEV of k-th wifi signals at the target place
                Integer avgTarget = WifiScan.calculateAverage(targetDataK);
                Integer pavgTarget = WifiScan.calculateProcessedAverage(avgTarget);
                Integer devTarget = WifiScan.calculateStandardDeviation(targetDataK, avgTarget);

                //PAVG, DEV of k-th wifi signals at the i-th fingerprint
                Integer avgFingerprint = WifiScan.calculateAverage(fingerprintDataIK);
                Integer pavgFingerprint = WifiScan.calculateProcessedAverage(avgFingerprint);
                Integer devFingerprint = WifiScan.calculateStandardDeviation(fingerprintDataIK, avgFingerprint);
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
                Integer avgTarget = WifiScan.calculateAverage(targetDataK);
                //mu value
                Integer avgFingerprint = WifiScan.calculateAverage(fingerprintDataIK);
                //sigma
                Integer devFingerprint = WifiScan.calculateStandardDeviation(fingerprintDataIK, avgFingerprint);
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
        // Get respective coordinates from each of the algorithm
        Coordinate euclidDistPosition = euclideanDistance();
        Coordinate jointProbPosition = jointProbability();
        // Coordinate cosineSimPosition = cosineSimilarity();

        // Calculate the final X and Y
        // + weightCosineSim * cosineSimPosition.getX()
        // + weightCosineSim * cosineSimPosition.getY()
        double finalX = weightEuclidDist * euclidDistPosition.getX() + weightJointProb * jointProbPosition.getX();
        double finalY = weightEuclidDist * euclidDistPosition.getX() + weightJointProb * jointProbPosition.getX();

        // return the calculated X and Y values
        return new Coordinate(finalX,finalY);
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

}