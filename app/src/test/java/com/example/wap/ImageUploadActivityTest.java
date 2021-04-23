package com.example.wap;

import android.util.Log;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageUploadActivityTest {

    private ImageUploadAcitivity imageUploadAcitivity;

    @Before
    public void runBeforeEachTest() {
        System.out.println("setting up ImageUploadAcitivityTest");
        imageUploadAcitivity = new ImageUploadAcitivity();
    }

    @Test
    public void imageUploadFailNoImage(){
        try{
            imageUploadAcitivity.uploadBtn.callOnClick();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Test
    public void imageUploadFailNoID(){
        try{
            imageUploadAcitivity.locationIDText.setText("");
            imageUploadAcitivity.uploadBtn.callOnClick();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void imageUploadFailNoName(){
        try{
            imageUploadAcitivity.locationNameText.setText("");
            imageUploadAcitivity.uploadBtn.callOnClick();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }





}
