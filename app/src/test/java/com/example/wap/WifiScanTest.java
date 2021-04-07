package com.example.wap;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class WifiScanTest {
    ArrayList<Integer> readings;
    Integer sum;
    @Before
    public void setup(){
        readings = new ArrayList<>(Arrays.asList(20,50,32,48));
        sum = 0;
    }
    @Test
    public void test_calculate_average(){
        for(Integer reading:readings){
            sum+=reading;
        }
        Integer expectedAverage = sum/readings.size();
        Integer actualAverage = WifiScan.calculateAverage(readings);
        assertEquals(expectedAverage,actualAverage);
    }
    @Test
    public void test_calculate_standard_deviation(){
        int average = WifiScan.calculateAverage(readings);
        for(Integer reading:readings){
            sum += (reading - average);
        }
        int expectedStandardDeviation = (int)(Math.sqrt(sum/readings.size()));
        int actualStandardDeviation = WifiScan.calculateStandardDeviation(readings,average);
        assertEquals(expectedStandardDeviation,actualStandardDeviation);
    }

}
