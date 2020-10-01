package com.example.tourism_cif;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FloatingActionButton floatingActionButton;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, postRef;

    private TextView NavProfileUserName;

    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.add_post_button);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserID = mAuth.getCurrentUser().getUid();

        //for drawer_layout, menu-thingy
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("FYPTourism");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //Home layout, displaying post
        postList = (RecyclerView) findViewById(R.id.users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //display new post at top
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        postList.setLayoutManager(linearLayoutManager);


        //include nav_header into drawer_menu
        View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_username);

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Username")){
                        String username = dataSnapshot.child("Username").getValue().toString();
                        NavProfileUserName.setText(username);
                    }else{
                        Toast.makeText(MainActivity.this, "Profile name does not exist", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });
        //menu-thingy ends

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendToPostActivity();
            }
        });

        DisplayUsersPost();
    }

    private void DisplayUsersPost() {
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postRef, Posts.class).build();
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder postsViewHolder, int i, @NonNull Posts posts) {

                //i-position. to get the position of the clicked post
                final String postKey = getRef(i).getKey();

                postsViewHolder.userName.setText(posts.getUname());
                postsViewHolder.postDate.setText(posts.getDate());
                postsViewHolder.postTime.setText(posts.getTime());
                postsViewHolder.postStatus.setText(posts.getStatus());
                postsViewHolder.setPostImage(getApplicationContext(), posts.getPostImage());
                //Picasso.get().load(posts.getPostImage()).fit().centerCrop().into(postsViewHolder.postImage);

                postsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("postKey", postKey);
                        startActivity(clickPostIntent);
                    }
                });
            }

            @NonNull
            @Override
            //connecting home_layout to PostViewHolder class
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_layout, parent, false);
                //PostsViewHolder viewHolder = new PostsViewHolder(view);
                return new PostsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, postDate, postTime, postStatus;
        ImageView postImage;
        View mView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            userName = itemView.findViewById(R.id.post_user_name);
            postDate = itemView.findViewById(R.id.post_date);
            postTime = itemView.findViewById(R.id.post_time);
            postStatus = itemView.findViewById(R.id.post_status);
        }

        public void setPostImage(Context ctx, String postimage) {
            ImageView postImage = (ImageView) itemView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(postImage);
        }
    }

    //click hamburger to open drawer_layout
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //whenever app runs, this method will be called automatically
    //to check user authentication
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        //not authenticated
        if (currentUser == null) {
            SendToLogin();
        } else {
            checkUserInDatabase();
        }
    }

    private void checkUserInDatabase() {
        final String currentUserID = mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if unique current user id not in database
                //user is authenticated but not exist in database, so send to Setup
                if (!dataSnapshot.hasChild(currentUserID)) {
                    sendUsertoSetup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendUsertoSetup() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_explore:
                Toast.makeText(this, "Explore", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_fav:
                Toast.makeText(this, "Favourite", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_trip:
                Toast.makeText(this, "Trip", Toast.LENGTH_SHORT).show();
                break;

                //profile settings
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                //SendToPostActivity();
                SendToSettingActivity();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendToLogin();
                break;
        }
    }

    private void SendToSettingActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

}
