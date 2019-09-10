package com.example.photoblogapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = (Toolbar)findViewById(R.id.add_post_toolbar);
    }
}
