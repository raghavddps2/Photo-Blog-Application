package com.example.photoblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginButton;
    private Button signUpButton;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth  = FirebaseAuth.getInstance();
        loginEmailText = (EditText) findViewById(R.id.email);
        loginPassText = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_button);
        signUpButton = (Button) findViewById(R.id.login_reg_button);
        loginProgress = (ProgressBar) findViewById(R.id.login_progress);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


        //On the click of the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String email = loginEmailText.getText().toString();
                String password = loginPassText.getText().toString();

                //We have the TextUtils to check the null conditions.
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    //Proceed.
                    //This will set it as visible.
                    loginProgress.setVisibility(View.VISIBLE);
                    //We will set an onClick LIstener here.
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //This will be when the sign in is complete
                            //We have the variable task woth us.
                            if(task.isSuccessful()){
                                Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                                //We just redirect to the MainActivity.
                            }
                            else{
                                //We will get what the exception was.
                                String error = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error: "+ error,Toast.LENGTH_SHORT).show();
                                //The above will show the required toast.
                            }
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //If the user is logged in.
            Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
        else{

        }
    }
}
