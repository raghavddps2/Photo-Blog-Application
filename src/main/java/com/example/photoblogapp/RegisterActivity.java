package com.example.photoblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class RegisterActivity extends AppCompatActivity {


    private EditText reg_email_field;
    private EditText reg_pass_field;
    private EditText reg_confirm_pass_field;
    private Button reg_login_button;
    private Button reg_btn;
    private ProgressBar reg_progress;


    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        reg_email_field = (EditText) findViewById(R.id.reg_email);
        reg_pass_field = (EditText) findViewById(R.id.reg_pass);
        reg_confirm_pass_field = (EditText) findViewById(R.id.reg_confirm_pass);
        reg_login_button = (Button) findViewById(R.id.reg_login_btn);
        reg_btn = (Button) findViewById(R.id.reg_btn);
        reg_progress = (ProgressBar) findViewById(R.id.reg_progress);



        reg_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = reg_email_field.getText().toString();
                String password = reg_pass_field.getText().toString();
                String confirmPassword = reg_confirm_pass_field.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)){

                    if(password.equals(confirmPassword)){
                        reg_progress.setVisibility(view.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    Intent setupintent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupintent);
                                    finish();
                                }
                                else{
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error: "+errorMessage,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(RegisterActivity.this,"Note: Passwords do not match",Toast.LENGTH_SHORT).show();
                    }

                    reg_progress.setVisibility(view.INVISIBLE);
                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }
    public void sendToMain(){
        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
