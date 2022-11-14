package com.stano.homeapp.users.employers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stano.homeapp.R;
import com.stano.homeapp.data.Employee;
import com.stano.homeapp.services.FetchAsync;
import com.stano.homeapp.services.FetchEmployeeAsync;
import com.stano.homeapp.services.RateAsync;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EmployeeRateActivity extends AppCompatActivity {

    private static final String TAG =
            "TAG :: " + EmployeeRateActivity.class.getSimpleName() + " :: ";
    private DatabaseReference employee;
    private TextView name, skill, service, phone;
    private RatingBar ratingBar;
    private Button confirm_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_rate);

        Log.d(TAG, "onCreate ");
        String employee_id = getIntent().getStringExtra("employee");
        String employer_id = getIntent().getStringExtra("employer");
        Log.d(TAG, "onCreate " + employee_id + " :: "+ employer_id);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        name = findViewById(R.id.name);
        skill = findViewById(R.id.skill);
        service = findViewById(R.id.service);
        phone = findViewById(R.id.phone);
        ratingBar = findViewById(R.id.rating_bar);
        confirm_btn = findViewById(R.id.confirm);
        employee = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child("Employee")
                .child(employee_id);
        confirm_btn.setEnabled(false);
        FetchEmployeeAsync fetchEmployeeAsync = new FetchEmployeeAsync(this, name, phone, skill,
                service,
                ratingBar, confirm_btn, employee);
       fetchEmployeeAsync.execute();
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rate = ratingBar.getRating();
                DatabaseReference employee_rate = FirebaseDatabase
                        .getInstance()
                        .getReference("Ratings")
                        .child(employee_id);
                HashMap<String, Object> userInfo = new HashMap<>();
                userInfo.put("employer", employer_id);
                userInfo.put("rate", rate);
                RateAsync fetchAsync = new RateAsync(EmployeeRateActivity.this, userInfo, employee_rate);

                fetchAsync.execute();
            }
        });
    }
}