package com.example.wap.models;

import android.graphics.Bitmap;
import android.location.Address;

import java.util.ArrayList;

public class Location {
    private String locationID;
    private String name;
    private String mapImage;
    private ArrayList<String> mapPointIDs;
    private int signalCounts;
    private int mapPointCounts;

    public Location(String locationID, String name, String mapImage) {
        this.locationID = locationID;
        this.name = name;
        this.mapPointIDs = new ArrayList<>();
        this.mapImage = mapImage;
    }

    public Location() {
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String id) {
        this.locationID = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapImage() {
        return mapImage;
    }

    public void setMapImage(String mapImage) {
        this.mapImage = mapImage;
    }

    public void addMapPointID(String pointID){
        ArrayList<String> newMapPoints = getMapPointIDs();
        newMapPoints.add(pointID);
        setMapPointIDs(newMapPoints);
    }
    public ArrayList<String> getMapPointIDs(){return mapPointIDs;}
    public void setMapPointIDs(ArrayList<String> mapPointIDs){this.mapPointIDs = mapPointIDs;}
    public int getSignalCounts(){return signalCounts;}
    public int getMapPointCounts(){return mapPointCounts;}
    public void setSignalCounts(int signalCounts){this.signalCounts = signalCounts;}
    public void setMapPointCounts(int mapPointCounts){this.mapPointCounts = mapPointCounts;}

    public void incrementSignalCounts(){
        int newCount = getSignalCounts();
        newCount++;
        setSignalCounts(newCount);
    }

    public void incrementMapPointCounts(){
        int newCount = getMapPointCounts();
        newCount++;
        setMapPointCounts(newCount);
    }
}


