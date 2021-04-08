package com.example.wap;

import com.example.wap.models.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class DataPrematching {
    /*
    // data from firebase
    public HashMap<String, ArrayList<String>> pointsFB;
    public HashMap<String, Coordinate> pointsCoordinatesFB;
    public HashMap<String, Double> signalStrengthFB;
    public HashMap<String, Double> signalStrengthOriginalFB;
    public HashMap<String, String> signalBSSIDFB;
    public HashMap<String, Double> signalStrengthSDFB;

     */

    // pre-matching data format
    ArrayList<Integer> fingerprintData;
    ArrayList<Coordinate> fingerprintCoordinate;

    // NEWLY ADDED FOR DATA CREATED AFTER PRE-MATCHING
    HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal;
    HashMap<String, HashMap<String, Double>> fingerprintAvgSignal;
    HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal;
    /*
    String locationID;

    public DataPrematching(String locationID){
        this.locationID = locationID;
    }

     */
    public void preMatching(ArrayList<Double> targetData, ArrayList<String> targetMacAdd, HashMap<String, ArrayList<String>> pointsFB, HashMap<String, Coordinate> pointsCoordinatesFB, HashMap<String, Double> signalStrengthFB, HashMap<String, Double> signalStrengthOriginalFB, HashMap<String, String> signalBSSIDFB, HashMap<String, Double> signalStrengthSDFB) {
        /*
        //retrieve from database
        DataRetrieval dataRev = new DataRetrieval(locationID);
        dataRev.retrievefromFirebase();
        pointsFB = dataRev.getPointsFB();
        signalBSSIDFB = dataRev.getSignalBSSIDFB();
        pointsCoordinatesFB = dataRev.getPointsCoordinatesFB();
        signalStrengthOriginalFB = dataRev.getSignalStrengthOriginalFB();
        signalStrengthSDFB = dataRev.getSignalStrengthSDFB();
        signalStrengthFB = dataRev.getSignalStrengthFB();
        System.out.println("SignalStrengthFB in prematching"+ signalStrengthFB);

         */


        double threshold = 0.4;

        // get FLAG value
        double total = 0;
        for (double strength: targetData) {
            total += strength;
        }

        final double FLAG = total / targetData.size();

        // get a list of mac address where the signal strength pass the FLAG value
        ArrayList<String> filteredMac = new ArrayList<>();
        for (int i = 0; i < targetData.size(); i++) {
            double strength = targetData.get(i);
            if (Math.abs(strength) > Math.abs(FLAG)) {
                filteredMac.add(targetMacAdd.get(i));
            }
        }


        // compare bssid in each fingerprint with the list of bssid from wifi scan at target location
        for (String pointID: pointsFB.keySet()) {
            // System.out.println("Size of each fingerprint: " + pointsFB.get(pointID).size());
            int count = 0;
            boolean check = true;
            ArrayList<String> allSignals = pointsFB.get(pointID);
            ArrayList<String> listOfBSSID = new ArrayList<>();

            // for each signal, retrieve the corresponding bssid
            for (String signalID: allSignals) {
                listOfBSSID.add(signalBSSIDFB.get(signalID));
            }

            // compare the bssid between fingerprint and target location
            // if the bssid of the target location is not in the fingerprint, eliminate the fingerprint
            for (String bssid : filteredMac) {
                if (listOfBSSID.contains(bssid)) {
                    count++;
                }
            }

            // System.out.println("filtered mac: " + filteredMac.toString());
            // System.out.println("list of bssid: " + listOfBSSID.toString());

            // calculating the percentage match
            double percentMatch = (double) count / filteredMac.size();
            System.out.println(percentMatch);
            if (percentMatch < threshold) {
                check = false;
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
                HashMap<String, Double> avgSignalFingerprint = new HashMap<>();
                HashMap<String, Double> stdDevSignalFingerprint = new HashMap<>();
                HashMap<String, Double> originalAvgSignalFingerprint = new HashMap<>();
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
    public HashMap<String, HashMap<String, Double>> getFingerprintOriginalAvgSignal() {
        return fingerprintOriginalAvgSignal;
    }

    public HashMap<String, HashMap<String, Double>> getFingerprintAvgSignal() {
        return fingerprintAvgSignal;
    }

    public HashMap<String, HashMap<String, Double>> getFingerprintStdDevSignal() {
        return fingerprintStdDevSignal;
    }
    public ArrayList<Coordinate> getFingerprintCoordinate() {
        return fingerprintCoordinate;
    }

}
