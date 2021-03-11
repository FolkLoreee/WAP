package com.example.wap.models;

public class Signal {
    private String signalID;
    private String wifiSSID;
    //frequency of the AP
    private double frequency;
    private String locationID;
    //strength of the signal
    private int signalStrength;
    //the actual distance calculated by the algorithm
    private double calculatedDistance;
    //the measured distance from the map
    private double mapDistance;

    public Signal(){}
    public Signal(String signalID, String locationID, String wifiSSID, double frequency, int signalStrength, double mapDistance){
        this.locationID = locationID;
        this.wifiSSID = wifiSSID;
        this.frequency = frequency;
        this.mapDistance = mapDistance;
        this.signalStrength = signalStrength;
        //id is SG-locationID-location.signalCounter
        // e.g. CampusCentre01-1, CampusCentre01-2, etc.
        this.signalID = signalID;
    }


    public String getSignalID() {
        return signalID;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public double getFrequency() {
        return frequency;
    }

    public String getLocationID() {
        return locationID;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public double getCalculatedDistance() {
        return calculatedDistance;
    }

    public double getMapDistance() {
        return mapDistance;
    }

    public void setSignalID(String id) {
        this.signalID= id;
    }

    public void setCalculatedDistance(double calculatedDistance) {
        this.calculatedDistance = calculatedDistance;
    }

    public void setMapDistance(double mapDistance) {
        this.mapDistance = mapDistance;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }



}
