package com.stano.homeapp.services;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import androidx.annotation.NonNull;

public class FetchAsync extends AsyncTask<Void, Void, DatabaseReference> {
    private static final String TAG = FetchAsync.class.getSimpleName() + " :: ";
    private DatabaseReference databaseReference;
    private ProgressDialog loader;
    private float rate;
    private Context context;
    public FetchAsync(){
        
    }

    @Override
    protected DatabaseReference doInBackground(Void... voids) {
        databaseReference.setValue(rate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Task" +
                            "<DataSnapshot> " +
                            "task. " +
                            "isSuccessful");
                    loader.dismiss();
                    ((Activity)(context)).finish();
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
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public FetchAsync(Context context, float rate, DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
        this.rate = rate;
        this.context = context;
        loader = new ProgressDialog(context);
        loader.setMessage("Wait ...");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
    }

    @Override
    protected void onPostExecute(DatabaseReference databaseReference) {
        super.onPostExecute(databaseReference);
        loader.dismiss();
    }

}
