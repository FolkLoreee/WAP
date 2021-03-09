package com.example.wap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.wap.models.Coordinate;

public class TestingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        // TODO: Retrieve data from Firebase

        // TODO: Pre-matching of fingerprints?
    }

    private Coordinate euclideanDistance() {
        // TODO: Euclidean distance positioning algorithm (Hannah)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    private Coordinate jointProbability() {
        // TODO: joint probability positioning algorithm (Hannah)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    private Coordinate cosineSimilarity() {
        // TODO: cosine similarity positioning algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

    private Coordinate weightedFusion() {
        // TODO: add different weights to each algorithm (Sherene)
        Coordinate position = new Coordinate(0,0);
        return position;
    }

}