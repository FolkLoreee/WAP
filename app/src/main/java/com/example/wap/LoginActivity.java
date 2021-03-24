package com.example.wap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.wap.R.layout.activity_login;

public class LoginActivity extends AppCompatActivity {
    //MainApplication mApplication;

    EditText etId,etPassword;
    Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(activity_login);
        etId = (EditText) findViewById(R.id.loginfield);
        etPassword = (EditText) findViewById(R.id.passwordfield);
        loginBtn = (Button) findViewById(R.id.login_button);

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



