package com.stano.homeapp.services;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.stano.homeapp.users.employers.EmployeeRateActivity;

import java.util.HashMap;

import androidx.annotation.NonNull;

public class RateAsync extends AsyncTask<Void, Void, DatabaseReference> {
    private static final String TAG = FetchAsync.class.getSimpleName() + " :: ";
    private DatabaseReference databaseReference;
    private ProgressDialog loader;
    private HashMap<String, Object> userInfo;
    private EmployeeRateActivity employeeRateActivity;
    public RateAsync(EmployeeRateActivity employeeRateActivity, HashMap<String, Object> userInfo, DatabaseReference databaseReference) {
        this.employeeRateActivity = employeeRateActivity;
        this.databaseReference = databaseReference;
        this.userInfo = userInfo;
    }

    @Override
    protected DatabaseReference doInBackground(Void... voids) {
        databaseReference.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Task" +
                            "<DataSnapshot> " +
                            "task. " +
                            "isSuccessful");
//                    loader.dismiss();
                    employeeRateActivity.finish();
                }else{
                    Log.d(TAG, "Task" +
                            "<DataSnapshot> " +
                            "task. " +
                            "not Success");
                }
            }
        });
        return null;
    }

    @Override
    protected void onPostExecute(DatabaseReference databaseReference) {
        super.onPostExecute(databaseReference);
    }
}
