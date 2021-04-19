package com.example.wap;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Location;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class MapViewActivityTest {
    private static final String TAG = "MapViewActivity Instrumented Testing";
    Context activityContext;
    FirebaseFirestore db;
    FirebaseStorage storage;
    ActivityScenario<MapActivity> activityScenario;
    WAPFirebase<Signal> signalWAPFirebase;

    @Before
    public void setup(){
        signalWAPFirebase = new WAPFirebase<>(Signal.class,"signals");
    }

    @Test
    public void test_signal_indexing(){
        int counter = 0;
        String pointID = "MP-LOCATION-";
        MapPoint point = new MapPoint();
        ArrayList<String> signalIDs = new ArrayList<>();
        String signalID = "SG-"+pointID+"-"+counter;
        Signal signal = new Signal();
        signal.setSignalID(signalID);
        signalIDs.add(signalID);


    }
}
