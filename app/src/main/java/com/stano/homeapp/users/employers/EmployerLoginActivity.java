package com.stano.homeapp.users.employers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.stano.homeapp.R;
import com.stano.homeapp.settings.HomeApp_Settings;
import com.stano.homeapp.users.employees.EmployeeLoginActivity;
import com.stano.homeapp.users.employees.EmployeeMapActivity;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EmployerLoginActivity extends AppCompatActivity {
    private static final String TAG = "TAG :: " + EmployerLoginActivity.class.getSimpleName() + " " +
            ":: ";
    private EditText email, password;
    private ProgressDialog loader;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");

        setContentView(R.layout.activity_employer_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setTitle(getResources().getString(R.string.employer_login_title));
        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.email_et);
        password = (EditText) findViewById(R.id.password_et);
        loader = new ProgressDialog(this);
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //store info of current user
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    loader.setMessage("Login in progress...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    FirebaseDatabase.getInstance().getReference("Users").child("Employer")
                            .child(user.getUid())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().exists()) {
                                            Log.d(TAG, "firebaseAuthListener.getReference() :: isSuccessful" );
                                            Intent intent = new Intent(EmployerLoginActivity.this
                                                    , EmployerMapActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else Log.d(TAG, "firebaseAuthListener.getReference() :: " +
                                                "NOT eeeeeeeeer");
                                    } else {
                                        if (task.getException() != null) {
                                            Log.d(TAG,
                                                    "firebaseAuthListener.getReference() :: NOT " +
                                                            "isSuccessful " + task.getException().getMessage());
//                                            if(Objects.equals(task.getException().getMessage(), "Client is offline")){
//                                                HomeApp_Settings.showAlert(EmployerLoginActivity.this, "Connection Error","You are " +
//                                                        "offline");
//                                            }
                                        }
                                        else Log.d(TAG, "firebaseAuthListener.getReference() :: " +
                                                "NOT isSuccessful");
                                    }
                                }
                            });
                    loader.dismiss();
                }

            }
        };
//        mAuth.addAuthStateListener(firebaseAuthListener);



        Button login = (Button) findViewById(R.id.login_btn);
        Button registration = (Button) findViewById(R.id.registration_btn);
        TextView as_an_employee = findViewById(R.id.as_an_employee);
        as_an_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmployerLoginActivity.this,
                        EmployeeLoginActivity.class));
            }
        });
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmployerLoginActivity.this,
                        EmployerRegistrationActivity.class));
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        final String email_txt = email.getText().toString();
        final String password_txt = password.getText().toString();
        if (TextUtils.isEmpty(email_txt)) {
            email.setError("Email Required!");
            return;
        }
        if (TextUtils.isEmpty(password_txt)) {
            password.setError("Password Required!");
            return;
        }
        loader.setMessage("Login in progress...");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
        mAuth.signInWithEmailAndPassword(email_txt, password_txt)
                .addOnCompleteListener(EmployerLoginActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                loader.dismiss();
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Login successful." );
                                    Toast.makeText(getApplicationContext(), "Login successful. ", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(EmployerLoginActivity.this
                                            , EmployerMapActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else
                                    Toast.makeText(EmployerLoginActivity.this, "Incorrect email or password.", Toast.LENGTH_LONG).show();
                            }
                        })
                .addOnFailureListener(EmployerLoginActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loader.dismiss();
                        Log.e(TAG, e.getMessage(), e);
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);

    }
}