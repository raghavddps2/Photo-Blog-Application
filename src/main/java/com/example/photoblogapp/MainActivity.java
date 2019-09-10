package com.example.photoblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private Toolbar main_toolbar;
    private FloatingActionButton addPostBtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        addPostBtn = findViewById(R.id.add_post);
        getSupportActionBar().setTitle("Photo Blog");

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,NewPostActivity.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }



    @Override
    protected void onStart() {
        super.onStart();

        //We get the current user.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            //User is currently signed in.
        }
        else{
            //User is not logged in.
             //Here, in this activity our work is over.
            //We direct the user to the login apge.
            sendToLogin();
        }
    }

    public void sendToLogin(){
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //we will pass our menu file.
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    public void logOut(){
        //We simply call the sign out function on the FirebaseAuth.
        mAuth.signOut();
        sendToLogin();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_logout:
                logOut(); //calling the method.
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                finish();
            default:
                return false;
        }

    }
}
