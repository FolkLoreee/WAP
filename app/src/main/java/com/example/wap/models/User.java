package com.example.wap.models;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.AuthRequirementOrBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class User {
    private final static String TAG = "User operations";


    private String username;
    private String email;
    private Authorization auth;

    public User(){}

    public User(String username, String email, Authorization auth){
        this.username = username;
        this.email = email;
        this.auth = auth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Authorization getAuth() {
        return auth;
    }

    public void setAuth(Authorization auth) {
        this.auth = auth;
    }
}
