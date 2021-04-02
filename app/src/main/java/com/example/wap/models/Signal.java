package com.example.wap.models;

public class Signal {
    private String signalID;
    private String wifiSSID;
    private String wifiBSSID;
    //frequency of the AP
    private double signalStrengthSD;
    private String locationID;
    //strength of the signal
    private double signalStrength;
    private double signalStrengthProcessed;
    //the actual distance calculated by the algorithm
    private double calculatedDistance;
    //the measured distance from the map
    private double mapDistance;

    public Signal(String signalID, String locationID, String wifiBSSID, String wifiSSID, double signalStrengthSD, double signalStrength, double signalStrengthProcessed, double mapDistance){
        this.locationID = locationID;
        this.wifiBSSID = wifiBSSID;
        this.wifiSSID = wifiSSID;
        this.signalStrengthSD = signalStrengthSD;
        this.mapDistance = mapDistance;
        this.signalStrength = signalStrength;
        this.signalStrengthProcessed = signalStrengthProcessed;
        //id is SG-locationID-location.signalCounter
        // e.g. CampusCentre01-1, CampusCentre01-2, etc.
        this.signalID = signalID;
    }

    public Signal() {
    }

    public String getWifiBSSID() {
        return wifiBSSID;
    }

    public String getSignalID() {
        return signalID;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public double getFrequency() {
        return signalStrengthSD;
    }

    public String getLocationID() {
        return locationID;
    }

    public double getSignalStrength() {
        return signalStrength;
    }

    public double getSignalStrengthProcessed() {
        return signalStrengthProcessed;
    }

    public double getSignalStrengthSD() {
        return signalStrengthSD;
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

    public void setSignalStrengthOriginal(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public void setSignalStrengthProcessed(int signalStrengthProcessed) {
        this.signalStrengthProcessed = signalStrengthProcessed;
    }

    public void setSignalStrengthSD(int signalStrengthSD) {
        this.signalStrengthSD = signalStrengthSD;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

}
