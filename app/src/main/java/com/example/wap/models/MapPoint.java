package com.example.wap.models;

//the point created when mapping

import java.util.ArrayList;

public class MapPoint {
    private final static String TAG = "Map Point operations";


    private String pointID;
    private Coordinate coordinate;
    private String locationID;
    private ArrayList<Signal> signals;
    private  ArrayList<String> signalIDs;
    private int signalCount;



    public MapPoint(){}

    public MapPoint(String pointID, Coordinate coordinate, String locationID) {
        this.coordinate = coordinate;
        this.locationID = locationID;
        //id is MP-locationID-location.pointCounter
        this.pointID = pointID;
        this.signalIDs = new ArrayList<>();
        this.signals = new ArrayList<>();
    }

    public String getPointID() {
        return pointID;
    }

    public void setPointID(String id) {
        this.pointID = id;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public ArrayList<String> getSignalIDs() {
        return signalIDs;
    }

    public void setSignalIDs(ArrayList<String> signalIDs) {
        this.signalIDs = signalIDs;
    }
    public ArrayList<Signal> getSignals(){return signals;}
    public void addSignal(Signal signal){signals.add(signal);}

}
