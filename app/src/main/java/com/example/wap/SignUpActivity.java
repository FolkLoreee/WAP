package com.example.wap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.firebase.WAPFirebase;
import com.example.wap.models.Authorization;
import com.example.wap.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity {

    private final String TAG = "SignUpActivity";
    protected final String PASSWORD_KEY = "passwordKey";
    protected final String USERNAME_KEY = "usernameKey";
    EditText etUsernameSignup, etPasswordSignup, etEmailSignup;
    Button signupButton;
    Spinner authSpinner;

    FirebaseAuth firebaseAuth;
    WAPFirebase<User> userWAPFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //Fetch resources
        etUsernameSignup = findViewById(R.id.etUsernameSignup);
        etPasswordSignup = findViewById(R.id.etPasswordSignup);
        etEmailSignup = findViewById(R.id.etEmailSignup);
        signupButton = findViewById(R.id.signupButton);
        authSpinner = findViewById(R.id.authSpinner);

        firebaseAuth = FirebaseAuth.getInstance();
        userWAPFirebase = new WAPFirebase<>(User.class,"users");
        ArrayAdapter<Authorization> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Authorization.values());
        authSpinner.setAdapter(arrayAdapter);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                boolean emailValid = AuthUtils.validateEmail(etEmailSignup.getText().toString());
                //TODO: check if there is any other validations required
                if (emailValid) {

                    //Create a new User object
                    User newUser = new User();
                    newUser.setUsername(etUsernameSignup.getText().toString());
                    newUser.setEmail(etEmailSignup.getText().toString());

                    //Implement a spinner to choose Authorization
                    newUser.setAuth(Authorization.ADMIN);

                    //Signing up the user to firebase auth and firebase firestore
                    firebaseAuth.createUserWithEmailAndPassword(newUser.getEmail(), etPasswordSignup.getText().toString())

                            //? If Firebase Auth creation succeeds
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    //Create Firestore User object
                                    userWAPFirebase.create(newUser, firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "User " + newUser.getUsername() + " has been successfully created");
                                            Toast.makeText(SignUpActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();

                                            //Go back to Login Activity
                                            Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                                            loginIntent.putExtra(PASSWORD_KEY, etPasswordSignup.getText().toString());
                                            loginIntent.putExtra(USERNAME_KEY, newUser.getUsername());
                                            startActivity(loginIntent);
                                        }
                                    });
                                }
                            })
                            //! If Firebase Auth User creation fails
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e instanceof FirebaseAuthUserCollisionException) {
                                        if (((FirebaseAuthUserCollisionException) e).getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                                            Log.w(TAG, "Email is already in use.");
                                            Toast.makeText(SignUpActivity.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e(TAG, "Error in creating Firebase Auth user: "+e.toString(),e);
                                        Toast.makeText(SignUpActivity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(SignUpActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
