package com.example.tourism_cif;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn, fbLoginBtn, googleLoginBtn;
    private TextView needNewAccount;
    private EditText userEmail, userPassword;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        needNewAccount = (TextView) findViewById(R.id.back_to_sign_up);
        userEmail = (EditText) findViewById(R.id.login_email);
        userPassword = (EditText) findViewById(R.id.login_password);
        loginBtn = (Button) findViewById(R.id.sign_in_button);

        needNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegister();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowtoLogin();
            }
        });
    }

    private void AllowtoLogin() {
        final String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please fill in your email", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in your password", Toast.LENGTH_SHORT).show();
        } else{
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToMain();
                                Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            } else{
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error occured: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        //not authenticated
        if(currentUser != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendToRegister() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);

    }
}