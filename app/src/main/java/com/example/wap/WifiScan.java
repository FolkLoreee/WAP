package com.example.wap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WifiScan {

    public static Double calculateAverage(List<Integer> readings) {
        //RANDOM ERROR: Data Cleaning - if there's a missed reading in readings, then, set the average to -100
        if (readings.contains(null)){
            return -100.0;
        }
        Double sum = 0.0;
        for (Integer reading : readings) {
            sum += reading;
        }
        Double average = sum / readings.size();
        return average;
    }

    public static Double calculateStandardDeviation(List<Integer> readings, double average) {
        Double sum = 0.0;
        for (Integer reading : readings) {
            //RANDOM ERROR handling
            if (reading == null){
                //do not calculate the null values
                continue;
            }else{
                //square the absolute value of reading - average
                Double temporary = Math.abs(reading - average);
                sum += Math.pow(temporary, 2);
            }

        }
        Double intermediate =  sum / readings.size();
        double sd = Math.sqrt(intermediate);
        return sd;
    }

    // error handling on the original average wifi signal
    public static Double calculateProcessedAverage(Double average) {

        // systematic error
        double offset = 2.4083333333333328;
        double result = average + offset;

        // gross error - by movement of people - use T test

        return result;
    }

    public static void askAndStartScanWifi(String LOG_TAG, int MY_REQUEST_CODE, Activity activity) {

        // With Android Level >= 23, you have to ask the user
        // for permission to Call.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // 23
            int permission1 = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

            // Check for permissions
            if (permission1 != PackageManager.PERMISSION_GRANTED) {

                Log.d(LOG_TAG, "Requesting Permissions");

                // Request permissions
                ActivityCompat.requestPermissions(activity,
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
    }

    public static void compareDeviceWifiValues(String device1, String device2) {
        HashMap<String, ArrayList<Double>> wifiStrengths = new HashMap<>();
        WAPFirebase<Signal> wapFirebaseSignal = new WAPFirebase<>(Signal.class,"signals");
        AtomicDouble runningTotal = new AtomicDouble(0);
        AtomicInteger secondCount = new AtomicInteger(0);

        wapFirebaseSignal.compoundQuery("locationID", "Bldg2ThinkTank").addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
            @Override
            public void onSuccess(ArrayList<Signal> signals) {
                int count = 0;
                for (Signal signal : signals) {
                    if (signal.getSignalID().equals(device1+count)) {
                        System.out.println(signal.getSignalID());
                        String bssid = signal.getWifiBSSID();
                        double signalStrength = signal.getSignalStrength();
                        if (wifiStrengths.containsKey(bssid)) {
                            wifiStrengths.get(bssid).add(signalStrength);
                        } else {
                            ArrayList<Double> strengths = new ArrayList<>();
                            strengths.add(signalStrength);
                            wifiStrengths.put(bssid, strengths);
                        }
                        count++;
                    }
                }

                for (int j = 0; j < 75; j++) {
                    wapFirebaseSignal.compoundQuery("signalID", device2 + j).addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
                        @Override
                        public void onSuccess(ArrayList<Signal> signals) {
                            for (Signal signal : signals) {
                                System.out.println(signal.getSignalID());
                                String bssid = signal.getWifiBSSID();
                                if (wifiStrengths.containsKey(bssid)) {
                                    wifiStrengths.get(bssid).add(signal.getSignalStrength());
                                    double difference = wifiStrengths.get(bssid).get(0) - wifiStrengths.get(bssid).get(1);
                                    System.out.println("BSSID: " + bssid + ", Difference: " + difference);
                                    runningTotal.getAndAdd(difference);
                                }
                            }
                        }
                    }).addOnCompleteListener(new OnCompleteListener<ArrayList<Signal>>() {
                        @Override
                        public void onComplete(@NonNull Task<ArrayList<Signal>> task) {
                            int previousCount = secondCount.getAndAdd(1);
                            System.out.println(previousCount);

                            // once retrieved all values, then it will compute the average difference between the 2 devices
                            if (previousCount == 74) {
                                double averageDifference = runningTotal.doubleValue() / wifiStrengths.size();
                                System.out.println("Average Difference between 2 Devices: " + averageDifference);
                            }
                        }
                    });
                }
            }
        });
    }
}