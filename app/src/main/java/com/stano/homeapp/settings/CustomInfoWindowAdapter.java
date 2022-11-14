package com.stano.homeapp.settings;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.stano.homeapp.R;
import com.stano.homeapp.data.Employee;
import com.stano.homeapp.data.Employer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static final String TAG =
            "TAG :: " + CustomInfoWindowAdapter.class.getSimpleName() + " :: ";
    private final Activity activity;
    private TextView phone;
    private TextView skill;
    private RatingBar ratingBar;

    public CustomInfoWindowAdapter(Activity activity) {
        Log.d(TAG, "getInfoWindow");
        this.activity = activity;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        Gson gson = new Gson();
        View v;
        Employee employee = gson.fromJson(marker.getSnippet(), Employee.class);

        if (employee != null) {
             v = activity.getLayoutInflater().inflate(R.layout.employee_map_custom_info, null);
            TextView name = v.findViewById(R.id.name);
            phone = v.findViewById(R.id.phone);
            skill = v.findViewById(R.id.skill);
            TextView service = v.findViewById(R.id.service);
            ratingBar = v.findViewById(R.id.rating_bar);
            name.setText(employee.getName());
            skill.setText(employee.getSkill());
            service.setText(employee.getService());
            phone.setText(employee.getPhone());
            ratingBar.setRating(Float.parseFloat(employee.getRatings()));
            Log.d(TAG, "getInfoContents :getRating: " +ratingBar.getRating());
            Log.d(TAG, "getInfoContents :: " +ratingBar.getNumStars());
        } else {
//            Employer employer = gson.fromJson(marker.getSnippet(), Employer.class);
//            v = activity.getLayoutInflater().inflate(R.layout.employer_map_custom_info, null);
//            name = v.findViewById(R.id.name);
//            phone = v.findViewById(R.id.phone);
//            ratingBar = v.findViewById(R.id.rating_bar);
//            name.setText(employer.getName());
//            phone.setText(employer.getPhone());
//            String rating = employer.getRatings();
//            if (rating == null) rating = "0";
//            ratingBar.setRating(Float.parseFloat(rating));
            return null;
        }

        return v;
    }
}
