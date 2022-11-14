package com.stano.homeapp.users.employers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stano.homeapp.R;
import com.stano.homeapp.settings.HomeApp_Settings;
import com.stano.homeapp.users.employees.EmployeeLoginActivity;
import com.stano.homeapp.users.employees.EmployeeSettingsActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmployerSettingsActivity extends AppCompatActivity {
    private static final String TAG =
            "TAG :: " + EmployerSettingsActivity.class.getSimpleName() + " :: ";
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 456654;
    private static boolean WRITE_PERMISSION_GRANTED =false;
    private final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private ProgressDialog loader;
    private DatabaseReference userDatabaseRef;
    private ImageView profileImage;
    private EditText name, phone, skill, email;
    private RadioGroup radioGroup;
    private Button confirm_btn;
    private String mName;
    private String mPhone;
    private String mEmail;
    private String mSkill;
    private String mService;
    private String mProfileImageUrl;
    private final ValueEventListener detailsValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Log.d(TAG, "getUserInfo :: " + entry.getKey() + "/" + entry.getValue());
                }
                if (name.getText().toString().isEmpty())
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        name.setText(mName);
                    }
                if (phone.getText().toString().isEmpty())
                    if (map.get("phone") != null) {
                        mPhone = map.get("phone").toString();
                        phone.setText(mPhone);
                    }
                if (email.getText().toString().isEmpty())
                    if (map.get("email") != null) {
                        mEmail = map.get("email").toString();
                        email.setText(mEmail);
                    }

                if (map.get("profileImageUrl") != null) {
                    mProfileImageUrl = map.get("profileImageUrl").toString();
                    Glide.with(getApplication()).load(mProfileImageUrl).into(profileImage);
                } else {
                    Glide.with(getApplication()).load(currentUser.getPhotoUrl()).into(profileImage);
                }
            }
            loader.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            loader.dismiss();
        }
    };
    private Uri filePath;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");
        setContentView(R.layout.activity_employer_settings);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.d(TAG, "onCreate");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        loader = new ProgressDialog(this);
        name = (EditText) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        confirm_btn = (Button) findViewById(R.id.confirm);
        userDatabaseRef =
                FirebaseDatabase.getInstance().getReference("Users").child(
                        "Employer").child(currentUserId);
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //store info of current user
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    EmployerSettingsActivity.this.setTitle("Profile : " + user.getDisplayName());
                    mProfileImageUrl = user.getPhotoUrl().toString();
                    name.setText(user.getDisplayName());
                    email.setText(user.getEmail());
                    Log.d(TAG, "onAuthStateChanged");
                    try {
                        // Setting image on image view using Bitmap
                        Bitmap bitmap = MediaStore
                                .Images
                                .Media
                                .getBitmap(
                                        getContentResolver(),
                                        user.getPhotoUrl());
                        profileImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        // Log the exception
                        Log.d(TAG," Bitmap bitmap :: ", e.getCause());
                        e.printStackTrace();

                    }
                } else {
                    Log.d(TAG, "firebaseAuthListener :: onAuthStateChanged ");
                    firebaseAuth.signOut();
                    Intent intent = new Intent(EmployerSettingsActivity.this,
                            EmployeeLoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        if(profileImage.getDrawable() == null){
            confirm_btn.setText(R.string.image);
        }else{
            confirm_btn.setText(R.string.save);
        }
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(
                        intent,
                        "Select Image from here..."), PICK_IMAGE_REQUEST);
            }
        });

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "confirm_btn.setOnClickListener");
                Log.d(TAG, "confirm_btn " + confirm_btn.getText());
                if(confirm_btn.getText()==getResources().getString(R.string.image)){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(
                            intent,
                            "Select Image from here..."), PICK_IMAGE_REQUEST);
                }else if(confirm_btn.getText()==getResources().getString(R.string.back)){
                    finish();
                }else if(confirm_btn.getText()==getResources().getString(R.string.save)){
                    saveUserInformation();
                }
            }
        });
        getReadWritePermission();
        getUserInfo();
    }

    private void getUserInfo() {
        this.setTitle("Profile : " + currentUser.getDisplayName());
        userDatabaseRef.addValueEventListener(detailsValueListener);

    }


    private void getReadWritePermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            WRITE_PERMISSION_GRANTED = true;
            Log.d(TAG, "ReadWritePermissionGranted");

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_READ_EXTERNAL_STORAGE);
        }
    }

    private void saveUserInformation() {
        mName = name.getText().toString();
        mPhone = phone.getText().toString();
        if (filePath == null) {
            HomeApp_Settings.showAlert(this, "Image Required", "Select profile Image");
            return;
        }
        loader.setMessage("Saving in progress...");
        loader.setCanceledOnTouchOutside(false);
        loader.show();

        if (filePath != null) {
            // Code for showing progressDialog while uploading
            Log.d(TAG, "User profile Uploading...");
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(filePath)
                    .build();
            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                                Log.d(TAG, "User profile updated. " + task.getResult());
                                Toast.makeText(EmployerSettingsActivity.this, "User profile updated",
                                        Toast.LENGTH_LONG).show();
                                confirm_btn.setText(R.string.back);
                            }else{
                                Log.e(TAG, task.getException().getMessage(),
                                        task.getException());
                            }
                        }
                    });
        }
        loader.dismiss();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    WRITE_PERMISSION_GRANTED = true;
                    Log.d(TAG, "ReadWritePermissionGranted");
                }
            }
            default:
                Log.d(TAG, "onRequestPermissionsResult :: " + requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            Log.d(TAG, "onActivityResult :: PICK_IMAGE_REQUEST");

            // Get the Uri of data
            filePath = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                profileImage.setImageBitmap(bitmap);
                confirm_btn.setText(R.string.save);
            } catch (IOException e) {
                // Log the exception
                Log.d(TAG, " onActivityResult bitmap :: ", e.getCause());
                e.printStackTrace();
            }
        } else Log.d(TAG, "onActivityResult :: not PICK_IMAGE_REQUEST");
    }
}