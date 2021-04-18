package com.example.wap;

import android.widget.ListView;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public class ImageSelectActivityTest {
    private ImageSelectActivity imageSelectActivity;
    private ImageSelectAdapter imageSelectAdapter;

    @Before
    public void runBeforeEachTest() {
        System.out.println("setting up ImageUploadAcitivityTest");
        imageSelectActivity = new ImageSelectActivity();
    }

    @Test
    public void imageSelectFailNoImage(){
        try{
            imageSelectActivity.select.callOnClick();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
