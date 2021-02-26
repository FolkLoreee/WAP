package com.example.wap.models;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class User {
    private final static String TAG = "User operations";



    private String uuid;
    private String name;
    private Coordinate coordinate;
    private Authorization auth;

    public User(GoogleSignInAccount signInAccount,Authorization auth){
        Log.d(TAG, "Instantiating user");
        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = fbAuth.getCurrentUser();
        this.name = currentUser.getDisplayName();
        if(currentUser!= null){
            this.setUuid(currentUser.getUid());
        }
        else{
            Log.w(TAG, "Google Account is not authenticated");
        }
    }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Authorization getAuth() {
        return auth;
    }

    public void setAuth(Authorization auth) {
        this.auth = auth;
    }
}
