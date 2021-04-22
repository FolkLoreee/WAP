package com.example.wap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wap.firebase.WAPFirebase;
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

import static android.app.Activity.RESULT_OK;

//import androidx.appcompat.app.AppCompatActivity;

public class ImageUploadAcitivity extends Fragment {

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

    public static Context contextOfApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_image_upload, parent, false);
    }

    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }

    @SuppressLint("WrongViewCast")
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

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationID = locationIDText.getText().toString();
                locationName = locationNameText.getText().toString();
                if(locationID == "" || locationID == null){
                    Toast.makeText(ImageUploadAcitivity.getContextOfApplication(), "No location ID", Toast.LENGTH_SHORT).show();
                }
                else if(locationName == "" || locationName == null){
                    Toast.makeText(ImageUploadAcitivity.getContextOfApplication(), "No Location Name", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (filePath == null) {
                        Toast.makeText(ImageUploadAcitivity.getContextOfApplication(), "Upload an image", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadMapImage();

                        Intent intent = new Intent(getActivity(), MapActivity.class);
                        intent.putExtra("locationID", locationID);
                        intent.putExtra("BitmapImage", bitmap);
                        startActivity(intent);
                    }
                }

            }
        });
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

    //TODO: test this function somehow
    private void uploadMapImage(){

        final StorageReference ref = storageRef.child("maps/" + locationID);

            UploadTask uploadTask = ref.putFile(filePath);
            // Retrieve the download url for the image uploaded to Firebase Storage
            // Download url is to be used to store in Firestore and to display later using Picasso
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Log.e("error", "null here");
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
        // Retrieve Location Details

        final Location location = new Location(locationID, locationName, storageLocation);
        // location.setMapImage(storageLocation);
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

}
