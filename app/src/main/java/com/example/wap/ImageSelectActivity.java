package com.example.wap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
    Button select;
    WAPFirebase<Location> locationWAPFirebase;
    ListView listView;
    ImageView imageView;
    ArrayList<Location> locationList = new ArrayList<Location>();
    LayoutInflater inflater;
    Bitmap bitmap;

    Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    private String Document_img1 = "";

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
        select = view.findViewById(R.id.select);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splitImage(bitmap);
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
                for(int i = 0; i<locations.size(); i++){
                    locationList.add(locations.get(i));
                    Log.d("Help", String.valueOf(locationList.get(i).getLocationID()));
                }
                ArrayAdapter<Location> locationArrayAdapter = new ImageSelectAdapter(getActivity(), R.layout.listview_item,locationList);
                getListView().setAdapter(locationArrayAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Help", String.valueOf(e));
            }
        });

    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        imageView = v.getRootView().findViewById(R.id.imageSelect);

        if(locationList.get(position).getMapImage() != null){
//            Uri imageUri = Uri.parse(locationList.get(position).getMapImage());
//            Picasso.get().load(imageUri)
//                    .fit().centerCrop().into(imageView);
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                try {
                    URL url = new URL(locationList.get(position).getMapImage());
                    locationID = locationList.get(position).getLocationID();
                    bitmap = Utils.getBitmap(url);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.d("Help", String.valueOf(e));
                }
            }

        }
        else{
            imageView.setImageResource(R.drawable.image_upload);
            Toast.makeText(getContext(), "No image", Toast.LENGTH_SHORT).show();
        }
//        try {
//            URL url = new URL(locationList.get(position).getMapImage());
//            Log.d("Help", locationList.get(position).getMapImage());
//            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            splitImage(image);
//            Log.d("help", "trying");
//        } catch(IOException e) {
//            Log.d("help list",String.valueOf(e));
//        }
//        Toast.makeText(getContext(), "hello", Toast.LENGTH_SHORT).show();

    }

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
        intent.putExtra("locationID",locationID);
        startActivity(intent);

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
