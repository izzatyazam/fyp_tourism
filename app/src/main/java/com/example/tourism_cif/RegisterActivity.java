package com.example.tourism_cif;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText userEmail, userPassword, userConfirmPassword;
    private Button signUpBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        userConfirmPassword = (EditText) findViewById(R.id.register_cfm_password);
        signUpBtn = (Button) findViewById(R.id.sign_up_button);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
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

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String cfmPassword = userConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please fill in your email", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in your password", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cfmPassword)) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cfmPassword)){
            Toast.makeText(this, "Your password do not match", Toast.LENGTH_SHORT).show();
        } else{
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToSetupProfile();
                                Toast.makeText(RegisterActivity.this, "Authentication succeed", Toast.LENGTH_SHORT).show();

                            } else{
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error occured: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sendToSetupProfile() {
        Intent setupProfileIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupProfileIntent);
        finish();
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

