package com.example.wap.models;

import android.graphics.Bitmap;
import android.location.Address;

public class Location {
    private String locationID;
    private String name;
    private int pointCounter;
    private int signalCounter;
    private Bitmap mapImage;
    private Address address;

    public Location(String locationID, String name){
        this.locationID = locationID;
        this.name = name;
        this.pointCounter = 0;
        this.signalCounter = 0;
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

    public Bitmap getMapImage() {
        return mapImage;
    }

    public void setMapImage(Bitmap mapImage) {
        this.mapImage = mapImage;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getPointCounter() {
        return pointCounter;
    }

    public int getSignalCounter() {
        return signalCounter;
    }

    public void setPointCounter(int pointCounter) {
        this.pointCounter = pointCounter;
    }

    public void setSignalCounter(int signalCounter) {
        this.signalCounter = signalCounter;
    }
    public void incrementSignalCounter(){
        this.setSignalCounter(this.getSignalCounter()+1);
    }
    public void incrementPointCounter(){
        this.setPointCounter(this.getPointCounter()+1);
    }
}
