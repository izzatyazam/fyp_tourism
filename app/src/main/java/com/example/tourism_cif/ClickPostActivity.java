package com.example.tourism_cif;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView postImage;
    private TextView postStatus;
    private Button editPostBtn, deletePostBtn;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private String postKey, currentUserID, dbUserID, status, image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //get the post activity from main activity
        postKey = getIntent().getExtras().get("postKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        postImage = (ImageView) findViewById(R.id.click_post_image);
        postStatus = (TextView) findViewById(R.id.click_post_status);
        editPostBtn = (Button) findViewById(R.id.edit_post_button);
        deletePostBtn = (Button) findViewById(R.id.delete_post_button);

        deletePostBtn.setVisibility(View.INVISIBLE);
        editPostBtn.setVisibility(View.INVISIBLE);


        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    status = dataSnapshot.child("status").getValue().toString();
                    image = dataSnapshot.child("postImage").getValue().toString();
                    dbUserID = dataSnapshot.child("uid").getValue().toString();

                    postStatus.setText(status);
                    Picasso.get().load(image).into(postImage);

                    //check if the post clicked is user's not other user
                    if(currentUserID.equals(dbUserID)){
                        deletePostBtn.setVisibility(View.VISIBLE);
                        editPostBtn.setVisibility(View.VISIBLE);

                    }

                    editPostBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(status);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deletePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });
    }

    private void EditCurrentPost(String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post:");

        final EditText inputField = new EditText(ClickPostActivity.this);
        //display the post user wants to edit
        inputField.setText(status);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("status").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post updated!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void DeleteCurrentPost(){
        //delete post from firebase
        clickPostRef.removeValue();
        sendToMain();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();

    }

    private void sendToMain() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}