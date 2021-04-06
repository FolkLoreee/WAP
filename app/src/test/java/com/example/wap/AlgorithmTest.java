package com.example.wap;

import com.example.wap.models.Coordinate;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;

public class AlgorithmTest{
    Algorithm algo;
    private static final double DELTA = 1e-15;

    @Before
    public void runBeforeEachTest()
    {
        System.out.println("setting up");
        /*
        *inputs
        * * fingerprintOriginalAvgSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, originalAverageWifiSignal>>
         * fingerprintAvgSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, averageWifiSignal>>
         * fingerprintStdDevSignal = HashMap<fingerprintCoordinate, HashMap<macAddress, standardDeviationSignal>>
         * fingerprintCoordinate = list of filtered fingerprints
         *
         * targetMacAdd = list of mac address received at target location
         * targetStdDev = list of wifi signal standard deviation values for each mac address
         * targetData = list of average wifi signal values for each mac address
         * targetDataOriginal = list of original average wifi signal values for each mac address (FOR NOW, THESE HAVE THE SAME VALUES AS THE PROCESSED ONES)
         */
        //data for fingerprintOriginalAvgSignal
        /*
        HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal =
        HashMap<String, HashMap<String, Double>> fingerprintAvgSignal
        HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal = ;
        ArrayList<Coordinate> fingerprintCoordinate = new ArrayList<>();
        Coordinate X = new Coordinate(10, 10);
        Coordinate Y = new Coordinate(20, 20);
        Coordinate Z = new Coordinate(25, 220);
        fingerprintCoordinate.add(X);
        fingerprintCoordinate.add(Y);
        fingerprintCoordinate.add(Z);

        // Algorithm algo = new Algorithm()

         */

        ArrayList<Coordinate> fingerprintCoordinate = new ArrayList<>();
        fingerprintCoordinate.add(new Coordinate(1,1));
        fingerprintCoordinate.add(new Coordinate(2, 5));
        fingerprintCoordinate.add(new Coordinate(3,4));
        fingerprintCoordinate.add(new Coordinate(9,1));
        fingerprintCoordinate.add(new Coordinate(6, 7));
        fingerprintCoordinate.add(new Coordinate(3,8));

       //HashMap<macAddress, originalAverageWifiSignal>, data necesscary for fingerprintOriginalAvgSignal
       HashMap<String, Double> subfingerprintOriginalAvgSignalCoor01 = new HashMap<>();
       subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-1", 75.0);
       subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-3", 15.5);
       subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-7", 33.7);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-10", 55.0);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-001", 66.5);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-0", 39.15);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-17", 25.0);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-5", 52.5);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-722", 13.7);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor02 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-1", 75.0);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-33", 15.5);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-7", 33.7);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-10", 55.0);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-001", 66.5);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-0", 39.15);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-17", 25.0);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-50", 52.5);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-722", 13.7);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor03 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-2", 95.0);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-3", -100.0);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-1", -33.7);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-4", 85.0);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-88", 66.5);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-0", 39.15);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-17", 25.0);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-5", 52.5);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-62", 13.7);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor04 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-345", 95.0);
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-90", -100.0);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor05 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-2", 65.0);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-3", -100.0);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-1", 133.7);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-4", -43.0);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-88", 66.5);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-003", 39.15);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor06 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor06.put("wifi-signal-202", -100.0);


        HashMap<Coordinate, HashMap<String, Double>> fingerprintOriginalAvgSignal = new HashMap<>();
        fingerprintOriginalAvgSignal.put(new Coordinate(1,1), subfingerprintOriginalAvgSignalCoor01);
        fingerprintOriginalAvgSignal.put(new Coordinate(2, 5), subfingerprintOriginalAvgSignalCoor02);
        fingerprintOriginalAvgSignal.put(new Coordinate(3, 4), subfingerprintOriginalAvgSignalCoor03);
        fingerprintOriginalAvgSignal.put(new Coordinate(9,1), subfingerprintOriginalAvgSignalCoor04);
        fingerprintOriginalAvgSignal.put(new Coordinate(6, 7), subfingerprintOriginalAvgSignalCoor05);
        fingerprintOriginalAvgSignal.put(new Coordinate(3, 8), subfingerprintOriginalAvgSignalCoor06);


        algo = new Algorithm();
    }
    /*
    *Test cases for calculating X,Y Coordinate from Joint Probability method
     */

    @Test
    public void omegaJointProbTest(){
        double output = algo.omegaJointProb(5);
        assertEquals(Math.log10(5), output, DELTA);
    }

    @Test
    public void calculateYJointProbTest(){
        double output = algo.calculateYJointProb(5.9, new Coordinate(7, 5.9));
        //for easy testing
        output = (double) Math.round(output * 100) / 100;
        assertEquals(4.55, output, DELTA);
    }

    @Test
    public void calculateXJointProbTest(){
        double output = algo.calculateXJointProb(5.9, new Coordinate(7, 5.9));
        //for easy testing
        output = (double) Math.round(output * 100) / 100;
        //5.395964081
        assertEquals(5.40, output, DELTA);
    }

    @Test
    public void calculateJointProbTest1(){
        double output = algo.calculateJointProb(2.5, 8.1, 3.0);
        //for easy testing, round off to 2 decimal places
        output = (double) Math.round(output * 100) / 100;
        assertEquals(0.02, output, DELTA);
    }

    @Test
    public void calculateJointProbTest2(){
        double output = algo.calculateJointProb(2.5, 8.1, 0.0);
        //for easy testing, round off to 2 decimal places
        output = (double) Math.round(output * 100) / 100;
        assertEquals(0.00, output, DELTA);
    }
    //haven't implemented test cases for no-argument functions yet
    @Test
    public void jointProbabilityTest1(){
        //Target Wifi signals with some negative values
        ArrayList<Double> targetDataOriginal = new ArrayList<>();
        targetDataOriginal.add(5.0);
        targetDataOriginal.add(-7.5);
        targetDataOriginal.add(40.3);
        targetDataOriginal.add(50.0);
        targetDataOriginal.add(-79.0);
        targetDataOriginal.add(63.4);

        ArrayList<String> targetMacAddress = new ArrayList<>();
        targetMacAddress.add("wifi-signal-1");
        targetMacAddress.add("wifi-signal-2");
        targetMacAddress.add("wifi-signal-3");
        targetMacAddress.add("wifi-signal-4");
        targetMacAddress.add("wifi-signal-5");
        targetMacAddress.add("wifi-signal-6");

        Coordinate output = algo.jointProbability(targetDataOriginal, targetMacAddress);

        //  TODO:need to change after writing the input values for fingerprint
        assertEquals(4.5, output.getX(), DELTA);
        assertEquals(4.5, output.getY(), DELTA);
    }

    @Test
    public void jointProbabilityTest2(){
        //Target Wifi signals with all positives
        ArrayList<Double> targetDataOriginal = new ArrayList<>();
        targetDataOriginal.add(5.0);
        targetDataOriginal.add(20.5);
        targetDataOriginal.add(40.3);
        targetDataOriginal.add(50.0);
        targetDataOriginal.add(79.0);
        targetDataOriginal.add(63.4);

        ArrayList<String> targetMacAddress = new ArrayList<>();
        targetMacAddress.add("wifi-signal-1");
        targetMacAddress.add("wifi-signal-2");
        targetMacAddress.add("wifi-signal-3");
        targetMacAddress.add("wifi-signal-4");
        targetMacAddress.add("wifi-signal-5");
        targetMacAddress.add("wifi-signal-6");

        Coordinate output = algo.jointProbability(targetDataOriginal, targetMacAddress);
        //  TODO:need to fix the expected values
        assertEquals(4.783444809515455, output.getX(), DELTA);
        assertEquals(4.380983164837119, output.getY(), DELTA);


    }

    //ALL NON-ZEROS JOINT PROB
    @Test
    public void calculateJointProbCoordinateTest1(){
        ArrayList<Double> jointProbArray = new ArrayList<>();
        jointProbArray.add(1.4);
        jointProbArray.add(2.2);
        jointProbArray.add(5.3);
        jointProbArray.add(3.5);
        jointProbArray.add(4.7);
        jointProbArray.add(1.5);

        Coordinate output = algo.calculateJointProbCoordinate(jointProbArray);
        assertEquals(4.783444809515455, output.getX(), DELTA);
        assertEquals(4.380983164837119, output.getY(), DELTA);

    }

    @Test
    public void calculateJointProbCoordinateTest2(){
        //some joint prob value = 0
        ArrayList<Double> jointProbArray = new ArrayList<>();
        jointProbArray.add(1.4);
        jointProbArray.add(0.0);
        jointProbArray.add(5.3);
        jointProbArray.add(3.5);
        jointProbArray.add(0.0);
        jointProbArray.add(1.5);

        Coordinate output = algo.calculateJointProbCoordinate(jointProbArray);
        assertEquals(4.8686162099791295, output.getX(), DELTA);
        assertEquals(3.1410443820133644, output.getY(), DELTA);
    }

    /*
    *Unit tests for Euclidean distance
     */

    @Test
    public void calculateEuclideanCoordinateTest(){
        ArrayList<Double> euclideanArray = new ArrayList<>();
        euclideanArray.add(5.5);
        euclideanArray.add(3.9);
        euclideanArray.add(8.8031);
        euclideanArray.add(4.132);
        euclideanArray.add(3.15);
        euclideanArray.add(1.001);

        Coordinate output = algo.calculateEuclideanCoordinate(euclideanArray);
        //  TODO: need to fix the expected values
        assertEquals(3.8455745350405257, output.getX(), DELTA);
        assertEquals(5.863858525140664, output.getY(), DELTA);
    }

    @Test
    public void euclideanDistanceTest(){
        ArrayList<Double> targetData = new ArrayList<>();
        targetData.add(5.2);
        targetData.add(20.3);
        targetData.add(40.5);
        targetData.add(51.5);
        targetData.add(79.4);
        targetData.add(63.9);

        ArrayList<Double> targetStdDev = new ArrayList<>();
        targetStdDev.add(3.5);
        targetStdDev.add(5.2);
        targetStdDev.add(4.3);
        targetStdDev.add(7.4);
        targetStdDev.add(6.2);
        targetStdDev.add(1.5);

        ArrayList<String> targetMacAddress = new ArrayList<>();
        targetMacAddress.add("wifi-signal-1");
        targetMacAddress.add("wifi-signal-2");
        targetMacAddress.add("wifi-signal-3");
        targetMacAddress.add("wifi-signal-4");
        targetMacAddress.add("wifi-signal-5");
        targetMacAddress.add("wifi-signal-6");

        Coordinate output = algo.euclideanDistance(targetData,  targetStdDev,  targetMacAddress);
        //  TODO: need to fix the expected values
        assertEquals(3.8455745350405257, output.getX(), DELTA);
        assertEquals(5.863858525140664, output.getY(), DELTA);

    }

    @Test
    public void subDEuclideanDisTest(){
        double output = algo.subDEuclideanDis(4.5, 2.2, 3.3, 1.3);
        //for easy testing, round off to 2 decimal places
        output = (double) Math.round(output * 100) / 100;
        assertEquals(22.09, output, DELTA);
    }

    @Test
    public void calculateYEuclideanTest(){
        double output = algo.calculateYEuclidean(9.5, new Coordinate(7, 5.9));
        //for easy testing, round off to 2 decimal places
        output = (double) Math.round(output * 100) / 100;
        assertEquals(0.62, output, DELTA);
    }

    @Test
    public void calculateXEuclideanTest(){
        double output = algo.calculateXEuclidean(9.5, new Coordinate(7, 5.9));
        //for easy testing, round off to 2 decimal places
        output = (double) Math.round(output * 100) / 100;
        assertEquals(0.74, output, DELTA);

    }
    @Test
    public void weightedFusionTest(){
        Coordinate output = algo.weightedFusion(new Coordinate(415.667, 31.2), new Coordinate(433.524, 34.12));
        assertEquals(424.5955, output.getX(), DELTA);
        assertEquals(32.66, output.getY(), DELTA);
    }



}