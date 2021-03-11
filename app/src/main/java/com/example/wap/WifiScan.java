package com.example.wap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class WifiScan {

    public static Integer calculateAverage(ArrayList<Integer> readings) {
        Integer sum = 0;
        for (Integer reading : readings) {
            sum += reading;
        }
        Integer average = sum / readings.size();
        return average;
    }

    public static Integer calculateStandardDeviation(ArrayList<Integer> readings, int average) {
        Integer sum = 0;
        for (Integer reading : readings) {
            sum += (reading - average);
        }
        sum /= readings.size();
        double sd = Math.sqrt((double) sum);
        return (int) sd;
    }

    // error handling on the original average wifi signal
    public static Integer calculateProcessedAverage(Integer average) {
        int offset = 0;
        // systematic error
        int result = average + offset;
        // gross error

        // random error
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