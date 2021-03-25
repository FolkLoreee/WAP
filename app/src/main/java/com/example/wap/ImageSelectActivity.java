package com.example.wap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.graphics.Bitmap.createBitmap;

public class ImageSelectActivity extends ListFragment {

    public static final String KEY_User_Document1 = "doc1";
    private final String TAG = "Image Upload Activity";
    FirebaseStorage storage;
    StorageReference storageRef;
    String locationName;
    String locationID;
    WAPFirebase<Location> locationWAPFirebase;
    ListView listView;
    ArrayList<Location> locationList = new ArrayList<Location>();
    LayoutInflater inflater;

    Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    private String Document_img1 = "";

    public static Context contextOfApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_image_select, parent, false);
    }

    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationWAPFirebase = new WAPFirebase<>(Location.class,"locations");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        locationWAPFirebase.getCollection().addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
            @Override
            public void onSuccess(ArrayList<Location> locations) {
                Log.d("Help", "success");
                for(int i = 0; i<locations.size(); i++){
                    locationList.add(locations.get(i));
                    Log.d("Help", String.valueOf(locationList.get(i).getLocationID()));
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Help", String.valueOf(e));
            }
        });
//        Log.d("Help", String.valueOf(locationList.get(0)));
        ArrayAdapter<Location> locationArrayAdapter = new LocationAdapter(this.getActivity(), R.layout.activity_image_select,locationList);
        getListView().setAdapter(locationArrayAdapter);
    }

    //    @Override
//    public void onActivityCreated(View view, Bundle savedInstanceState) {
//
//        locationWAPFirebase = new WAPFirebase<>(Location.class,"locations");
//        storage = FirebaseStorage.getInstance();
//        storageRef = storage.getReference();
//
//        listView = view.findViewById(R.id.listView);
//        locationWAPFirebase.getCollection().addOnSuccessListener(new OnSuccessListener<ArrayList<Location>>() {
//            @Override
//            public void onSuccess(ArrayList<Location> locations) {
//                Log.d("Help", "success");
//                for(int i = 0; i<locations.size(); i++){
//                    locationList.add(locations.get(i));
//                }
//            }
//
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d("Help", String.valueOf(e));
//            }
//        });
////        Log.d("Help", String.valueOf(locationList.get(0)));
//        populateList();
//        inflater.inflate(R.layout.activity_image_select, (ViewGroup) view.getParent(), false);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            Bitmap image;
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                URL url = null;
//                try {
//                    url = new URL(locationList.get(position).getMapImage());
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                splitImage(image);
//            }
//        });
//
//    }

    private void splitImage(Bitmap bitmap) {

        //For the number of rows and columns of the grid to be displayed
        int rows, cols;
        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;
        //To store all the small image chunks in bitmap format in this list
        //To store all the xy coordinate of the image chunks
        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>();
        ArrayList<Coordinate> coordImages = new ArrayList<Coordinate>();

        //Getting the scaled bitmap of the source image
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        rows = cols = (int) Math.sqrt(300);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;

        //xCoord and yCoord are the pixel positions of the image chunks
        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
                coordImages.add(new Coordinate(xCoord, yCoord));

            }
            yCoord += chunkHeight;
        }

        MapViewActivity.imageChunks = chunkedImages;
        MapViewActivity.imageChunksCopy = makeDeepCopyInteger(chunkedImages);

        MapViewActivity.imageCoords = coordImages;

        Intent intent = new Intent(getActivity(), MapViewActivity.class);
        startActivity(intent);
    }


    public class LocationAdapter extends ArrayAdapter<Location> {
        private int layoutResource;
        public LocationAdapter(Context context, int layoutResource, ArrayList<Location> users) {
            super(context, layoutResource, users);
            this.layoutResource = layoutResource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(layoutResource, null);
            }
            Location currentLocation = getItem(position);

            TextView locationName = (TextView) view.findViewById(R.id.textView);
            locationName.setText(currentLocation.getName());
            TextView locationID = (TextView) view.findViewById(R.id.textView2);
            locationID.setText(currentLocation.getLocationID());

            return view;
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




}
