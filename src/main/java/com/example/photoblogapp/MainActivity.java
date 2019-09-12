package com.example.photoblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Toolbar main_toolbar;
    private FloatingActionButton addPostBtn;

    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;

    private String current_user_id;

    //We are initializing the fragments.
    private FragmentHome fragmentHome;
    private FragmentNotification fragmentNotification;
    private FragmentAccount fragmentAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = FirebaseAuth.getInstance().getUid();

        mAuth = FirebaseAuth.getInstance();
        main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
        addPostBtn = findViewById(R.id.add_post);

        fragmentAccount = new FragmentAccount();
        fragmentHome = new FragmentHome();
        fragmentNotification = new FragmentNotification();
//        getSupportActionBar().setTitle("Photo Blog");



        bottomNavigationView = findViewById(R.id.mainBottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch(menuItem.getItemId()){
                    case R.id.bottom_home:
                        replaceFragement(fragmentHome);
                        return true;
                    case R.id.bottom_account:
                        replaceFragement(fragmentAccount);
                        return true;
                    case R.id.bottom_notification:
                        replaceFragement(fragmentNotification);
                        return true;

                }
                return false;
            }
        });

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,NewPostActivity.class);
                startActivity(intent);
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToLogin();
        }
        else{
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            //We will send it to the setup activity.
                            Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    else{
                        String errorMsg = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error: "+errorMsg,Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

    private void replaceFragement(Fragment fragment){

        //This is used to change the fragments.
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //The above will start the transaction.
        //The below will simply replace the fragment.
        fragmentTransaction.replace(R.id.main_container,fragment); //This will replace the container with the said fragment.
        fragmentTransaction.commit(); //This is like done.
    }

}
