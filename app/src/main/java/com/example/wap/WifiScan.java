package com.example.wap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

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
        int offset = 0;
        // systematic error
        double result = average + offset;
        // gross error - by movement of people - use T test

        // random error - HANDLED IN calculateAverage
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
}