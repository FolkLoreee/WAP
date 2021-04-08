package com.example.wap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.Spinner;
import android.widget.TextView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.graphics.Bitmap.createBitmap;

//import androidx.appcompat.app.AppCompatActivity;

public class ImageUploadAcitivity extends Fragment {

    public static final String KEY_User_Document1 = "doc1";
    private final String TAG = "Image Upload Activity";
    FirebaseStorage storage;
    StorageReference storageRef;
    String locationName;
    String locationID;
    WAPFirebase<Location> locationWAPFirebase;
    ImageView uploadImg;
    EditText locationIDText,locationNameText;
    ImageButton uploadBtn;
    Bitmap bitmap;
    Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    private String Document_img1 = "";

    public static Context contextOfApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_image_upload, parent, false);
    }

    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        locationWAPFirebase = new WAPFirebase<>(Location.class,"locations");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        uploadImg = (ImageView) view.findViewById(R.id.imageUpload);
        uploadBtn = (ImageButton) view.findViewById(R.id.UploadBtn);

        locationIDText = view.findViewById(R.id.locationIDEditText);
        locationNameText = view.findViewById(R.id.locationNameEditText);

        contextOfApplication = getActivity().getApplicationContext();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationID = locationIDText.getText().toString();
                locationName = locationNameText.getText().toString();
                splitImage(bitmap);
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(ImageUploadAcitivity.getContextOfApplication().getContentResolver(), filePath);
                uploadImg.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }



    private ArrayList<Bitmap> makeDeepCopyInteger(ArrayList<Bitmap> old){
        ArrayList<Bitmap> copy = new ArrayList<Bitmap>(old.size());
        for(Bitmap i : old){
            Bitmap deepCopy = createBitmap(i);
            copy.add(deepCopy);
        }
        return copy;
    }


    private void splitImage(Bitmap bitmap) {
//
//        //For the number of rows and columns of the grid to be displayed
//        int rows, cols;
//        //For height and width of the small image chunks
//        int chunkHeight, chunkWidth;
//        //To store all the small image chunks in bitmap format in this list
//        //To store all the xy coordinate of the image chunks
//        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>();
//        ArrayList<Coordinate> coordImages = new ArrayList<Coordinate>();
//
//        //Getting the scaled bitmap of the source image
//        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
//        Bitmap bitmap = drawable.getBitmap();
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
//        rows = cols = (int) Math.sqrt(16);
//        chunkHeight = bitmap.getHeight() / rows;
//        chunkWidth = bitmap.getWidth() / cols;
//
//        //xCoord and yCoord are the pixel positions of the image chunks
//        int yCoord = 0;
//        for (int x = 0; x < rows; x++) {
//            int xCoord = 0;
//            for (int y = 0; y < cols; y++) {
//                chunkedImages.add(createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
//                xCoord += chunkWidth;
//                coordImages.add(new Coordinate(xCoord, yCoord));
//
//            }
//            yCoord += chunkHeight;
//        }
//        uploadMapImage();
//        MapViewActivity.imageChunks = chunkedImages;
//        MapViewActivity.imageChunksCopy = makeDeepCopyInteger(chunkedImages);
//
//        MapViewActivity.imageCoords = coordImages;
//
////        MapViewActivity.locationID = locationIDText.getText().toString();
//        MapViewActivity.locationName = locationNameText.getText().toString();
        MapActivity.bitmapImg = bitmap;
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra("locationID",locationID);
        startActivity(intent);
    }

    private void uploadMapImage(){

        final StorageReference ref = storageRef.child("maps/" + locationID);
            UploadTask uploadTask = ref.putFile(filePath);
            // Retrieve the download url for the image uploaded to Firebase Storage
            // Download url is to be used to store in Firestore and to display later using Picasso
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                Toast.makeText(getActivity(), "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                assert downloadUri != null;
                                addToFirestore(downloadUri.toString());
                            } else {
                                Toast.makeText(getActivity(), "Upload FAILED", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    }
    private void addToFirestore(String storageLocation) {
        // Retrieve Item Details

        final Location location = new Location(locationID,locationName);
        location.setMapImage(storageLocation);
        locationWAPFirebase.create(location,locationID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"Successfully created a new location");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Fails to create a new location");
                Log.e(TAG,e.toString());
            }
        });
    }

    class CoordImages {
        int xcoord;
        int ycoord;
        CoordImages(int xcoord, int ycoord){
            this.xcoord = xcoord;
            this.ycoord = ycoord;
        }
    }

}
