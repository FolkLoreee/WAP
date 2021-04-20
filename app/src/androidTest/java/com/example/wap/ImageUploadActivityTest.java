package com.example.wap;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Location;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class ImageUploadActivityTest {

    private static final String TAG = "ImageUploadActivity Instrumented Test";
    Context activityContext;
    WAPFirebase<Location> locationWAPFirebase;
    FirebaseFirestore db;
    FirebaseStorage storage;
    ActivityScenario<ChooseMapActivity> activityScenario;
    private ImageUploadAcitivity imageUploadAcitivity;
    Location testLocation;

    @Before
    public void setup() {
        System.out.println("setting up ImageUploadAcitivityTest");
        imageUploadAcitivity = new ImageUploadAcitivity();
        locationWAPFirebase = new WAPFirebase<>(Location.class, "locations");
    }

    @After
    public void teardown() {
        if (activityScenario != null) activityScenario.close();
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

    @Test
    public void create_location_class() {
        String locationID = "TestLocation1";
        String locationName = "Testing Location";
        testLocation = new Location(locationID, locationName);
        assertNotNull("Location created", testLocation);
    }
    @Test
    public void test_setup_firebase_storage() {
        activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
        activityScenario.onActivity(activity -> {
            activityContext = activity.getApplicationContext();
            storage = FirebaseStorage.getInstance();
            StorageReference storageRef =  storage.getReference();
            assertNotNull(storageRef);
        });
    }
    @Test
    public void create_and_delete_location_firebase() {
        String locationID = "TestLocation1";
        String locationName = "Testing Location";
        testLocation = new Location(locationID, locationName);
        activityScenario = ActivityScenario.launch(ChooseMapActivity.class);
        activityScenario.onActivity(activity -> {
            activityContext = activity.getApplicationContext();
            FirebaseApp.initializeApp(activityContext);
            locationWAPFirebase.create(testLocation, locationID).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //remove the document from the db, once successful
                    locationWAPFirebase.delete(locationID).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Successfully deleted Location");
                            assertTrue(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Fails to delete Location");
                            fail();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //force it to fail the test
                    Log.d(TAG, "Fails to create Location");
                    fail();
                }
            });
        });

    }

    //UI testing

}