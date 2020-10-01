package com.example.tourism_cif;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    //Coding Cafe's
    private Toolbar mToolbar;

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

        mToolbar = (Toolbar) findViewById(R.id.add_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Post");

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
        if(imageUri == null){
            Toast.makeText(this, "Please choose image", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
        } else{
            saveImagetoFirebaseStorage();
        }
        /*if(imageUri != null){
            saveImagetoFirebaseStorage();
        }
        if(!TextUtils.isEmpty(status)){
            savePostInfoToDatabase();
        }
        else{
            Toast.makeText(this, "Upload an image or Write something...", Toast.LENGTH_SHORT).show();
        }*/
    }

    private void saveImagetoFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        final StorageReference filePath = postImageRef.child("Posts").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");


        //filePath.putFile((imageUri)-store images in storage. Others is to tell user success/not
        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadUrl = uri.toString();
                        Toast.makeText(PostActivity.this, "Post uploaded", Toast.LENGTH_SHORT).show();

                        savePostInfoToDatabase();
                    }
                });
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
        startActivity(mainIntent);
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
            //show the selected image at the addImageBtn
            imageUri = data.getData();
            addImageBtn.setImageURI(imageUri);
        }
    }

    //back button from toolbar
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home){
            sendToMain();
        }
        return super.onOptionsItemSelected(item);
    }


}