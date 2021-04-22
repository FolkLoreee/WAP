package com.example.wap;

import com.example.wap.models.Coordinate;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class AlgorithmTest{
    Algorithm algo;
    private static final double DELTA = 1e-15;

    @Before
    public void runBeforeEachTest()
    {
        System.out.println("setting up");

        ArrayList<Coordinate> fingerprintCoordinate = new ArrayList<>();
        fingerprintCoordinate.add(new Coordinate(1,1));
        fingerprintCoordinate.add(new Coordinate(2, 5));
        fingerprintCoordinate.add(new Coordinate(3,4));
        fingerprintCoordinate.add(new Coordinate(9, 1));
        fingerprintCoordinate.add(new Coordinate(6, 7));
        fingerprintCoordinate.add(new Coordinate(3, 8));

        HashMap<String, Double> subfingerprintStdDevSignalCoor01 = new HashMap<>();
        subfingerprintStdDevSignalCoor01.put("wifi-signal-1", 1.34569);
        subfingerprintStdDevSignalCoor01.put("wifi-signal-3", 2.1234);
        subfingerprintStdDevSignalCoor01.put("wifi-signal-2", 0.73890);
        subfingerprintStdDevSignalCoor01.put("wifi-signal-5", 2.0597);
        subfingerprintStdDevSignalCoor01.put("wifi-signal-6", 1.00987);


        HashMap<String, Double> subfingerprintStdDevSignalCoor02 = new HashMap<>();
        subfingerprintStdDevSignalCoor02.put("wifi-signal-1", 1.4567);
        subfingerprintStdDevSignalCoor02.put("wifi-signal-3", 1.00953);
        subfingerprintStdDevSignalCoor02.put("wifi-signal-2", 2.138);
        subfingerprintStdDevSignalCoor02.put("wifi-signal-4", 1.64380);
        subfingerprintStdDevSignalCoor02.put("wifi-signal-5", 0.0875);
        subfingerprintStdDevSignalCoor02.put("wifi-signal-6", 0.0354);

        HashMap<String, Double> subfingerprintStdDevSignalCoor03 = new HashMap<>();
        subfingerprintStdDevSignalCoor03.put("wifi-signal-2", 2.0583);
        subfingerprintStdDevSignalCoor03.put("wifi-signal-3", 0.0001);
        subfingerprintStdDevSignalCoor03.put("wifi-signal-1", 0.0000);
        subfingerprintStdDevSignalCoor03.put("wifi-signal-4", 1.5678);
        subfingerprintStdDevSignalCoor03.put("wifi-signal-6", 2.457);
        subfingerprintStdDevSignalCoor03.put("wifi-signal-0", 1.474);

        HashMap<String, Double> subfingerprintStdDevSignalCoor04 = new HashMap<>();
        subfingerprintStdDevSignalCoor04.put("wifi-signal-3", 2.0453);
        subfingerprintStdDevSignalCoor04.put("wifi-signal-5", 0.98439);
        subfingerprintStdDevSignalCoor04.put("wifi-signal-1", 1.4567);
        subfingerprintStdDevSignalCoor04.put("wifi-signal-4", 1.05342);
        subfingerprintStdDevSignalCoor04.put("wifi-signal-22", 2.138);
        subfingerprintStdDevSignalCoor04.put("wifi-signal-2", 1.9758);

        HashMap<String, Double> subfingerprintStdDevSignalCoor05 = new HashMap<>();
        subfingerprintStdDevSignalCoor05.put("wifi-signal-2", 1.00492);
        subfingerprintStdDevSignalCoor05.put("wifi-signal-3", 1.4968);
        subfingerprintStdDevSignalCoor05.put("wifi-signal-1", 0.07947);
        subfingerprintStdDevSignalCoor05.put("wifi-signal-4", 1.4280);
        subfingerprintStdDevSignalCoor05.put("wifi-signal-5", 0.09142);
        subfingerprintStdDevSignalCoor05.put("wifi-signal-6", 1.0003);

        HashMap<String, Double> subfingerprintStdDevSignalCoor06 = new HashMap<>();
        subfingerprintStdDevSignalCoor06.put("wifi-signal-202", 1.04523);
        subfingerprintStdDevSignalCoor06.put("wifi-signal-1", 1.04523);
        subfingerprintStdDevSignalCoor06.put("wifi-signal-2", 0.9538);
        subfingerprintStdDevSignalCoor06.put("wifi-signal-6", 1.6483);
        subfingerprintStdDevSignalCoor06.put("wifi-signal-4", 2.4515);
        subfingerprintStdDevSignalCoor06.put("wifi-signal-5", 2.415957);

        HashMap<String, HashMap<String, Double>> fingerprintStdDevSignal = new HashMap<>();
        fingerprintStdDevSignal.put("(1,1)", subfingerprintStdDevSignalCoor01);
        fingerprintStdDevSignal.put("(2,5)", subfingerprintStdDevSignalCoor02);
        fingerprintStdDevSignal.put("(3,4)", subfingerprintStdDevSignalCoor03);
        fingerprintStdDevSignal.put("(9,1)", subfingerprintStdDevSignalCoor04);
        fingerprintStdDevSignal.put("(6,7)", subfingerprintStdDevSignalCoor05);
        fingerprintStdDevSignal.put("(3,8)", subfingerprintStdDevSignalCoor06);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor01 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-1", 75.0);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-3", 15.5);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-2", 33.7);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-5", 55.0);
        subfingerprintOriginalAvgSignalCoor01.put("wifi-signal-6", 66.5);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor02 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-1", 75.0);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-3", 15.5);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-2", 33.7);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-4", 55.0);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-5", 66.5);
        subfingerprintOriginalAvgSignalCoor02.put("wifi-signal-6", 39.15);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor03 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-2", 95.0);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-3", -100.0);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-1", -33.7);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-4", 85.0);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-6", 66.5);
        subfingerprintOriginalAvgSignalCoor03.put("wifi-signal-0", 39.15);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor04 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-3", 95.0);
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-5", -100.0);
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-1", 95.0);
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-4", -100.0);
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-22", 95.0);
        subfingerprintOriginalAvgSignalCoor04.put("wifi-signal-2", -100.0);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor05 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-2", 65.0);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-3", -100.0);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-1", 133.7);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-4", -43.0);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-5", 66.5);
        subfingerprintOriginalAvgSignalCoor05.put("wifi-signal-6", 39.15);

        HashMap<String, Double> subfingerprintOriginalAvgSignalCoor06 = new HashMap<>();
        subfingerprintOriginalAvgSignalCoor06.put("wifi-signal-202", -100.0);
        subfingerprintOriginalAvgSignalCoor06.put("wifi-signal-1", -30.0);
        subfingerprintOriginalAvgSignalCoor06.put("wifi-signal-2", -80.0);
        subfingerprintOriginalAvgSignalCoor06.put("wifi-signal-6", 69.0);
        subfingerprintOriginalAvgSignalCoor06.put("wifi-signal-4", -29.0);
        subfingerprintOriginalAvgSignalCoor06.put("wifi-signal-5", 98.0);

        HashMap<String, HashMap<String, Double>> fingerprintOriginalAvgSignal = new HashMap<>();
        fingerprintOriginalAvgSignal.put("(1,1)", subfingerprintOriginalAvgSignalCoor01);
        fingerprintOriginalAvgSignal.put("(2,5)", subfingerprintOriginalAvgSignalCoor02);
        fingerprintOriginalAvgSignal.put("(3,4)", subfingerprintOriginalAvgSignalCoor03);
        fingerprintOriginalAvgSignal.put("(9,1)", subfingerprintOriginalAvgSignalCoor04);
        fingerprintOriginalAvgSignal.put("(6,7)", subfingerprintOriginalAvgSignalCoor05);
        fingerprintOriginalAvgSignal.put("(3,8)", subfingerprintOriginalAvgSignalCoor06);

        algo = new Algorithm(fingerprintOriginalAvgSignal, fingerprintOriginalAvgSignal, fingerprintStdDevSignal, fingerprintCoordinate);
    }
    /*
     *Test cases for calculating X,Y Coordinate from Joint Probability method
     */

    @Test
    public void filterWifiByFlag() {
        ArrayList<Double> targetData = new ArrayList<>();
        targetData.add(-80.0);
        targetData.add(-57.3);
        targetData.add(-89.3);
        targetData.add(-67.1);
        targetData.add(-53.2);

        double FLAG = -67.2;

        ArrayList<String> targetMacAdd = new ArrayList<>();
        targetMacAdd.add("12:34:56:78:90");
        targetMacAdd.add("34:56:78:90:12");
        targetMacAdd.add("56:78:90:12:34");
        targetMacAdd.add("78:90:12:34:56");
        targetMacAdd.add("90:12:34:56:78");

        HashMap<String, Double> actualFilteredMac = algo.filterWifiByFlag(targetData, FLAG, targetMacAdd);

        HashMap<String, Double> expectedFilteredMac = new HashMap<>();
        expectedFilteredMac.put("34:56:78:90:12", -57.3);
        expectedFilteredMac.put("78:90:12:34:56", -67.1);
        expectedFilteredMac.put("90:12:34:56:78", -53.2);

        assertEquals(expectedFilteredMac, actualFilteredMac);
    }

    @Test
    public void filterWifiByFlagIfFLAGZero() {
        ArrayList<Double> targetData = new ArrayList<>();
        targetData.add(-80.0);
        targetData.add(-57.3);
        targetData.add(-89.3);
        targetData.add(-67.1);
        targetData.add(-53.2);

        ArrayList<String> targetMacAdd = new ArrayList<>();
        targetMacAdd.add("12:34:56:78:90");
        targetMacAdd.add("34:56:78:90:12");
        targetMacAdd.add("56:78:90:12:34");
        targetMacAdd.add("78:90:12:34:56");
        targetMacAdd.add("90:12:34:56:78");

        HashMap<String, Double> actualFilteredMac = algo.filterWifiByFlag(targetData, 0.0, targetMacAdd);
        HashMap<String, Double> expectedFilteredMac = new HashMap<>();

        assertEquals(expectedFilteredMac, actualFilteredMac);
    }

    @Test
    public void calculateFlagTestIfEmpty() {
        ArrayList<Double> testData = new ArrayList<>();
        double flagValue = algo.calculateFlag(testData);
        assertEquals(0.0, flagValue, DELTA);
    }

    @Test
    public void calculateFlagTest() {
        ArrayList<Double> testData = new ArrayList<>();
        testData.add(-85.1);
        testData.add(-45.9);
        testData.add(-52.1);
        testData.add(-63.2);
        testData.add(-78.6);
        double flagValue = algo.calculateFlag(testData);
        assertEquals(-49.0, flagValue, DELTA);
    }

    @Test
    public void checkPercentageMatchIfNull() {
        HashMap<String, Double> filteredMacNull = new HashMap<>();
        HashMap<String, ArrayList<String>> pointsFB = new HashMap<>();
        HashMap<String, String> signalBSSIDFB = new HashMap<>();
        HashMap<String, Double> signalStrengthFB = new HashMap<>();
        double match = algo.checkPercentageMatch("", filteredMacNull, pointsFB, signalBSSIDFB, signalStrengthFB);
        assertEquals(0.0, match, DELTA);
    }

    @Test
    public void checkPercentageMatch() {
        HashMap<String, Double> filteredMac = new HashMap<>();
        filteredMac.put("12:34:56:78:90", -45.7);
        filteredMac.put("34:56:78:90:12", -52.7);
        filteredMac.put("56:78:90:12:34", -73.2);
        filteredMac.put("78:90:12:34:56", -64.1);
        filteredMac.put("90:12:34:56:78", -85.2);

        HashMap<String, ArrayList<String>> pointsFB = new HashMap<>();
        ArrayList<String> signalsIDs = new ArrayList<>();
        signalsIDs.add("signal-1");
        signalsIDs.add("signal-2");
        signalsIDs.add("signal-3");
        pointsFB.put("point-1", signalsIDs);

        HashMap<String, String> signalBSSIDFB = new HashMap<>();
        signalBSSIDFB.put("signal-1", "12:34:56:78:90");
        signalBSSIDFB.put("signal-2", "34:56:78:90:12");
        signalBSSIDFB.put("signal-3", "56:78:90:12:34");

        HashMap<String, Double> signalStrengthFB = new HashMap<>();
        signalStrengthFB.put("signal-1", -43.5);
        signalStrengthFB.put("signal-2", -55.0);
        signalStrengthFB.put("signal-3", -72.6);
        System.out.println(signalStrengthFB);

        double match = algo.checkPercentageMatch("point-1", filteredMac, pointsFB, signalBSSIDFB, signalStrengthFB);

        assertEquals(0.6, match, DELTA);
    }

    @Test
    public void stringifyCoordinatesIfCoordinatesNull() {
        Coordinate coord = null;

        String output = algo.stringifyCoordinates(coord);

        assertEquals("", output);
    }

    @Test
    public void stringifyCoordinates() {
        Coordinate coord = new Coordinate(2, 4);

        String output = algo.stringifyCoordinates(coord);

        assertEquals("2.0, 4.0", output);
    }

    @Test
    public void preMatchingK() {
        ArrayList<Double> targetData = new ArrayList<>();
        targetData.add(-80.0);
        targetData.add(-57.3);
        targetData.add(-89.3);
        targetData.add(-67.1);
        targetData.add(-53.2);

        ArrayList<String> targetMacAdd = new ArrayList<>();
        targetMacAdd.add("12:34:56:78:90");
        targetMacAdd.add("34:56:78:90:12");
        targetMacAdd.add("56:78:90:12:34");
        targetMacAdd.add("78:90:12:34:56");
        targetMacAdd.add("90:12:34:56:78");

        HashMap<String, ArrayList<String>> pointsFB = new HashMap<>();
        HashMap<String, Coordinate> pointsCoordinatesFB = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            ArrayList<String> signalsIDs = new ArrayList<>();
            signalsIDs.add("signal-" + i);
            signalsIDs.add("signal-" + (i+1));
            signalsIDs.add("signal-" + (i+1));
            pointsFB.put("point-" + i, signalsIDs);
            pointsCoordinatesFB.put("point-" + i, new Coordinate(2*i, 2*i+2));
        }

        HashMap<String, Double> signalStrengthFB = new HashMap<>();
        HashMap<String, Double> signalStrengthOriginalFB = new HashMap<>();
        HashMap<String, String> signalBSSIDFB = new HashMap<>();
        HashMap<String, Double> signalStrengthSDFB = new HashMap<>();

        String[] fakeBSSIDs = { "12:34:56:78:90", "90:12:34:56:78", "78:90:12:34:56", "56:78:90:12:34", "34:56:78:90:12", "21:34:56:78:90", "21:43:56:78:90"};
        double[] signalStrengths = {-81.32, -53.9, -54.796, -63.95, -65.43, -78.43, -67.85};
        double[] signalStrengthsOriginal = {-80.90, -53.21, -53.32, -67.78, -74.83, -76.494, -69.24};
        for (int i = 0; i < 7; i ++) {
            signalStrengthFB.put("signal-" + i, signalStrengths[i]);
            signalStrengthOriginalFB.put("signal-" + i, signalStrengthsOriginal[i]);
            signalBSSIDFB.put("signal-" + i, fakeBSSIDs[i]);
            signalStrengthSDFB.put("signal-" + i, 1.0);
        }

        algo = new Algorithm(pointsFB, pointsCoordinatesFB, signalStrengthFB, signalStrengthOriginalFB, signalBSSIDFB, signalStrengthSDFB);
        algo.preMatchingK(targetData, targetMacAdd);

        HashMap<String, HashMap<String, Double>> actualOriginalAvgSignal = new HashMap<>();
        HashMap<String, HashMap<String, Double>> actualAvgSignal = new HashMap<>();
        HashMap<String, HashMap<String, Double>> actualStdDevSignal = new HashMap<>();
        HashMap<String, Double> data = new HashMap<>();
        data.put("90:12:34:56:78", -53.21);
        data.put("78:90:12:34:56", -53.32);
        actualOriginalAvgSignal.put("2.0, 4.0", data);
        HashMap<String, Double> data2 = new HashMap<>();
        data2.put("90:12:34:56:78", -53.9);
        data2.put("78:90:12:34:56", -54.796);
        actualAvgSignal.put("2.0, 4.0", data2);
        HashMap<String, Double> data3 = new HashMap<>();
        data3.put("90:12:34:56:78", 1.0);
        data3.put("78:90:12:34:56", 1.0);
        actualStdDevSignal.put("2.0, 4.0", data3);
        HashMap<String, Double> data4 = new HashMap<>();
        data4.put("12:34:56:78:90", -80.9);
        data4.put("90:12:34:56:78", -53.21);
        actualOriginalAvgSignal.put("0.0, 2.0", data4);
        HashMap<String, Double> data5 = new HashMap<>();
        data5.put("12:34:56:78:90", -81.32);
        data5.put("90:12:34:56:78", -53.9);
        actualAvgSignal.put("0.0, 2.0", data5);
        HashMap<String, Double> data6 = new HashMap<>();
        data6.put("12:34:56:78:90", 1.0);
        data6.put("90:12:34:56:78", 1.0);
        actualStdDevSignal.put("0.0, 2.0", data6);

        assertEquals(actualOriginalAvgSignal, algo.fingerprintOriginalAvgSignal);
        assertEquals(actualAvgSignal, algo.fingerprintAvgSignal);
        assertEquals(actualStdDevSignal, algo.fingerprintStdDevSignal);
    }

    @Test
    public void preMatchingKIfNoMatches() {
        ArrayList<Double> targetData = new ArrayList<>();
        targetData.add(-80.0);
        targetData.add(-57.3);
        targetData.add(-89.3);
        targetData.add(-67.1);

        ArrayList<String> targetMacAdd = new ArrayList<>();
        targetMacAdd.add("12:43:56:78:90");
        targetMacAdd.add("34:56:87:90:12");
        targetMacAdd.add("56:78:09:12:34");
        targetMacAdd.add("78:90:12:34:65");

        HashMap<String, ArrayList<String>> pointsFB = new HashMap<>();
        HashMap<String, Coordinate> pointsCoordinatesFB = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            ArrayList<String> signalsIDs = new ArrayList<>();
            signalsIDs.add("signal-" + i);
            signalsIDs.add("signal-" + (i + 1));
            signalsIDs.add("signal-" + (i + 1));
            pointsFB.put("point-" + i, signalsIDs);
            pointsCoordinatesFB.put("point-" + i, new Coordinate(2 * i, 2 * i + 2));
        }

        HashMap<String, Double> signalStrengthFB = new HashMap<>();
        HashMap<String, Double> signalStrengthOriginalFB = new HashMap<>();
        HashMap<String, String> signalBSSIDFB = new HashMap<>();
        HashMap<String, Double> signalStrengthSDFB = new HashMap<>();

        String[] fakeBSSIDs = {"12:34:56:78:90", "90:12:34:56:78", "78:90:12:34:56", "56:78:90:12:34", "34:56:78:90:12", "21:34:56:78:90", "21:43:56:78:90"};
        double[] signalStrengths = {-89.32, -45.04, -54.796, -63.95, -73.43, -78.43, -67.85};
        double[] signalStrengthsOriginal = {-83.90, -43.21, -53.32, -67.78, -74.83, -76.494, -69.24};
        for (int i = 0; i < 7; i++) {
            signalStrengthFB.put("signal-" + i, signalStrengths[i]);
            signalStrengthOriginalFB.put("signal-" + i, signalStrengthsOriginal[i]);
            signalBSSIDFB.put("signal-" + i, fakeBSSIDs[i]);
            signalStrengthSDFB.put("signal-" + i, 1.0);
        }

        algo = new Algorithm(pointsFB, pointsCoordinatesFB, signalStrengthFB, signalStrengthOriginalFB, signalBSSIDFB, signalStrengthSDFB);
        algo.preMatchingK(targetData, targetMacAdd);
        assertEquals(true, algo.filteredFailed);
    }

    @Test
    public void omegaJointProbTest(){
        double output = algo.omegaJointProb(5);
        assertEquals(Math.log10(5), output, DELTA);
    }

    @Test
    public void omegaJointProbZeroTest(){
        double output = algo.omegaJointProb(0);
        assertEquals(Math.log10(1), output, DELTA);
    }

    @Test
    public void calculateYJointProbTest(){
        double output = algo.calculateYJointProb(5.9, new Coordinate(7, 5.9));
        //for easy testing
        output = (double) Math.round(output * 100) / 100;
        assertEquals(4.55, output, DELTA);
    }

    @Test
    public void calculateYJointProbTestFalse(){
        double output = algo.calculateYJointProb(5.9, new Coordinate(7, 5.9));
        //for easy testing
        output = (double) Math.round(output * 100) / 100;
        assertFalse(100.456 == output);
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
    public void calculateXJointProbTestFalse(){
        double output = algo.calculateXJointProb(5.9, new Coordinate(7, 5.9));
        //for easy testing
        output = (double) Math.round(output * 100) / 100;
        assertFalse(49.008 == output);
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

        assertEquals(5.086800936887126, output.getX(), DELTA);
        assertEquals(4.818239369094473, output.getY(), DELTA);
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
        assertEquals(4.002195680812009, output.getX(), DELTA);
        assertEquals(3.6850819739876397, output.getY(), DELTA);
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
        assertEquals( 3.8455745350405257, output.getX(), DELTA);
        assertEquals(5.863858525140664, output.getY(), DELTA);
    }

    @Test
    public void calculateEuclideanCoordinateTestXFalse(){
        ArrayList<Double> euclideanArray = new ArrayList<>();
        euclideanArray.add(5.5);
        euclideanArray.add(3.9);
        euclideanArray.add(8.8031);
        euclideanArray.add(4.132);
        euclideanArray.add(3.15);
        euclideanArray.add(1.001);

        Coordinate output = algo.calculateEuclideanCoordinate(euclideanArray);
        assertNotEquals(4.558, output.getX(), DELTA);
        assertEquals(5.863858525140664, output.getY(), DELTA);
    }

    @Test
    public void calculateEuclideanCoordinateTestYFalse(){
        ArrayList<Double> euclideanArray = new ArrayList<>();
        euclideanArray.add(5.5);
        euclideanArray.add(3.9);
        euclideanArray.add(8.8031);
        euclideanArray.add(4.132);
        euclideanArray.add(3.15);
        euclideanArray.add(1.001);

        Coordinate output = algo.calculateEuclideanCoordinate(euclideanArray);
        assertEquals( 3.8455745350405257, output.getX(), DELTA);
        assertNotEquals(4.66336784344, output.getY(), DELTA);
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
        assertEquals( 4.283169849490053, output.getX(), DELTA);
        assertEquals(3.3002643872956865, output.getY(), DELTA);
    }

    @Test
    public void euclideanDistanceTestXFalse(){
        ArrayList<Double> targetData = new ArrayList<>();
        targetData.add(5.2);
        targetData.add(20.3);
        targetData.add(40.5);
        targetData.add(51.5);
        targetData.add(79.4);
        targetData.add(45.7);

        ArrayList<Double> targetStdDev = new ArrayList<>();
        targetStdDev.add(3.5);
        targetStdDev.add(5.2);
        targetStdDev.add(12.3);
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
        assertNotEquals( 4.283169849490053, output.getX(), DELTA);
        assertEquals(3.305941790539165, output.getY(), DELTA);
    }

    @Test
    public void euclideanDistanceTestYFalse(){
        ArrayList<Double> targetData = new ArrayList<>();
        targetData.add(5.2);
        targetData.add(20.3);
        targetData.add(40.5);
        targetData.add(51.5);
        targetData.add(79.4);
        targetData.add(45.7);

        ArrayList<Double> targetStdDev = new ArrayList<>();
        targetStdDev.add(3.5);
        targetStdDev.add(5.2);
        targetStdDev.add(12.3);
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
        assertEquals( 4.341365146377838, output.getX(), DELTA);
        assertNotEquals(2.4589835473, output.getY(), DELTA);
    }

    @Test
    public void subDEuclideanDisTest(){
        double output = algo.subDEuclideanDis(4.5, 2.2, 3.3, 1.3);
        //for easy testing, round off to 2 decimal places
        output = (double) Math.round(output * 100) / 100;
        assertEquals(22.09, output, DELTA);
    }

    @Test
    public void subDEuclideanDisTestFalse(){
        double output = algo.subDEuclideanDis(0.0, 2.2, 3.3, 1.3);
        //for easy testing, round off to 2 decimal places
        output = (double) Math.round(output * 100) / 100;
        assertNotEquals(22.09, output, DELTA);
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
        assertEquals(420.13125, output.getX(), DELTA);
        assertEquals(31.93, output.getY(), DELTA);
    }

    @Test
    public void weightedFusionTestXFalse(){
        Coordinate output = algo.weightedFusion(new Coordinate(415.667, 31.2), new Coordinate(433.524, 34.12));
        assertNotEquals(112.4546, output.getX(), DELTA);
        assertEquals(31.93, output.getY(), DELTA);
    }

    @Test
    public void weightedFusionTestYFalse(){
        Coordinate output = algo.weightedFusion(new Coordinate(125.322, 31.2), new Coordinate(100.524, 34.12));
        assertEquals(119.1225, output.getX(), DELTA);
        assertNotEquals(112.4546, output.getY(), DELTA);

    }
}