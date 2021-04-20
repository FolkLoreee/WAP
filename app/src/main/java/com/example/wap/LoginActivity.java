package com.example.wap;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import static com.example.wap.R.layout.activity_login;

public class LoginActivity extends AppCompatActivity {
    //TODO: Handle the back button behaviour
    //TODO: Handle the application lifecycle

    private final String TAG = "LoginActivity";
    EditText etEmail, etPassword;
    TextView tvClickToRegister;
    ImageButton loginBtn;

    FirebaseAuth firebaseAuth;
    WAPFirebase<User> userWAPFirebase;

    Authorization userAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Getting resources
        super.onCreate(savedInstanceState);
        setContentView(activity_login);
        etEmail = findViewById(R.id.loginfield);
        etPassword = findViewById(R.id.passwordfield);
        loginBtn = findViewById(R.id.login_button);

        tvClickToRegister = findViewById(R.id.tvClickToRegister);

        //Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        //Firebase Firestore
        userWAPFirebase = new WAPFirebase<>(User.class, "users");

        //Fetching Intents
        Intent intent = getIntent();
        if (intent != null) {
            String email = intent.getStringExtra(SignUpActivity.EMAIL_KEY);
            String password = intent.getStringExtra(SignUpActivity.PASSWORD_KEY);
            etEmail.setText(email);
            etPassword.setText(password);
        }


        tvClickToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set status to register instead of login
                //Start sign up activity
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Validate that email and password fields are not empty
                boolean inputsValid = AuthUtils.validateEmail(etEmail.getText().toString()) && AuthUtils.validateInputField(etPassword.getText().toString(), "password");
                if (inputsValid) {
                    firebaseAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("Login Successful");
                            builder.setMessage("welcome to WAP");

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            userWAPFirebase.query(firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<User>() {
                                @Override
                                public void onSuccess(User user) {
                                    try {
                                        userAuth = user.getAuth();
//                                        Log.d(TAG, "LoginActivity:" + String.valueOf(userAuth));
                                        if(userAuth.equals(Authorization.ADMIN)){
                                            startActivity(new Intent(LoginActivity.this, ChooseMapActivity.class));
                                            finish();
                                        }
                                        else{
                                            Log.d(TAG, "LoginActivity:" + String.valueOf(userAuth) + "   " +String.valueOf(Authorization.ADMIN));
                                            startActivity(new Intent(LoginActivity.this, TestingActivityUser.class));
                                            finish();
                                        }
                                    }
                                    catch (Exception e){
                                        Log.d(TAG, "LoginActivity: userAuth Fail");
                                        e.printStackTrace();
                                    }


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "LoginActivity: Here fail here");
                                }
                            });



                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Invalid email/password", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
                else{
                    Toast.makeText(LoginActivity.this, "Invalid email/password", Toast.LENGTH_SHORT).show();
                }
            }

            ;
        });
    }
}



