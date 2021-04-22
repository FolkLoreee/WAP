package com.example.wap;

import com.example.wap.models.Coordinate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapActivityTest {

    @Test
    public void centerOfRectTestPass(){

        Coordinate coordinateTest = new Coordinate(0,0);

        float centerTest[] = MapActivity.centerOfRect(coordinateTest, 5,5);
        assertEquals(2.5, centerTest[0], 1e-15);
        assertEquals(2.5, centerTest[1], 1e-15);
    }

    @Test
    public void centerOfRectTestCoordinateNullFail(){

        Coordinate coordinateTest = null;

        float centerTest[] = MapActivity.centerOfRect(coordinateTest, 5,5);
        assertEquals(0, centerTest[0], 1e-15);
        assertEquals(0, centerTest[1], 1e-15);
    }

    @Test
    public void centerOfRectTestHeightFail(){

        Coordinate coordinateTest = new Coordinate(0,0);

        float centerTest[] = MapActivity.centerOfRect(coordinateTest, 5,-5);
        assertEquals(0, centerTest[0], 1e-15);
        assertEquals(0, centerTest[1], 1e-15);
    }

    @Test
    public void centerOfRectTestWidthFail(){

        Coordinate coordinateTest = new Coordinate(0,0);

        float centerTest[] = MapActivity.centerOfRect(coordinateTest, -5,5);
        assertEquals(0, centerTest[0], 1e-15);
        assertEquals(0, centerTest[1], 1e-15);
    }
}
