package com.example.wap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.wap.models.Coordinate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TestingActivity extends AppCompatActivity {

    //retrieve from the database
    ArrayList<Integer> fingerprintData;
    ArrayList<Integer> targetData;
    ArrayList<Coordinate> fingerprintCoordinate;
    //for average, process-average, standard deviation calculations
    ArrayList<Integer> fingerprintDataIK;
    ArrayList<Integer> targetDataK;
    //create an arraylist to store the euclidean distance di values
    ArrayList<Double> euclideanArray;
    //create an arraylist to store the jointprob i values
    ArrayList<Double> jointProbArray;
    //firebase
    FirebaseFirestore db;
    String TAG = "i";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        // TODO: Retrieve data from Firebase

        //real-time for target data

        //firestore for fingerprint
        db = FirebaseFirestore.getInstance();
        //if the user is in level 1 (Have to add if-else condition)
        db.collection("signals")
                .whereEqualTo("locationID", "CampusCenter1")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot docu : task.getResult()) {
                                Log.d(TAG, docu.getId() + " => " + docu.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        // TODO: Pre-matching of fingerprints?

    }

    private Coordinate euclideanDistance() {
        // TODO: Euclidean distance positioning algorithm (Hannah)
        //Coordinate position = new Coordinate(0,0);
        
        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;

        //euclidean distance
        double euclideanDis = 0;

        for (int i = 1; i <fingerprintData.size()+1 ; i++){
            for (int k = 1; k < targetData.size()+1; k++ ){
                //Deep copy of the targetData and FingerprintData until k-th
                for (int j = 0; j < k; j++){
                    targetDataK.set(j, targetData.get(j));
                    if (j < i){
                        fingerprintDataIK.set(j, fingerprintDataIK.get(j));
                    }

                }
                //PAVG, DEV of k-th wifi signals at the target place
                Integer avgTarget = calculateAverage(targetDataK);
                Integer pavgTarget = calculateProcessedAverage(avgTarget);
                Integer devTarget = calculateStandardDeviation(targetDataK, avgTarget);

                //PAVG, DEV of k-th wifi signals at the i-th fingerprint
                Integer avgFingerprint = calculateAverage(fingerprintDataIK);
                Integer pavgFingerprint = calculateProcessedAverage(avgFingerprint);
                Integer devFingerprint = calculateStandardDeviation(fingerprintDataIK, avgFingerprint);
                //find the absolute value of pavg
                Integer absPavg = Math.abs(pavgTarget - pavgFingerprint);
                double sqauredValue = Math.pow(absPavg + devTarget + devFingerprint, 2);
                //sum it
                euclideanDis += sqauredValue ;
            }
            euclideanDis = Math.sqrt(euclideanDis);
            euclideanArray.set(i-1, euclideanDis);
        }

        //calculate the coordinate
        for (int j = 1; j < fingerprintCoordinate.size()+1 ; j++) {
            //find x and y multiplied by omega and sum all
            numeratorX += calculateXEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            numeratorY += calculateYEuclidean(euclideanArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            //omega
            denominatorPart += (1 / euclideanArray.get(j - 1));
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;
        //clear the array to be reusable
        euclideanArray.clear();
        return new Coordinate(x, y);
    }

    private Coordinate jointProbability() {
        // TODO: joint probability positioning algorithm (Hannah)
        Coordinate position = new Coordinate(0,0);
        double Pik = 1;
        double jointProbi = 1;

        double numeratorX = 0;
        double numeratorY = 0;
        double denominatorPart = 0;

        for (int i = 1; i <fingerprintData.size()+1 ; i++){
            for (int k = 1; k < targetData.size()+1; k++ ){
                //Deep copy of the targetData and FingerprintData until k-th
                for (int j = 0; j < k; j++){
                    targetDataK.set(j, targetData.get(j));
                    if (j < i){
                        fingerprintDataIK.set(j, fingerprintDataIK.get(j));
                    }

                }
                //AVGk, DEV of k-th wifi signals at the target place
                //x value
                Integer avgTarget = calculateAverage(targetDataK);
                //mu value
                Integer avgFingerprint = calculateAverage(fingerprintDataIK);
                //sigma
                Integer devFingerprint = calculateStandardDeviation(fingerprintDataIK, avgFingerprint);

                //calculate Pik
                Pik = calculateJointProb(avgTarget, avgFingerprint, devFingerprint);
                //Pi = Pi1 * Pi2 * Pi3 * ... *Pik
                jointProbi = jointProbi * Pik;
            }
            jointProbArray.set(i-1, jointProbi);
        }
        //calculate the coordinate
        for (int j = 1; j < fingerprintCoordinate.size()+1 ; j++) {
            //find x and y multiplied by omega and sum all
            numeratorX += calculateXJointProb(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            numeratorY += calculateYEuclidean(jointProbArray.get(j - 1), fingerprintCoordinate.get(j - 1));
            //omega
            denominatorPart += omegaJointProb(jointProbArray.get(j-1));
        }
        double x = numeratorX / denominatorPart;
        double y = numeratorY / denominatorPart;
        //clear the array to be reusable
        jointProbArray.clear();
        return new Coordinate(x, y);

    }

    private Coordinate cosineSimilarity() {
        // TODO: cosine similarity positioning algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    private Coordinate weightedFusion() {
        // TODO: add different weights to each algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }
    //helper method to calculate joint probability
    private double calculateJointProb(Integer x, Integer mu, Integer sigma){
        double p1 = 1 / (sigma * Math.sqrt(2* Math.PI));
        double numerator = Math.pow(x- mu, 2);
        double denominator = 2* Math.pow(sigma, 2);
        double p2 = Math.exp(-numerator/denominator);

        return p1*p2;
    }

    //helper method to calculate numerator of coordinate
    private double calculateXEuclidean(double distance, Coordinate coordinate){
        //omega value
        double omega = 1 / distance;
        //x
        double x = coordinate.getX();
        x = omega * x;
        return x;
    }

    //helper method to calculate numerator of coordinate
    private double calculateYEuclidean(double distance, Coordinate coordinate){
        //omega value
        double omega = 1 / distance;
        //y
        double y = coordinate.getY();
        y = omega *y;

        return y;
    }

    //helper method to calculate numerator of coordinate
    private double calculateXJointProb(double probability, Coordinate coordinate){
        //omega value
        double omega = omegaJointProb(probability);
        //x
        double x = coordinate.getX();
        x = omega * x;
        return x;
    }

    //helper method to calculate numerator of coordinate
    private double calculateYJointProb(double probability, Coordinate coordinate){
        //omega value
        double omega = omegaJointProb(probability);
        //y
        double y = coordinate.getY();
        y = omega *y;

        return y;
    }

    private double omegaJointProb(double probability){
        return Math.log(probability);
    }

    private Integer calculateAverage (ArrayList<Integer> readings) {
        Integer sum = 0;
        for (Integer reading: readings) {
            sum += reading;
        }
        Integer average = sum / readings.size();
        return average;
    }

    private Integer calculateStandardDeviation(ArrayList<Integer> readings, int average) {
        Integer sum = 0;
        for (Integer reading: readings) {
            sum += (reading - average);
        }
        sum /= readings.size();
        double sd = Math.sqrt((double) sum);
        return (int) sd;
    }

    // error handling on the original average wifi signal
    private Integer calculateProcessedAverage (Integer average) {
        int offset = 0;
        // systematic error
        int result = average + offset;
        // gross error

        // random error
        return result;
    }

}