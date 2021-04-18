package com.example.wap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Authorization;
import com.example.wap.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.wap.R.layout.activity_login;

public class LoginActivity extends AppCompatActivity {
    //MainApplication mApplication;
    private final String TAG = "LoginActivity";
    EditText etId, etPassword, etEmail;
    TextView tvClickToRegister;
    ImageButton loginBtn;
    FirebaseAuth firebaseAuth;
    WAPFirebase<User> userWAPFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Getting resources
        super.onCreate(savedInstanceState);
        setContentView(activity_login);
        etId = findViewById(R.id.loginfield);
        etPassword = findViewById(R.id.passwordfield);
        loginBtn = findViewById(R.id.login_button);

        tvClickToRegister = findViewById(R.id.tvClickToRegister);

        //Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        //Firebase Firestore
        userWAPFirebase = new WAPFirebase<>(User.class, "users");

        //TODO: encrypted preferences setup to allow persistent login

        tvClickToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set status to register instead of login
                //TODO: start sign up activity
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (etId.getText().toString().equals("admin") && etPassword.getText().toString().equals("123")) {
                        Context context;
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle("Login Successful");
                        builder.setMessage("welcome to WAP");

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        startActivity(new Intent(LoginActivity.this, ChooseMapActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid User/Pass", Toast.LENGTH_SHORT).show();

                    }
                }

            ;
        });
    }
}



