package com.example.tourism_cif;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText userName, userCountry;
    private Button saveProfileBtn;
    private CircleImageView profilePicture;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        userName = (EditText) findViewById(R.id.username_field);
        userCountry = (EditText) findViewById(R.id.user_country_field);
        saveProfileBtn = (Button) findViewById(R.id.save_profile_button);
        profilePicture = (CircleImageView) findViewById(R.id.profile_picture);

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserAccountInfo();
            }
        });
    }

    private void saveUserAccountInfo() {
        String username = userName.getText().toString();
        String country = userCountry.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please fill in username", Toast.LENGTH_SHORT).show();
        } if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please fill in country", Toast.LENGTH_SHORT).show();
        } else{
            //save user info using hashmap
            HashMap userMap = new HashMap();
            userMap.put("Username", username);
            userMap.put("Country", country);
            userMap.put("App Mode", "General");

            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SetupActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();
                        sendToMain();
                    } else{
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    private void sendToMain() {
        Intent setupIntent = new Intent(SetupActivity.this, MainActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}