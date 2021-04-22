package com.example.wap;

import com.example.wap.models.Coordinate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapActivityTest {

    @Test
    public void centerOfRectTest(){

        Coordinate coordinateTest = new Coordinate(0,0);

        float centerTest[] = MapActivity.centerOfRect(coordinateTest, 5,5);
        assertEquals(2.5, centerTest[0], 1e-15);
        assertEquals(2.5, centerTest[1], 1e-15);
    }
}
