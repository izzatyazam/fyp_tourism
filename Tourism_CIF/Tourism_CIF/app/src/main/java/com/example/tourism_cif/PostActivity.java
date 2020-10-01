package com.example.tourism_cif;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    FloatingActionButton addPost;
    //Coding Cafe's
    private ImageButton addImageBtn;
    private Button addPostBtn;
    private EditText postStatus;
    private static final int Gallery_Pick = 1;
    private Uri imageUri;
    private String status;

    private StorageReference postImageRef;
    private DatabaseReference userRef, postRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postImageRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        addImageBtn = (ImageButton) findViewById(R.id.add_image_button);
        addPostBtn = (Button) findViewById(R.id.button_upload);
        postStatus = (EditText) findViewById(R.id.write_post);

        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {
        status = postStatus.getText().toString();
        if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show();            
        } else{
            saveImagetoFirebaseStorage();
        }
    }

    private void saveImagetoFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        StorageReference filePath = postImageRef.child("Uploads").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

        //filePath.putFile((imageUri)-store images in storage. Others is to tell user success/not
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    //get the link of image from firebase storage and save it under downloadUrl variable
                    downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Successfully posted", Toast.LENGTH_SHORT).show();

                    savePostInfoToDatabase();

                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured: ", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void savePostInfoToDatabase() {
        //retrieve user's username from Users node. And store it inside Posts node to store post information (foreign key)
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check if child exist
                if(dataSnapshot.exists()){
                    //.child(--) --must be same as in database
                    String userName = dataSnapshot.child("Username").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", currentUserID);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("status", postStatus.getText().toString());
                    postMap.put("postImage", downloadUrl);
                    postMap.put("uname", userName);

                    //security purpose. ID+already unique random name
                    postRef.child(postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        sendToMain();
                                        Toast.makeText(PostActivity.this, "Post is updated", Toast.LENGTH_SHORT).show();
                                    } else{
                                        Toast.makeText(PostActivity.this, "Error occured while updating your post", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== Gallery_Pick && resultCode==RESULT_OK && data!=null){
            imageUri = data.getData();
            addImageBtn.setImageURI(imageUri);
        }
    }


}