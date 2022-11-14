package com.stano.homeapp.users.employees;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.stano.homeapp.R;

public class EmployeeWorkView extends AppCompatActivity {
    private static final String TAG =
            "TAG :: " + EmployeeWorkView.class.getSimpleName() + " :: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");

        setContentView(R.layout.activity_employee_work_view);
    }
}