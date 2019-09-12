package com.example.photoblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.admin.v1beta1.Progress;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.zip.InflaterInputStream;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 200;
    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private ProgressBar newProgressBar;
    private FirebaseAuth firebaseAuth;
    private EditText newPostText;
    private Uri postImageUri;
    private Button postButton;

    //compressor
    private Bitmap compressedImageFile;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String currUserId;
    private String currUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = (Toolbar)findViewById(R.id.add_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.new_post_img);
        newPostText = findViewById(R.id.postDes);
        postButton = findViewById(R.id.postButton);


        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        newProgressBar = findViewById(R.id.save_post_progress);
        newProgressBar.setVisibility(View.INVISIBLE);
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,2)
                        .start(NewPostActivity.this);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.i("Click","CLicked button");
                final String desc = newPostText.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null) {
                    newProgressBar.setVisibility(View.VISIBLE);
                    final String randomName = UUID.randomUUID().toString();

                    //Lets store the things to the firestore now.
                    Log.i("random","HIII");

                    //We store the image according to the time posted. jpg
                    final StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");

                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            final String downloadUri = task.getResult().getUploadSessionUri().toString();
                            if (task.isSuccessful()) {

                                //This is how we get the image_file.
                                //we have the File class.
                                File new_image_file = new File(postImageUri.getPath());

                                try {
                                    //We then ompress using this.
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(200).setMaxWidth(200).setQuality(10).compressToBitmap(new_image_file);

                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();


                                //We store the thumbnail into the thumbs folder.
                                UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName+".jpg").putBytes(thumbData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                        String thumbdownloadUri = taskSnapshot.getUploadSessionUri().toString();

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", downloadUri);
                                        postMap.put("thumbnial_url",thumbdownloadUri);
                                        postMap.put("desc", desc);
                                        postMap.put("user", currUserId);
                                        postMap.put("Timestamp", FieldValue.serverTimestamp());
                                        firebaseFirestore.collection("posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(NewPostActivity.this,"Post was added",Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(NewPostActivity.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish(); //This tells that the user should not be able to go back again.



                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this, "Firestore Error: " + error, Toast.LENGTH_SHORT).show();
                                                }
                                                newProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                        //If the task is successfull, we will do the stuff we want to do. If the task is not successfull
                                        //We will do our rest work.
                                        //This will upload the thumbnail as well.
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //If the task is not successfull, we will do.
                                        Toast.makeText(NewPostActivity.this,"Error:",Toast.LENGTH_SHORT).show();
                                    }
                                });


                            } else {
                                newProgressBar.setVisibility(View.INVISIBLE);
                                String error = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }


    //When the user clicks on the add post button, the data should be sent ot the firebase then.




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                //This will then set the image.
                newPostImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    //This was the function to generate the random string.
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
