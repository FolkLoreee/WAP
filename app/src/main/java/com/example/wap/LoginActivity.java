package com.example.wap;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.wap.R.layout.activity_login;

public class LoginActivity extends AppCompatActivity {
    EditText etId,etPassword;
    Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(activity_login);
        etId = (EditText) etId.findViewById(R.id.loginfield);
        etPassword = (EditText) etPassword.findViewById(R.id.passwordfield);
        loginBtn = (Button) loginBtn.findViewById(R.id.login_button);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etId.getText().toString().equals("admin") && etPassword.getText().toString().equals("123")){
                    Context context;
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Login Successful");
                    builder.setMessage("welcome to WAP");

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();



                }
                else{
                    Toast.makeText(LoginActivity.this, "Invalid User/Pass", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    }



