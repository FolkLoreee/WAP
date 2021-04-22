package com.example.wap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class WifiScanTest {
    private static final double DELTA = 1e-15;

    @Before
    public void runBeforeEachTest() {
        System.out.println("setting up");
    }

    @Test
    public void calculateAverageTest(){
        Integer[] readingsList = new Integer[]{55, 37, -79, 47, 90, 65, 88};
        List<Integer> readings = Arrays.asList(readingsList);

        double output = WifiScan.calculateAverage(readings);
        assertEquals(43.285714285714285, output, DELTA);
    }

    @Test
    public void calculateAverageTestNull(){
        Integer[] readingsList = new Integer[]{55, null, -79, 47, null, 65, 88};
        List<Integer> readings = Arrays.asList(readingsList);

        double output = WifiScan.calculateAverage(readings);
        assertEquals(-100, output, DELTA);
    }

    @Test
    public void calculateStandardDeviationTest(){

        Integer[] readingsList = new Integer[]{55, 37, -79, 47, 90, 65, 88};
        List<Integer> readings = Arrays.asList(readingsList);
        double average = WifiScan.calculateAverage(readings);
        double output = WifiScan.calculateStandardDeviation(readings, average);
        assertEquals(53.18086198655163, output, DELTA);
    }

    @Test
    public void calculateStandardDeviationTestNull(){

        Integer[] readingsList = new Integer[]{55, 37, null, 47, 90, null, 88};
        List<Integer> readings = Arrays.asList(readingsList);
        double average = WifiScan.calculateAverage(readings);
        double output = WifiScan.calculateStandardDeviation(readings, average);
        assertEquals(139.3079835677564, output, DELTA);
    }

    @Test
    public void calculateProcessedAverageTest(){
        double average = 49.06579;
        double output = WifiScan.calculateProcessedAverage(average);
        assertEquals(51.47412333333333, output, DELTA);
    }

    @Test
    public void calculateProcessedAverageTestFail(){
        double average = 40.079;
        double output = WifiScan.calculateProcessedAverage(average);
        assertFalse(output == 405.004);
    }


    @After
    public void runAfterEachTest()
    {
        System.out.println("setting down");
    }

}
