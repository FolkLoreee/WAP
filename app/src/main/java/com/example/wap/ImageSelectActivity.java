package com.example.wap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Location;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static android.graphics.Bitmap.createBitmap;

public class ImageSelectActivity extends ListFragment  {

    public static final String KEY_User_Document1 = "doc1";
    private final String TAG = "Image Upload Activity";
    public static final String LOCATION_ID_KEY = "locationID";
    public static final String LOCATION_NAME_KEY = "locationName";
    public static final String LOCATION_URL_KEY = "locationURL";
    FirebaseStorage storage;
    StorageReference storageRef;
    String locationName;
    String locationURL;
    String locationID;
    ImageButton select;
    WAPFirebase<Location> locationWAPFirebase;
    ListView listView;
    ImageView imageView;
    ArrayList<Location> locationList = new ArrayList<Location>();
    ArrayAdapter<Location> locationArrayAdapter;
    Bitmap bitmap = null;

    public static Context contextOfApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        locationWAPFirebase = new WAPFirebase<>(Location.class,"locations");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        return inflater.inflate(R.layout.activity_image_select, parent, false);



    }

    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        select = (ImageButton) view.findViewById(R.id.select);
        listView = view.findViewById(android.R.id.list);
        contextOfApplication = getActivity().getApplicationContext();

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap == null){
                    Toast.makeText(ImageSelectActivity.getContextOfApplication(), "No Image", Toast.LENGTH_SHORT).show();
                }
                else{
//                    MapActivity.bitmapImg = bitmap;
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(LOCATION_ID_KEY,locationID);
                    intent.putExtra(LOCATION_URL_KEY,locationURL);
                    intent.putExtra(LOCATION_NAME_KEY,locationName);

                    startActivity(intent);
                }

            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        locationWAPFirebase.getCollection().addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
            @Override
            public void onSuccess(ArrayList<Location> locations) {
                Log.d("Help", "success");
                try {
                    for (int i = 0; i < locations.size(); i++) {
                        locationList.add(locations.get(i));
                        Log.d("Help", String.valueOf(locationList.get(i).getLocationID()));
                    }
                    locationArrayAdapter = new ImageSelectAdapter(getActivity(), R.layout.listview_item, locationList);
                    getListView().setAdapter(locationArrayAdapter);
                }
                catch (Exception e){
                    e.printStackTrace();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Help", String.valueOf(e));
            }
        });

    }

    //TODO: test this function somehow
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        imageView = v.getRootView().findViewById(R.id.imageSelect);

        if(locationList.get(position).getMapImage() != null){

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                try {
                    URL url = new URL(locationList.get(position).getMapImage());
                    locationName = locationList.get(position).getName();
                    locationID = locationList.get(position).getLocationID();
                    locationURL = locationList.get(position).getMapImage();
                    bitmap = Utils.getBitmap(url);
                    imageView.setImageBitmap(bitmap);
                    MapActivity.bitmapImg = bitmap;
                } catch (IOException e) {
                    Log.d("Help", String.valueOf(e));
                }
            }

        }
        else{
            imageView.setImageResource(R.drawable.image_upload);
            Toast.makeText(getContext(), "No image", Toast.LENGTH_SHORT).show();
        }

    }

}
