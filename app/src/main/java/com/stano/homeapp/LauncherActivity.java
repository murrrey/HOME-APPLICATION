package com.stano.homeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.stano.homeapp.settings.HomeApp_Settings;
import com.stano.homeapp.users.employees.EmployeeLoginActivity;
import com.stano.homeapp.users.employers.EmployerLoginActivity;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "TAG :: " + LauncherActivity.class.getSimpleName() + " :: ";
    private Button employee_btn, employer_btn;
    private Boolean connected, allowed,location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        HomeApp_Settings settings =
                new HomeApp_Settings(this);
        Log.d(TAG, "onCreate");
        location = settings.isLocationEnabled(this);
        settings.checkLocationPermission(this);
        settings.checkMap(this);
        connected = settings.checkInternet(this);
        allowed = settings.checkPermissionAllowed(this.getApplicationContext());

        employee_btn = findViewById(R.id.employee_btn);
        employer_btn = findViewById(R.id.employer_btn);
        if(!location){
            HomeApp_Settings.showAlert(this, "Error!!!","Turn On Location");
        }
        if(!allowed){
            HomeApp_Settings.showAlert(this, "Error!!!","No Internet Connection");
        }
        employee_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LauncherActivity.this, EmployeeLoginActivity.class));
            }
        });
        employer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LauncherActivity.this, EmployerLoginActivity.class));
            }
        });

    }


}