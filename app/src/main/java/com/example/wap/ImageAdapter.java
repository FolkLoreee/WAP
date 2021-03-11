package com.example.wap;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

//The adapter class associated with the ChunkedImageActivity class
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Bitmap> imageChunks;
    private int imageWidth, imageHeight;

    //constructor
    public ImageAdapter(Context c, ArrayList<Bitmap> images){
        mContext = c;
        imageChunks = images;
        imageWidth = images.get(0).getWidth();
        imageHeight = images.get(0).getHeight();
    }

    @Override
    public int getCount() {
        return imageChunks.size();
    }

    @Override
    public Object getItem(int position) {
        return imageChunks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        gridView = new View(mContext);

        gridView = inflater.inflate(R.layout.map_button, null);

        ImageView image = (ImageView) gridView.findViewById(R.id.map);
        image.setLayoutParams(new GridView.LayoutParams(imageWidth , imageHeight));
        image.setPadding(0, 0, 0, 10);

        image.setImageBitmap(imageChunks.get(position));
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext.getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                Log.v("Help", String.valueOf(position));
            }
        });

        return gridView;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView image;
//
//        if(convertView == null){
//            image = new ImageView(mContext);
//
//            /*
//             * NOTE: I have set imageWidth - 10 and imageHeight
//             * as arguments to LayoutParams class.
//             * But you can take anything as per your requirement
//             */
//            image.setLayoutParams(new GridView.LayoutParams(imageWidth , imageHeight));
//            image.setPadding(0, 0, 0, 10);
//        }else{
//            image = (ImageView) convertView;
//        }
//        image.setImageBitmap(imageChunks.get(position));
//        return image;
//    }
}
