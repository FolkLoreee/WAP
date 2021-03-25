package com.example.wap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wap.R;
import com.example.wap.models.Coordinate;
import com.example.wap.models.Location;

import java.util.ArrayList;

import static android.graphics.Bitmap.createBitmap;

public class ImageSelectAdapter extends ArrayAdapter<Location> {
    private int layoutResource;

    public ImageSelectAdapter(Context context, int layoutResource, ArrayList<Location> users) {
        super(context, layoutResource, users);
        this.layoutResource = layoutResource;
    }

    public ImageSelectAdapter(@NonNull Context context, int resource) {
        super(context, resource);
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