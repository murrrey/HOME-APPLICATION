package com.stano.homeapp.services;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.stano.homeapp.data.Employee;
import com.stano.homeapp.data.User;
import com.stano.homeapp.users.employers.EmployerMapActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;

public class SendSms extends AsyncTask<User, Void, String> {
    private static final String TAG =
            "TAG :: " + SendSms.class.getSimpleName() + " :: ";
    private final String employer_name;
    private final Activity activity;
    private  ProgressDialog progressDialog;
    private URLConnection conn;
    private  String time;
    private final String employee_id;
    private final String employer_id;
    private final LatLng pickupLocation;
    private final DatabaseReference  employees_notified;
    private Marker marker;


    public SendSms(Activity activity, String employer_name, String employer_id, String employee_id,
                   LatLng pickupLocation, DatabaseReference employees_notified, Marker marker) {
        this.employer_name = employer_name.toUpperCase();
        this.employer_id = employer_id;
        this.employee_id = employee_id;
        this.activity = activity;
        this.pickupLocation = pickupLocation;
        this.employees_notified = employees_notified;
        this.marker = marker;
        try {
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
            if (timeOfDay < 12) {
                time = "Good Morning ";
            } else if (timeOfDay < 16) {
                time = "Good Afternoon ";
            } else time = "Good Evening ";
        } catch (Exception ignore) {
            time = "Hello ";
        }
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute :: ");
        // display a progress dialog for good user experiance
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(User... users) {
        Employee employee = (Employee) users[0];
        Log.d(TAG, "doInBackground :: " + employee.getPhone());
        String employee_name = employee.getName().toUpperCase();
        String employee_phone = employee.getPhone();
        if ('0' == employee_phone.charAt(0)) {
            employee_phone = employee_phone.replaceFirst("0", "254");
        }
        try {
            String message = time + employee_name + " you have been selected for a job at " +
                    "HomeApp, " + " by " + employer_name + " " +
                    "kindly " +
                    "log in " +
                    "to your account.";
            String link = "http://techsultsms.co.ke/sms/api?";
            String data = URLEncoder.encode("action", "UTF-8") + "=" +
                    URLEncoder.encode("send-sms", "UTF-8");
            data += "&" + URLEncoder.encode("api_key", "UTF-8") + "=" +
                    URLEncoder.encode("QnJpYW46QnJpYW5QQHNz", "UTF-8");
            data += "&" + URLEncoder.encode("to", "UTF-8") + "=" +
                    URLEncoder.encode(employee_phone, "UTF-8");

            data += "&" + URLEncoder.encode("from", "UTF-8") + "=" +
                    URLEncoder.encode("Techsult", "UTF-8");
            data += "&" + URLEncoder.encode("sms", "UTF-8") + "=" +
                    URLEncoder.encode(message, "UTF-8");
            Log.d(TAG, "doInBackground :: Data String is:  " + data);
            URL url = new URL(link);
            conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
//            Log.d(TAG, "doInBackground :: getResponseCode " + conn.getResponseCode());
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            // Read Server Response
            while (reader.readLine() != null) {
                line = reader.readLine();
                Log.d(TAG, "readLine :: " + line);
                sb.append(line);
            }

            Log.d(TAG, "doInBackground :: " + data);
            Log.d(TAG, "doInBackground :: " + sb);
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "doInBackground", e);
            return e.toString();
        } finally {
            if (conn != null) {
//                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if(conn!=null) {
            try {
                Log.d(TAG, "onPostExecute :: getContent" + conn.getContent());
                conn.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "onPostExecute :: IOException" + e.getLocalizedMessage(), e.getCause());
            }
        }
        Toast.makeText(activity, "Employee has been notified, kindly wait for their response.",
                Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        updateEmployee();
        Log.d(TAG, "onPostExecute :: " + s);
    }

    private void updateEmployee() {
        final GeoFire geoFireNotified = new GeoFire(employees_notified);
        if(pickupLocation==null){
            Log.e(TAG, "geoFireNotified.updateEmployeeRef :: pickupLocation==null" );
        }else{
            Log.e(TAG, "geoFireNotified.updateEmployeeRef :: pickupLocation" );
        }
        if(employee_id==null){
            Log.e(TAG, "geoFireNotified.updateEmployeeRef :: employee==null" );
        }else{
            Log.d(TAG, "geoFireNotified.updateEmployeeRef :: getId "+ employee_id);
        }
        geoFireNotified.setLocation(employee_id, new GeoLocation(pickupLocation.latitude,
                pickupLocation.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Log.d(TAG,"geoFireNotified.setLocation :: " +error.getMessage(), error.toException());
                } else {
                    Log.d(TAG, "geoFireNotified.setLocation :: " +
                            key);
                    Log.d(TAG,
                            "successfully setLocation " +
                                    "geoFireNotified :: " + employer_id);
                    if(marker!=null){
                        marker.remove();
                    }
                }
            }
        });
    }
}
