package com.example.wap;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


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
    //coordinateKey
    ArrayList<String> coordinateKey;

    // data from firebase
    HashMap<String, ArrayList<String>> pointsFB;
    HashMap<String, Coordinate> pointsCoordinatesFB;
    HashMap<String, Double> signalStrengthFB;
    HashMap<String, Double> signalStrengthOriginalFB;
    HashMap<String, String> signalBSSIDFB;
    HashMap<String, Double> signalStrengthSDFB;

    // constants
    private final double weightEuclidDist = 0.5;
    private final double weightJointProb = 0.5;
    private final int k = 4;

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
        });

        wapFirebaseSignal.compoundQuery("locationID", locationID).addOnSuccessListener(new OnSuccessListener<ArrayList<Signal>>() {
            @Override
            public void onSuccess(ArrayList<Signal> signals) {
                for (Signal signal: signals) {
                    String signalID = signal.getSignalID();
                    String bssid = signal.getWifiBSSID();
                    double signalStrengthSD = signal.getSignalStrengthSD();
                    double signalStrength = signal.getSignalStrength();
                    signalStrengthFB.put(signalID, signalStrength);
                    signalStrengthOriginalFB.put(signalID, signalStrength);
                    signalStrengthSDFB.put(signalID, signalStrengthSD);
                    signalBSSIDFB.put(signalID, bssid);
                }
            }
        });
    }

    public void preMatching(ArrayList<Double> targetData, ArrayList<String> targetMacAdd) {

        double threshold = 0.8;

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

            // calculating the percentage match
            double percentMatch = (double) count / filteredMac.size();
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

    public void preMatching2 (ArrayList<Double> targetData, ArrayList<String> targetMacAdd) {

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

        // Hashmap to store the percentMatch to each fingerprint
        HashMap<String, Double> fingerprintsMatch = new HashMap<>();
        ArrayList<Double> matches = new ArrayList<>();

        // compare bssid in each fingerprint with the list of bssid from wifi scan at target location
        for (String pointID: pointsFB.keySet()) {
            int count = 0;
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

            // calculating the percentage match
            double percentMatch = (double) count / filteredMac.size();
            fingerprintsMatch.put(pointID, percentMatch);
            matches.add(percentMatch);
        }

        // sort the matches list in descending order to find top 4 fingerprints
        Collections.sort(matches);
        Collections.reverse(matches);

        for (int i = 0; i < 6; i++) {
            String fingerprintID = "";
            for (Map.Entry<String, Double> fingerprint: fingerprintsMatch.entrySet()) {
                if (fingerprint.getValue().equals(matches.get(i))) {
                    // get the key of this particular value which is the ID of the fingerprint
                    fingerprintID = fingerprint.getKey();
                }
            }

            // get coordinates of this fingerprint
            Coordinate coordinates = pointsCoordinatesFB.get(fingerprintID);
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

            for (String signalID: pointsFB.get(fingerprintID)) {
                originalAvgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthOriginalFB.get(signalID));
                avgSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthFB.get(signalID));
                stdDevSignalFingerprint.put(signalBSSIDFB.get(signalID), signalStrengthSDFB.get(signalID));
            }

            fingerprintOriginalAvgSignal.put(coordinatesStr, originalAvgSignalFingerprint);
            fingerprintAvgSignal.put(coordinatesStr, avgSignalFingerprint);
            fingerprintStdDevSignal.put(coordinatesStr, stdDevSignalFingerprint);

            fingerprintsMatch.remove(fingerprintID);
        }
    }
    
    public Coordinate euclideanDistance(ArrayList<Double> targetData, ArrayList<Double> targetStdDev, ArrayList<String> targetMacAdd) {

        //retrieve the keys of the fingerprint
        for (String coorStr : fingerprintAvgSignal.keySet()){
            coordinateKey.add(coorStr);
        }

        System.out.println(coordinateKey);

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
                    System.out.println("devFingerprint : "+ devFingerprint);

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

        //retrieve the keys of the fingerprint
        for (String coorStr : fingerprintAvgSignal.keySet()){
            coordinateKey.add(coorStr);
        }

        for (int i = 1; i <fingerprintCoordinate.size()+1 ; i++) {

            double Pik = 1;
            double jointProbi = 1;

            HashMap<String, Double> subFingerprintOriginalAvgSignalJP = fingerprintOriginalAvgSignal.get(coordinateKey.get(i - 1));
            HashMap<String, Double> subFingerprintStdDevSignalJP = fingerprintStdDevSignal.get(coordinateKey.get(i - 1));
            System.out.println("Std Dev signal JP : " +subFingerprintStdDevSignalJP);

            //System.out.println(fingerprintOriginalAvgSignal);



            for (int k = 1; k < targetDataOriginal.size() + 1; k++) {
                //AVGk, DEV of k-th wifi signals at the target place
                //x value
                Double avgTarget = targetDataOriginal.get(k - 1);
                System.out.println("avgTarget: " + avgTarget);
                System.out.println(subFingerprintOriginalAvgSignalJP.containsKey(targetMacAdd.get(k - 1)));

                if (subFingerprintOriginalAvgSignalJP.containsKey(targetMacAdd.get(k - 1))){
                    //mu and sigma value
                    Double avgFingerprint = subFingerprintOriginalAvgSignalJP.get(targetMacAdd.get(k - 1));
                    Double devFingerprint = subFingerprintStdDevSignalJP.get(targetMacAdd.get(k - 1));
                    System.out.println("StdDEV : "+devFingerprint);

                    //calculate Pik
                    Pik = calculateJointProb(avgTarget, avgFingerprint, devFingerprint);

                    //Pi = Pi1 * Pi2 * Pi3 * ... *Pik
                    if (Pik != 0){
                        jointProbi = jointProbi * Pik;
                    }

                    System.out.println("avgFingerprint: " + avgFingerprint + ", devFingerprint: " + devFingerprint + ", Pik: " + Pik + ", Joint Prob: " + jointProbi);
                }
                if (jointProbi == 0){
                    jointProbi = 1;
                }
            }
            jointProbArray.add(jointProbi);
        }

        System.out.println("Joint Probability Array: " + jointProbArray.toString());
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
                System.out.println("numeratorX: " + numeratorX + ", numeratorY: " + numeratorY + "denominatorPart: " + denominatorPart);
            }
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;
        return new Coordinate(x, y);
    }

    public Coordinate weightedFusion(Coordinate euclidDistPosition, Coordinate jointProbPosition) {
        // Coordinate cosineSimPosition

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
        System.out.println("p1: "+ p1 + ", numerator: " + numerator + ", denominator: " + denominator + ", p2: " + p2 + ", p2/p1: " + p2/p1);
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