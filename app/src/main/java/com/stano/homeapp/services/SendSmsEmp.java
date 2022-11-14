package com.stano.homeapp.services;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.stano.homeapp.data.Employee;
import com.stano.homeapp.data.Employer;
import com.stano.homeapp.data.User;
import com.stano.homeapp.users.employees.EmployeeMapActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;

public class SendSmsEmp extends AsyncTask<User, Void, String> {
    private static final String TAG =
            "TAG :: " + SendSmsEmp.class.getSimpleName() + " :: ";
    private final String employee_name;
    private final Activity activity;
    private  ProgressDialog progressDialog;
    private URLConnection conn;
    private  String time;
    private final String employee_phone;
private DatabaseReference work_in_progress;


    public SendSmsEmp(Activity activity, String employee_name, String employee_phone, DatabaseReference work_in_progress) {
        this.employee_name = employee_name.toUpperCase();
        this.employee_phone = employee_phone;
        this.activity = activity;
        this.work_in_progress = work_in_progress;
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
        Employer employer = (Employer) users[0];
        Log.d(TAG, "doInBackground :: " + employer.getPhone());
        String employer_name = employer.getName().toUpperCase();
        String employer_phone = employer.getPhone();
        if ('0' == employer_phone.charAt(0)) {
            employer_phone = employer_phone.replaceFirst("0", "254");
        }
        try {
            String message = time + employer_name + ", " +employee_name +" has accepted " +
                    "your job offer at " +
                    "HomeApp, " +
                    "kindly " +
                    "contact them through " +
                    employee_phone;
            String link = "http://techsultsms.co.ke/sms/api?";
            String data = URLEncoder.encode("action", "UTF-8") + "=" +
                    URLEncoder.encode("send-sms", "UTF-8");
            data += "&" + URLEncoder.encode("api_key", "UTF-8") + "=" +
                    URLEncoder.encode("QnJpYW46QnJpYW5QQHNz", "UTF-8");
            data += "&" + URLEncoder.encode("to", "UTF-8") + "=" +
                    URLEncoder.encode(employer_phone, "UTF-8");

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
        Log.d(TAG, "onPostExecute :: " + s);

    }


}
