package com.example.tourism_cif;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userCountry, appMode;
    private Button updateProfileBtn;

    private DatabaseReference settingUserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        settingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        userName = (EditText) findViewById(R.id.settings_username);
        userCountry = (EditText) findViewById(R.id.settings_country);
        appMode = (EditText) findViewById(R.id.settings_app_mode);
        updateProfileBtn = (Button) findViewById(R.id.settings_update_button );

        settingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myUserName = dataSnapshot.child("Username").getValue().toString();
                    String myCountry = dataSnapshot.child("Country").getValue().toString();
                    String myMode = dataSnapshot.child("App Mode").getValue().toString();

                    userName.setText(myUserName);
                    userCountry.setText(myCountry);
                    appMode.setText(myMode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateProfileInfo();
            }
        });




    }

    private void validateProfileInfo() {
        String uName = userName.getText().toString();
        String cName = userCountry.getText().toString();
        String aMode = appMode.getText().toString();

        if(TextUtils.isEmpty(uName)) {
            Toast.makeText(this, "Please update your username", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(cName)) {
            Toast.makeText(this, "Please update your country", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(aMode)) {
            Toast.makeText(this, "Please update app mode", Toast.LENGTH_SHORT).show();
        } else{
            UpdateProfileInfo(uName, cName, aMode);
        }
    }

    private void UpdateProfileInfo(String uName, String cName, String aMode) {
        HashMap userMap = new HashMap();
        userMap.put("Username", uName);
        userMap.put("Country", cName);
        userMap.put("App Mode", aMode);
        settingUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendToMain();
                    Toast.makeText(SettingsActivity.this, "Profile successfully updated", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(SettingsActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToMain() {
        Intent settingIntent = new Intent(SettingsActivity.this, MainActivity.class);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }


}