package com.example.wap;

import androidx.annotation.NonNull;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Algorithm {

    // pre-matching data format
    ArrayList<Integer> fingerprintData;
    ArrayList<Coordinate> fingerprintCoordinate;

    // data format created after pre-matching
    HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal;
    HashMap<String, HashMap<String, Double>> fingerprintAvgSignal;
    HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal;

    // create an arraylist to store the euclidean distance di values
    ArrayList<Double> euclideanArray;
    // create an arraylist to store the jointprob i values
    ArrayList<Double> jointProbArray;

    boolean filteredFailed = false;

    // data from firebase
    HashMap<String, ArrayList<String>> pointsFB;
    HashMap<String, Coordinate> pointsCoordinatesFB;
    HashMap<String, Double> signalStrengthFB;
    HashMap<String, Double> signalStrengthOriginalFB;
    HashMap<String, String> signalBSSIDFB;
    HashMap<String, Double> signalStrengthSDFB;

    // constants
    private final double weightEuclidDist = 0.50;
    private final double weightJointProb = 0.50;
    private final int k = 4;
    private final double threshold = 0.7;
    private final int wifiThreshold = -60;
    private final int proximityThreshold = 8;

    /**
     * fingerprintOriginalAvgSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, originalAverageWifiSignal>>
     * fingerprintAvgSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, averageWifiSignal>>
     * fingerprintStdDevSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, standardDeviationSignal>>
     * fingerprintCoordinate = list of filtered fingerprints
     *
     * targetMacAdd = list of mac address received at target location
     * targetStdDev = list of wifi signal standard deviation values for each mac address
     * targetData = list of average wifi signal values for each mac address
     * targetDataOriginal = list of original average wifi signal values for each mac address (FOR NOW, THESE HAVE THE SAME VALUES AS THE PROCESSED ONES)
     * Retrieve the corresponding average and standard deviation values for each mac address by the index value
     * */

    public Algorithm(){
        pointsFB = new HashMap<>();
        pointsCoordinatesFB = new HashMap<>();
        signalStrengthFB = new HashMap<>();
        signalBSSIDFB = new HashMap<>();
        signalStrengthSDFB = new HashMap<>();
        signalStrengthOriginalFB = new HashMap<>();

        fingerprintData = new ArrayList<>();
        fingerprintCoordinate = new ArrayList<>();

        fingerprintOriginalAvgSignal = new HashMap<>();
        fingerprintAvgSignal = new HashMap<>();
        fingerprintStdDevSignal = new HashMap<>();

        euclideanArray = new ArrayList<>();
        jointProbArray = new ArrayList<>();

    }

    public Algorithm(HashMap<String, ArrayList<String>> pointsFB, HashMap<String, Coordinate> pointsCoordinatesFB, HashMap<String, Double> signalStrengthFB, HashMap<String, Double> signalStrengthOriginalFB, HashMap<String, String> signalBSSIDFB, HashMap<String, Double> signalStrengthSDFB) {
        this.pointsFB = pointsFB;
        this.pointsCoordinatesFB = pointsCoordinatesFB;
        this.signalStrengthFB = signalStrengthFB;
        this.signalBSSIDFB = signalBSSIDFB;
        this.signalStrengthSDFB = signalStrengthSDFB;
        this.signalStrengthOriginalFB = signalStrengthOriginalFB;

        fingerprintData = new ArrayList<>();
        fingerprintCoordinate = new ArrayList<>();

        fingerprintOriginalAvgSignal = new HashMap<>();
        fingerprintAvgSignal = new HashMap<>();
        fingerprintStdDevSignal = new HashMap<>();

        euclideanArray = new ArrayList<>();
        jointProbArray = new ArrayList<>();
    }

    public Algorithm(HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal, HashMap<String, HashMap<String, Double>> fingerprintAvgSignal, HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal, ArrayList<Coordinate> fingerprintCoordinate) {
        this.fingerprintOriginalAvgSignal = fingerprintOriginalAvgSignal;
        this.fingerprintAvgSignal = fingerprintAvgSignal;
        this.fingerprintStdDevSignal = fingerprintStdDevSignal;

        this.fingerprintCoordinate = fingerprintCoordinate;
        this.euclideanArray = new ArrayList<>();
        this.jointProbArray = new ArrayList<>();
    }

    public void retrievefromFirebase(String locationID) {
        WAPFirebase<MapPoint> wapFirebasePoints = new WAPFirebase<>(MapPoint.class,"points");
        WAPFirebase<Signal> wapFirebaseSignal = new WAPFirebase<>(Signal.class,"signals");

        wapFirebasePoints.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<MapPoint>>() {
            @Override
            public void onSuccess(ArrayList<MapPoint> mapPoints) {
                for (MapPoint point: mapPoints) {
                    String pointID = point.getPointID();
                    ArrayList<String> signalsIDs = point.getSignalIDs();
                    pointsFB.put(pointID, signalsIDs);
                    pointsCoordinatesFB.put(pointID, point.getCoordinate());
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<ArrayList<MapPoint>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<MapPoint>> task) {
                wapFirebaseSignal.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
                    @Override
                    public void onSuccess(ArrayList<Signal> signals) {
                        for (Signal signal: signals) {
                            String signalID = signal.getSignalID();
                            String bssid = signal.getWifiBSSID();
                            double signalStrengthSD = signal.getSignalStrengthSD();
                            double signalStrength = signal.getSignalStrength();
                            // double processedSignalStrength = signal.getSignalStrengthProcessed();
                            signalStrengthFB.put(signalID, signalStrength);
                            signalStrengthOriginalFB.put(signalID, signalStrength);
                            signalStrengthSDFB.put(signalID, signalStrengthSD);
                            signalBSSIDFB.put(signalID, bssid);
                        }
                    }
                });
            }
        });
    }

    // New Firebase Retrieval
    public void retrievefromFirebase2(String locationID) {
        WAPFirebase<MapPoint> wapFirebasePoints = new WAPFirebase<>(MapPoint.class,"points");

        wapFirebasePoints.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<MapPoint>>() {
            @Override
            public void onSuccess(ArrayList<MapPoint> mapPoints) {
                for (MapPoint point: mapPoints) {
                    String pointID = point.getPointID();
                    ArrayList<String> signalsIDs = new ArrayList<>();
                    pointsCoordinatesFB.put(pointID, point.getCoordinate());
                    ArrayList<Signal> signals = point.getSignals();
                    for (Signal signal: signals) {
                        String signalID = signal.getSignalID();
                        signalsIDs.add(signalID);
                        String bssid = signal.getWifiBSSID();
                        double signalStrengthSD = signal.getSignalStrengthSD();
                        double signalStrength = signal.getSignalStrength();
                        double processedSignalStrength = signal.getSignalStrengthProcessed();
                        signalStrengthFB.put(signalID, processedSignalStrength);
                        signalStrengthOriginalFB.put(signalID, signalStrength);
                        signalStrengthSDFB.put(signalID, signalStrengthSD);
                        signalBSSIDFB.put(signalID, bssid);
                    }
                    pointsFB.put(pointID, signalsIDs);
                }
//                System.out.println("PRINTING FIREBASE RECORDS");
//                System.out.println("all points" + pointsFB);
//                System.out.println("coordinates of all points" + pointsCoordinatesFB);
//                System.out.println("processed signal strength: " + signalStrengthFB);
//                System.out.println("original signal strength: " + signalStrengthOriginalFB);
//                System.out.println("standard deviation of signal strength: " + signalStrengthSDFB);
//                System.out.println("BSSIDs: " + signalBSSIDFB);
            }
        });
    }

    public String stringifyCoordinates(Coordinate coordinates) {
        // convert coordinates to string to store as key values for the hashmaps

        // if coordinates are null, return an empty string
        if (coordinates == null) {
            return "";
        }

        StringBuilder str = new StringBuilder();
        str.append(coordinates.getX());
        str.append(", ");
        str.append(coordinates.getY());
        String coordinatesStr = str.toString();

        return coordinatesStr;
    }

    public double calculateFlag(ArrayList<Double> targetData) {

        double total = 0;
        int numOfSignals = 0;

        // cover for edge case where targetData is empty
        if (targetData.size() == 0) {
            return 0.0;
        }

        for (double strength: targetData) {
            // only wifi signals that are above -80 will be taken into calculation
            // this is so that the average values are computed with stronger wifi signals and do not deviate because of weaker signals
            if (strength > wifiThreshold) {
                total += strength;
                numOfSignals += 1;
            }
        }

        return total / numOfSignals;
    }

    public HashMap<String, Double> filterWifiByFlag(ArrayList<Double> targetData, double FLAG, ArrayList<String> targetMacAdd) {
        // get a list of mac address where the signal strength pass the FLAG value
        HashMap<String, Double> filteredMac = new HashMap<>();
        for (int i = 0; i < targetData.size(); i++) {
            double strength = targetData.get(i);
            if (Math.abs(strength) < Math.abs(FLAG)) {
                filteredMac.put(targetMacAdd.get(i), targetData.get(i));
            }
        }

        return filteredMac;
    }

    // OLD CODE THAT IS INCORRECT
    /*
    public ArrayList<String> filterWifiByFlag(ArrayList<Double> targetData, double FLAG, ArrayList<String> targetMacAdd) {
        // get a list of mac address where the signal strength pass the FLAG value
        ArrayList<String> filteredMac = new ArrayList<>();
        for (int i = 0; i < targetData.size(); i++) {
            double strength = targetData.get(i);
            if (Math.abs(strength) > Maths.abs(FLAG)) {
                filteredMac.add(targetMacAdd.get(i));
            }
        }

        return filteredMac;
    }
     */

    public double checkPercentageMatch(String pointID, HashMap<String, Double> filteredMac, HashMap<String, ArrayList<String>> pointsFB, HashMap<String, String> signalBSSIDFB) {
        // cover for the edge case where filteredMac is empty
        if (filteredMac.size() == 0) {
            return 0.0;
        }

        int count = 0;

        // ArrayList<String> listOfBSSID = new ArrayList<>();
        HashMap<String, Double> listOfSignalStrengths = new HashMap<>();

        // for each signal, retrieve the corresponding bssid
        for (String signalID: pointsFB.get(pointID)) {
            listOfSignalStrengths.put(signalBSSIDFB.get(signalID), signalStrengthFB.get(signalID));
        }

        // compare the bssid between fingerprint and target location
        for (String bssid : filteredMac.keySet()) {
            if (listOfSignalStrengths.get(bssid) != null) {
                double signalStrengthTarget = filteredMac.get(bssid);
                double signalStrength = listOfSignalStrengths.get(bssid);
                double difference = Math.abs(signalStrengthTarget - signalStrength);
                if (difference < proximityThreshold) {
                    count++;
                }
            }
        }

        return (double) count / filteredMac.size();
    }

    private void storeFingerprint(String pointID) {
        // get coordinates of this fingerprint
        Coordinate coordinates = pointsCoordinatesFB.get(pointID);
        fingerprintCoordinate.add(coordinates);

        // convert coordinates to string to store as key values for the hashmaps
        String coordinatesStr = stringifyCoordinates(coordinates);

        // create a hashmap for each mac address and corresponding signal strength at this fingerprint
        HashMap<String, Double> avgSignalFingerprint = new HashMap<>();
        HashMap<String, Double> stdDevSignalFingerprint = new HashMap<>();
        HashMap<String, Double> originalAvgSignalFingerprint = new HashMap<>();

        for (String signalID: pointsFB.get(pointID)) {
            originalAvgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthOriginalFB.get(signalID));
            avgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthFB.get(signalID));
            stdDevSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthSDFB.get(signalID));
        }

        fingerprintOriginalAvgSignal.put(coordinatesStr, originalAvgSignalFingerprint);
        fingerprintAvgSignal.put(coordinatesStr, avgSignalFingerprint);
        fingerprintStdDevSignal.put(coordinatesStr, stdDevSignalFingerprint);
    }

    /*
    public void preMatching(ArrayList<Double> targetData, ArrayList<String> targetMacAdd) {
        // obtain FLAG value to filter out weak wifi signals
        final double FLAG = calculateFlag(targetData);

        // get a list of mac address where the signal strength pass the FLAG value
        ArrayList<String> filteredMac = filterWifiByFlag(targetData, FLAG, targetMacAdd);

        // compare bssid in each fingerprint with the list of bssid from wifi scan at target location
        for (String pointID: pointsFB.keySet()) {
            // calculating the percentage match
            double percentMatch = checkPercentageMatch(pointID, filteredMac, pointsFB, signalBSSIDFB);
            // System.out.println(pointID + ": " + percentMatch);
            if (percentMatch > threshold) {
                storeFingerprint(pointID);
            }
        }
    }
     */

    public void preMatchingK (ArrayList<Double> targetData, ArrayList<String> targetMacAdd) {

        // obtain FLAG value to filter out weak wifi signals
        final double FLAG = calculateFlag(targetData);;

        // get a list of mac address where the signal strength pass the FLAG value
        HashMap<String, Double> filteredMac = filterWifiByFlag(targetData, FLAG, targetMacAdd);

        // Hashmap to store the percentMatch to each fingerprint
        HashMap<String, Double> fingerprintsMatch = new HashMap<>();
        ArrayList<Double> matches = new ArrayList<>();

        // compare bssid in each fingerprint with the list of bssid from wifi scan at target location
        for (String pointID: pointsFB.keySet()) {
            System.out.println("pointID: " + pointID);
            double percentMatch = checkPercentageMatch(pointID, filteredMac, pointsFB, signalBSSIDFB);
            // filters out fingerprints that do not even contain any of the bssid at the target location
            System.out.println("percentMatch: " + percentMatch);
            if (percentMatch > 0.0) {
                fingerprintsMatch.put(pointID, percentMatch);
                matches.add(percentMatch);
            }
        }

        // sort the matches list in descending order to find top k fingerprints
        Collections.sort(matches);
        Collections.reverse(matches);

        System.out.println("matches: " + matches);

        if (matches.size() > 0) {
            int limit = 0;
            if (matches.size() < k) {
                limit = matches.size();
            } else {
                limit = k;
            }
            for (int i = 0; i < limit; i++) {
                // retrieve the pointID of the fingerprint
                String fingerprintID = "";
                for (Map.Entry<String, Double> fingerprint: fingerprintsMatch.entrySet()) {
                    if (fingerprint.getValue().equals(matches.get(i))) {
                        // get the key of this particular value which is the ID of the fingerprint
                        fingerprintID = fingerprint.getKey();
                        System.out.println("Top K fingerprints: " + fingerprintID + ", " + matches.get(i));
                        break;
                    }
                }

                // store the fingerprint into the data variables
                storeFingerprint(fingerprintID);

                // remove fingerprint once it is added to the filtered list of fingerprints
                fingerprintsMatch.remove(fingerprintID);
            }
        }
        else {
            // if all fingerprints don't match i.e. user is out of the location selected, this will be set to true
            filteredFailed = true;
        }

        System.out.println("signal strengths: " + fingerprintAvgSignal);
        System.out.println("standard deviations: " + fingerprintStdDevSignal);
    }

    public Coordinate euclideanDistance(ArrayList<Double> targetData, ArrayList<Double> targetStdDev, ArrayList<String> targetMacAdd) {
        ArrayList<String> coordinateKey = new ArrayList<>();
        //retrieve the keys of the fingerprint
        for (String coorStr : fingerprintAvgSignal.keySet()){
            coordinateKey.add(coorStr);
        }

        // System.out.println(coordinateKey);

        //the number of coordinates
        for (int i = 1; i <= fingerprintCoordinate.size() ; i++) {
            //euclidean distance
            double euclideanDis = 0;

            HashMap<String, Double> subFingerprintAvgSignal = fingerprintAvgSignal.get(coordinateKey.get(i-1));
            HashMap<String, Double> subFingerprintStdDevSignal = fingerprintStdDevSignal.get(coordinateKey.get(i-1));

            //the number of the values of mac addresses
            for (int k = 1; k < targetData.size() + 1; k++) {
                //PAVG, DEV of k-th wifi signals at the target place
                Double pavgTarget = targetData.get(k - 1);
                Double devTarget = targetStdDev.get(k - 1);

                if (subFingerprintAvgSignal.containsKey(targetMacAdd.get(k-1))){
                    //PAVG, DEV of k-th wifi signals at the i-th fingerprint
                    Double pavgFingerprint = subFingerprintAvgSignal.get(targetMacAdd.get(k - 1));
                    Double devFingerprint = subFingerprintStdDevSignal.get(targetMacAdd.get(k - 1));
                    // System.out.println("devFingerprint : "+ devFingerprint);

                    //sum it
                    euclideanDis += subDEuclideanDis(pavgTarget, devTarget, pavgFingerprint, devFingerprint);
                }
            }
            euclideanDis = Math.sqrt(euclideanDis);
            euclideanArray.add(euclideanDis);
        }

        Coordinate position = calculateEuclideanCoordinate(euclideanArray);
        euclideanArray.clear();

        return position;
    }


    /*

     */
    public double subDEuclideanDis(Double pavgTarget, Double devTarget, Double pavgFingerprint, Double devFingerprint){
        //find the absolute value of pavg
        double absPavg = Math.abs(pavgTarget - pavgFingerprint);
        double output = Math.pow(absPavg + devTarget + devFingerprint, 2);
        return output;
    }

    public Coordinate calculateEuclideanCoordinate(ArrayList<Double> euclideanArray){
        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;

        //calculate the coordinate
        for (int j = 1; j <= fingerprintCoordinate.size(); j++) {

            //find x and y multiplied by omega and sum all
            numeratorX += calculateXEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            numeratorY += calculateYEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            //omega
            denominatorPart += (1 / euclideanArray.get(j - 1));
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;
        return new Coordinate(x, y);
    }



    // wifi scan data from target location
    ;
    public Coordinate jointProbability(ArrayList<Double> targetDataOriginal, ArrayList<String> targetMacAdd) {
        ArrayList<String> coordinateKey = new ArrayList<>();
        //retrieve the keys of the fingerprint
        for (String coorStr : fingerprintAvgSignal.keySet()){
            coordinateKey.add(coorStr);
        }

        for (int i = 1; i <fingerprintCoordinate.size()+1 ; i++) {

            double Pik = 1;
            double jointProbi = 1;

            HashMap<String, Double> subFingerprintOriginalAvgSignalJP = fingerprintOriginalAvgSignal.get(coordinateKey.get(i - 1));
            HashMap<String, Double> subFingerprintStdDevSignalJP = fingerprintStdDevSignal.get(coordinateKey.get(i - 1));
            // System.out.println("Std Dev signal JP : " +subFingerprintStdDevSignalJP);

            // System.out.println(fingerprintOriginalAvgSignal);

            for (int k = 1; k < targetDataOriginal.size() + 1; k++) {
                //AVGk, DEV of k-th wifi signals at the target place
                //x value
                Double avgTarget = targetDataOriginal.get(k - 1);
                // System.out.println("avgTarget: " + avgTarget);
                // System.out.println(subFingerprintOriginalAvgSignalJP.containsKey(targetMacAdd.get(k - 1)));

                if (subFingerprintOriginalAvgSignalJP.containsKey(targetMacAdd.get(k - 1))){
                    //mu and sigma value
                    Double avgFingerprint = subFingerprintOriginalAvgSignalJP.get(targetMacAdd.get(k - 1));
                    Double devFingerprint = subFingerprintStdDevSignalJP.get(targetMacAdd.get(k - 1));
                    // System.out.println("StdDEV : "+devFingerprint);

                    //calculate Pik
                    Pik = calculateJointProb(avgTarget, avgFingerprint, devFingerprint);

                    //Pi = Pi1 * Pi2 * Pi3 * ... *Pik
                    if (Pik != 0){
                        jointProbi = jointProbi * Pik;
                    }

                    // System.out.println("avgFingerprint: " + avgFingerprint + ", devFingerprint: " + devFingerprint + ", Pik: " + Pik + ", Joint Prob: " + jointProbi);
                }
                if (jointProbi == 0){
                    jointProbi = 1;
                }
            }
            jointProbArray.add(jointProbi);
        }

        // System.out.println("Joint Probability Array: " + jointProbArray.toString());
        Coordinate position = calculateJointProbCoordinate(jointProbArray);
        //clear the array to be reusable
        jointProbArray.clear();
        return position;
    }

    public Coordinate calculateJointProbCoordinate(ArrayList<Double> jointProbArray){
        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;
        //calculate the coordinate
        for (int j = 1; j <= fingerprintCoordinate.size(); j++) {
            if (jointProbArray.get(j-1) != 0.0) {
                //find x and y multiplied by omega and sum all
                numeratorX += calculateXJointProb(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
                numeratorY += calculateYJointProb(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
                //omega
                denominatorPart += omegaJointProb(jointProbArray.get(j-1));
                // System.out.println("numeratorX: " + numeratorX + ", numeratorY: " + numeratorY + "denominatorPart: " + denominatorPart);
            }
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;
        return new Coordinate(x, y);
    }

    public Coordinate weightedFusion(Coordinate euclidDistPosition, Coordinate jointProbPosition) {
        // Calculate the final X and Y
        double finalX = weightEuclidDist * euclidDistPosition.getX() + weightJointProb * jointProbPosition.getX();
        double finalY = weightEuclidDist * euclidDistPosition.getY() + weightJointProb * jointProbPosition.getY();
        System.out.println("Euclidean x: " + euclidDistPosition.getX() + ", Euclidean y: " + euclidDistPosition.getY());
        System.out.println("Joint Prob x: " + jointProbPosition.getX() + ", Joint Prob y: " + jointProbPosition.getY());
        System.out.println("Final coordinates: " + finalX + ", " + finalY);

        // return the calculated X and Y values
        return new Coordinate(finalX,finalY);
    }

    //helper method to calculate joint probability
    public double calculateJointProb(Double x, Double mu, Double sigma){

        if (sigma == 0.0) {
            sigma = 1.0;
        }
        double p1 = (sigma * Math.sqrt(2* Math.PI));
        double numerator = Math.pow(x-mu, 2);
        double denominator = 2* Math.pow(sigma, 2);
        double p2 = Math.exp(-numerator/denominator);
        // System.out.println("p1: "+ p1 + ", numerator: " + numerator + ", denominator: " + denominator + ", p2: " + p2 + ", p2/p1: " + p2/p1);
        return p2 / p1;
    }

    //helper method to calculate numerator of coordinate
    public double calculateXEuclidean(double distance, Coordinate coordinate){
        //omega value
        double omega = 1 / distance;
        //x
        double x = coordinate.getX();
        x = omega * x;
        return x;
    }

    //helper method to calculate numerator of coordinate
    public double calculateYEuclidean(double distance, Coordinate coordinate){
        //omega value
        double omega = 1 / distance;
        //y
        double y = coordinate.getY();
        y = omega *y;

        return y;
    }

    //helper method to calculate numerator of coordinate
    public double calculateXJointProb(double probability, Coordinate coordinate){
        //omega value
        double omega = omegaJointProb(probability);
        //x
        double x = coordinate.getX();
        x = omega * x;
        return x;
    }

    //helper method to calculate numerator of coordinate
    public double calculateYJointProb(double probability, Coordinate coordinate){
        //omega value
        double omega = omegaJointProb(probability);
        //y
        double y = coordinate.getY();
        y = omega *y;

        return y;
    }

    public double omegaJointProb(double probability){
        //no standard deviation means their matches are exact
        //hence, we don't need to add any coordinate value to the existing coordinate
        //Math.log10(1) = 0
        if (probability == 0){
            return Math.log10(1);
        }
        return Math.log10(probability);
    }

}