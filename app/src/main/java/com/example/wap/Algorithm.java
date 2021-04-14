package com.example.wap;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.MapPoint;
import com.example.wap.models.Signal;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.example.wap.MapViewActivity.locationID;


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

    // wifi scan data from target location
    ArrayList<Double> targetDataOriginal;
    ArrayList<Double> targetData;
    ArrayList<Double> targetStdDev;
    ArrayList<String> targetMacAdd;

    // weights
    private final double weightEuclidDist = 0.5;
    private final double weightJointProb = 0.5;
    private final double weightCosineSim = 1/3;

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

    }


    public Algorithm(HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal, HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal1, HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal, ArrayList<Coordinate> fingerprintCoordinate) {
        this.fingerprintData = new ArrayList<>();
        this.fingerprintCoordinate = fingerprintCoordinate;

        this.fingerprintOriginalAvgSignal = fingerprintOriginalAvgSignal;
        this.fingerprintAvgSignal = fingerprintOriginalAvgSignal;
        this.fingerprintStdDevSignal = fingerprintStdDevSignal;


//        this.targetData = new ArrayList<>();
//        this.targetMacAdd = new ArrayList<>();
//        this.targetDataOriginal = new ArrayList<>();
//        this.targetStdDev = new ArrayList<>();
        this.coordinateKey = new ArrayList<>();
        this.euclideanArray = new ArrayList<>();
        this.jointProbArray = new ArrayList<>();
    }

    public Coordinate euclideanDistance(ArrayList<Double> targetData, ArrayList<Double> targetStdDev, ArrayList<String> targetMacAdd) {

        //retrieve the keys of the fingerprint
        for (String coorStr : fingerprintAvgSignal.keySet()){
            coordinateKey.add(coorStr);
        }

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

    public Coordinate jointProbabilityVer2(ArrayList<Double> targetData, ArrayList<Double> targetStdDev, ArrayList<String> targetMacAdd){
        //step 1: calculate the distance between the unknown point and each of the n fingerprint
        Coordinate euclideanDis = euclideanDistance(targetData, targetStdDev, targetMacAdd);

        //Data pre-processing for step 2: fingerprint prematching
        ArrayList<Coordinate> fingerprintCoorArray = prematchingJointProb(euclideanDis);
        double mi = mCalculate(fingerprintCoorArray.get(0), euclideanDis);
        double mj = mCalculate(fingerprintCoorArray.get(1), euclideanDis);

        //requires actual distance
        //for now, will put p as 0.5
        double x_error = xError(mi, mj, 0.5);
        //alpha, beta1 and beta2 requires actual distance which we don't have
        double y_error = 0;




        return new Coordinate(0 ,0);
    }

    public double mCalculate(Coordinate fingerprintCoor, Coordinate euclideanDis){
        double xTemp = Math.pow(fingerprintCoor.getX() - euclideanDis.getX(), 2);
        double yTemp = Math.pow(fingerprintCoor.getY() - euclideanDis.getY(), 2);
        double m = Math.sqrt(xTemp+yTemp);
        return m;
    }

    public double yError(){
        return 0;
    }

    public double alpha(double di, double dj){
        double diSqr = Math.pow(di, 2);
        double diFor = Math.pow(di, 4);
        double djSqr = Math.pow(dj, 2);
        double djFor = Math.pow(dj, 4);
        return -16 + 8 * djSqr + 8 * diSqr - djFor - diFor + 2* diSqr * djSqr;

    }

    public double xError(double mi, double mj, double p){
        double x_error = xErrorSub(mi, p) - xErrorSub(mj, p);
        x_error = x_error * 1/2;
        return x_error;
    }
    public double xErrorSub(double m, double p){
        double numerator = m * m * p;
        double denominator = 1 + p;
        return numerator / denominator ;
    }

    public ArrayList<Coordinate> prematchingJointProb(Coordinate euclideanDis){
        ArrayList<Coordinate> output = new ArrayList<>();
        //create a copy of fingerprint
        for (int j = 0; j < fingerprintCoordinate.size(); j++){
            output.add(fingerprintCoordinate.get(j));
        }
        double minX = euclideanDis.getX();
        double maxX = euclideanDis.getX();

        int minXindex = 0;
        int maxXindex = 0;
        while (output.size() > 2){
            for (int i = 0; i < fingerprintCoordinate.size(); i++){
                Coordinate temporary = fingerprintCoordinate.get(i);
                if (temporary.getX() < minX){
                    minX = temporary.getX();
                    minXindex = i;

                }else if (temporary.getX() > maxX){
                    maxX = temporary.getX();
                    maxXindex = i;
                }

            }
            if (output.size() == 3){
                output.remove(minXindex);
            }else{
                output.remove(minXindex);
                output.remove(maxXindex);
            }

        }
        return output;

    }


    public Coordinate cosineSimilarity() {
        // TODO: cosine similarity positioning algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    public Coordinate weightedFusion(Coordinate euclidDistPosition, Coordinate jointProbPosition) {
        // Coordinate cosineSimPosition

        // Calculate the final X and Y
        // + weightCosineSim * cosineSimPosition.getX()
        // + weightCosineSim * cosineSimPosition.getY()
        double finalX = weightEuclidDist * euclidDistPosition.getX() + weightJointProb * jointProbPosition.getX();
        double finalY = weightEuclidDist * euclidDistPosition.getY() + weightJointProb * jointProbPosition.getY();
        System.out.println("euclidean x: " + euclidDistPosition.getX() + ", euclidean y: " + euclidDistPosition.getY());
        System.out.println("joint prob x: " + jointProbPosition.getX() + ", joint prob y: " + jointProbPosition.getY());
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