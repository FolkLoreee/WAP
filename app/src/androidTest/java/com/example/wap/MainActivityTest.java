package com.example.wap;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Location;
import com.google.firebase.FirebaseApp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest{

    Context appContext;
    WAPFirebase<Location> locationWAPFirebase;
    @Test
    @Before
    public void useAppContext() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.wap", appContext.getPackageName());
    }
    @Test
    public void instantiateWAP(){
        FirebaseApp.initializeApp(appContext);
        locationWAPFirebase = new WAPFirebase<>(Location.class,"locations");
        assertNotNull("Location WAPFirebase created", locationWAPFirebase);
    }
}