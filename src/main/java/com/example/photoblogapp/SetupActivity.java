package com.example.photoblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {


    private CircleImageView setup_image; //Image to set as profile picture.
    private Uri mainImageUri;

    private StorageReference mStorageReference;
    private FirebaseAuth firebaseAuth;//As we will need the user id as well.
    //We want a reference to the storage.
    private EditText setup_name;
    private Button setup_button;
    private String user_id;
    private FirebaseFirestore db;
    private ProgressBar setup_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        final Toolbar setupToolbar = (Toolbar) findViewById(R.id.setup_toolbar);
        setSupportActionBar(setupToolbar);

        getSupportActionBar().setTitle("Account Settings");


        firebaseAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();

        setup_name = findViewById(R.id.setup_name);
        setup_button = (Button) findViewById(R.id.setup_button);

        setup_progress = findViewById(R.id.setup_progress);

        setup_image = findViewById(R.id.setup_image);
        setup_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //If user si running marshmallow or greater version
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED){
                            //We need to ask the permission.
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        //We can now request permission.
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{
                        //Toast.makeText(SetupActivity.this,"Permission Granted",Toast.LENGTH_SHORT).show();
                        //This will send the user to crop the image.
                        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(SetupActivity.this);
                    }
                }

                else{
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(SetupActivity.this);
                }
            }
        });

    db.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

            if(task.isSuccessful()){

                //First will check if the things exist.
                if(task.getResult().exists()){
                    Toast.makeText(SetupActivity.this,"Data exists",Toast.LENGTH_SHORT).show();

                    //If exists we have to retrieve the data and this is how we get the data.
                    String name = task.getResult().getString("name");
                    String image = task.getResult().getString("image");

                    mainImageUri = Uri.parse(image);
                    setup_name.setText(name);

                    //THe below will set the default image.
                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.default_image);

                    //We need the glide library to sett the image, this will directly do what we want to do.
                    Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setup_image);
                    setup_progress.setVisibility(View.INVISIBLE);
                }
                else{
                    Toast.makeText(SetupActivity.this,"Data does not exists",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                String error = task.getException().getMessage();
                Toast.makeText(SetupActivity.this,"Firestore error: "+error,Toast.LENGTH_SHORT).show();
            }
        }
    });


        //Now, when the person clicks on the setup button, we can put a listener there.
        setup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Now, we need to send the image to the firebase storage and the image to the filestore
                //we need to get the user id as well.

                final String user_name = setup_name.getText().toString();
                //we need the mainImageUri as well.
                if(!TextUtils.isEmpty(user_name) && mainImageUri != null){

                    //We need to store the image to farebase storage and the name to filestore.
                    setup_progress.setVisibility(View.VISIBLE);

                    final String user_id = firebaseAuth.getCurrentUser().getUid();
                    final StorageReference image_path = mStorageReference.child("profile_images").child(user_id+".jpg");
                    //Now, we even need to put the image uri.
                    image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){
                                image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                     @Override
                                                                                     public void onSuccess(Uri uri) {

                                                                                         String download_url = uri.toString();


                                                                                         Map<String ,String > userMap = new HashMap<>();
                                                                                         userMap.put("name",user_name);
                                                                                         userMap.put("image",download_url);

                                                                                         //Each users detail will be stored in the document that is uniquely identified by that user.
                                                                                         db.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                             @Override
                                                                                             public void onComplete(@NonNull Task<Void> task) {

                                                                                                 //If the task is successfull, we are gonna do something, otherwise we will do something else.
                                                                                                 if(task.isSuccessful()){
                                                                                                     Toast.makeText(SetupActivity.this,"Image upload success",Toast.LENGTH_SHORT).show();
                                                                                                     Intent intent = new Intent(SetupActivity.this,MainActivity.class);
                                                                                                     startActivity(intent);
                                                                                                     finish();
                                                                                                 }
                                                                                                 else{
                                                                                                     String error = task.getException().getMessage();
                                                                                                     Toast.makeText(SetupActivity.this,"Firestore Error: "+error,Toast.LENGTH_SHORT).show();
                                                                                                 }

                                                                                             }
                                                                                         });
                                                                                     }
                                                                                 });
                                //We can pass object, so what we are gonna do is, we will create a map.

//                                Toast.makeText(SetupActivity.this,"The image is uploaded",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this,"Error: "+error,Toast.LENGTH_SHORT).show();
                            }

                            setup_progress.setVisibility(View.INVISIBLE);
                        }
                    });


                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setup_image.setImageURI(mainImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
