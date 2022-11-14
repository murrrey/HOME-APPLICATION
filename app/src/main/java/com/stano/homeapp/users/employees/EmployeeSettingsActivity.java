package com.stano.homeapp.users.employees;

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
import android.widget.RatingBar;
import android.widget.TextView;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stano.homeapp.R;
import com.stano.homeapp.settings.HomeApp_Settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class EmployeeSettingsActivity extends AppCompatActivity {
    private static final String TAG =
            "TAG :: " + EmployeeSettingsActivity.class.getSimpleName() + " :: ";
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 45654;
    private static boolean WRITE_PERMISSION_GRANTED = false;
    private final int PICK_IMAGE_REQUEST = 1;
    private final ValueEventListener ratingsListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            int rate = 0;
            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                for (DataSnapshot next : dataSnapshot.getChildren()) {
                    Log.d(TAG, "ratingsListener :: getKey   = " + next.getKey());
                    Log.d(TAG, "ratingsListener :: getValue = " + next.getValue());
                    if(Objects.equals(next.getKey(), "rate")){
                        rate += Integer.parseInt(next.getValue().toString());
                    }
                }
            }
            ratings.setText(String.valueOf(rate));
            if(rate==0){
                ratings.setVisibility(View.GONE);
            }else{
                if(rate<2.5){
                    ratings.setBackground(getResources().getDrawable(R.drawable.ratings_red));
                }else{
                    ratings.setBackground(getResources().getDrawable(R.drawable.ratings_blue));
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private ProgressDialog loader;
    private DatabaseReference userDatabaseRef, userNotifiedRef;
    private ImageView profileImage;
    private EditText name, phone, skill, email;
    private TextView ratings;
    private RadioGroup radioGroup;
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
                if (skill.getText().toString().isEmpty())
                    if (map.get("skill") != null) {
                        mSkill = map.get("skill").toString();
                        skill.setText(mSkill);
                    }
                if (map.get("service") != null) {
                    mService = map.get("service").toString();
                    switch (mService) {
                        case "Maintenance and Repair":
                            radioGroup.check(R.id.maintenance);
                            break;
                        case "Freelance":
                            radioGroup.check(R.id.freelance);
                            break;
                    }
                }

                if (map.get("profileImageUrl") != null) {
                    mProfileImageUrl = map.get("profileImageUrl").toString();
                    Glide.with(getApplication()).load(mProfileImageUrl).into(profileImage);
                } else {
                    Glide.with(getApplication()).load(currentUser.getPhotoUrl()).into(profileImage);
                }
                loadRatings();
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

    private void loadRatings() {
        Query query = FirebaseDatabase
                .getInstance()
                .getReference("Ratings")
                .child(currentUser.getUid()).orderByKey();
        query.addListenerForSingleValueEvent(ratingsListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_settings);
        Log.d(TAG, "onCreate ");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        loader = new ProgressDialog(this);
        name = (EditText) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        skill = (EditText) findViewById(R.id.skill);
        email = (EditText) findViewById(R.id.email);
        ratings = findViewById(R.id.employee_ratebar);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        Button confirm = (Button) findViewById(R.id.confirm);
        Button cancel = (Button) findViewById(R.id.cancel);
        userDatabaseRef =
                FirebaseDatabase.getInstance().getReference("Users").child(
                        "Employee").child(currentUserId);
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //store info of current user
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    EmployeeSettingsActivity.this.setTitle("Profile : " + user.getDisplayName().toUpperCase());
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
                        Log.d(TAG, " Bitmap bitmap :: ", e.getCause());
                        e.printStackTrace();

                    }
                } else {
                    Log.d(TAG, "firebaseAuthListener :: onAuthStateChanged ");
                    firebaseAuth.signOut();
                    Intent intent = new Intent(EmployeeSettingsActivity.this,
                            EmployeeLoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
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

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getReadWritePermission();
        getUserInfo();
    }


    private void getUserInfo() {
        this.setTitle("Profile : " + currentUser.getDisplayName());

        userDatabaseRef.addValueEventListener(detailsValueListener);

    }

    private void saveUserInformation() {
        mName = name.getText().toString();
        mPhone = phone.getText().toString();
        mSkill = skill.getText().toString();
        int selectId = radioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = findViewById(selectId);

        if (mSkill.isEmpty()) {
            skill.setError("Skill required");
            return;
        }
        if (radioButton == null) {
            HomeApp_Settings.showAlert(this, "Select Category", "FreeLance or Repairs");
            return;
        }
        if (filePath == null) {
            HomeApp_Settings.showAlert(this, "Image Required", "Select profile Image");
            return;
        }
        loader.setMessage("Saving in progress...");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("service", radioButton.getText());
        userInfo.put("skill", mSkill);
//        userInfo.put("profileImageUrl", filePath);
        userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG,
                            "User updateChildren updated." + task.getResult());
                    Log.d(TAG, "User profile updated. isSuccessful");
                    finish();
                } else {
                    Log.e(TAG, task.getException().getMessage(),
                            task.getException());
                }
            }
        });
        if (filePath != null) {
            // Code for showing progressDialog while uploading

            FirebaseUser user = mAuth.getCurrentUser();
            Log.d(TAG, "User profile Uploading...");
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(filePath)
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                                Log.d(TAG, "User profile updated. " + task.getResult());
                                loader.dismiss();
                            } else {
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

    private void getReadWritePermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
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
            } catch (IOException e) {
                // Log the exception
                Log.d(TAG, " onActivityResult bitmap :: ", e.getCause());
                e.printStackTrace();
            }
        } else Log.d(TAG, "onActivityResult :: not PICK_IMAGE_REQUEST");
    }
}