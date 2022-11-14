package com.stano.homeapp.users.employers;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.stano.homeapp.LauncherActivity;
import com.stano.homeapp.R;
import com.stano.homeapp.data.Employee;
import com.stano.homeapp.data.Employer;
import com.stano.homeapp.data.EmployerRequest;
import com.stano.homeapp.services.SendSms;
import com.stano.homeapp.settings.CustomInfoWindowAdapter;
import com.stano.homeapp.settings.HomeApp_Settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class EmployerMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "TAG :: " + EmployerMapActivity.class.getSimpleName() + " :: ";
    private static final int FAR_ZOOM = 105;
    private static final int MID_ZOOM = 145;
    private static final int CLOSE_ZOOM = 100;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 18765445;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private final int RATE_REQUEST = 1254;
    private Location lastKnownLocation;
    private Location mLastLocation;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String destination, requestService, mPhone;
    private LatLng destinationLatLng, pickupLocation;
    private Marker employeeMarker, employerMarker;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private CameraPosition cameraPosition;
    private boolean locationPermissionGranted;
    private GoogleMap mMap;
    private Button logout_btn, request_btn, settings_btn, select_btn;
    private RadioGroup radioGroup;
    private Boolean requestBol = false;
    private Employer employer;
    private String employerMarkerInfo;
    private DatabaseReference employer_ref;
    private DatabaseReference employee_ref;
    private DatabaseReference employees_searching, employees_notified, rating_ref;
    private DatabaseReference working;
    private boolean employer_working = false;
    private HashMap<String, Marker> stringMarkerHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve location and camera position from saved instance state.
        Log.d(TAG, "onCreate ");

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_employer_map);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        this.setTitle(currentUser.getDisplayName());
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        employer_ref = firebaseDatabase
                .getReference("Users")
                .child("Employer")
                .child(currentUser.getUid());
        employees_searching = firebaseDatabase
                .getReference("employees_available");
        employees_notified = firebaseDatabase
                .getReference("employees_notified")
                .child(currentUser.getUid());
        working = firebaseDatabase
                .getReference("employers_working");
        rating_ref = firebaseDatabase
                .getReference("Ratings");
        employer_ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
//                    DataSnapshot dataSnapshot = task.getResult();

                }
            }
        });
        logout_btn = findViewById(R.id.logout);
        request_btn = findViewById(R.id.request);
        settings_btn = findViewById(R.id.settings);
        radioGroup = findViewById(R.id.radioGroup);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(EmployerMapActivity.this, LauncherActivity.class);
                startActivity(intent);
                finish();
            }
        });
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployerMapActivity.this,
                        EmployerSettingsActivity.class);
                startActivity(intent);
            }
        });
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(String.valueOf(EmployerMapActivity.this), "An error occurred: " + status);
            }
        });
        request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(EmployerMapActivity.this,
//                        EmployeeRateActivity.class);
//                startActivity(intent);
                Log.d(TAG, "setOnClickListener");

                if (requestBol) {
                    endRide();
                } else {
                    requestEmployee();
                }
            }
        });
        Gson gson = new Gson();
        loadCurrentUser();
        rating_ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot next : dataSnapshot.getChildren()) {
                        Log.d(TAG, "ratingsListener :: getKey   = " + next.getKey());
                        Log.d(TAG, "ratingsListener :setaaating: getValue = " + next.getValue());
                        stringMarkerHashMap.forEach((key, value) -> {
                            if(Objects.equals(next.getKey(), key)){
                                Log.d(TAG, "ratingsListener :: stringMarkerHashMap   = " + key);
                                Log.d(TAG, "ratingsListener :stringMarkerHashMap: getValue = " + value);
                                if(next.getKey().equals("rate")){
                                    Log.d(TAG, "ratingsListener :: rate 12345  = " + key);
                                    Employee employee = gson.fromJson(value.getSnippet(), Employee.class);
                                    employee.setRatings(next.getValue().toString());
                                    value.setSnippet(gson.toJson(employee));
                                    MarkerOptions m = new MarkerOptions()
                                            .position(value.getPosition())
                                            .title(value.getTitle())
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.proicon))
                                            .snippet(value.getTitle());
                                    value.remove();
                                    value = null;
//                                    m.visible(true);
                                    Marker marker =
                                            mMap.addMarker(m);

                                    marker.showInfoWindow();
                                    stringMarkerHashMap.remove(employee.getId());
                                    stringMarkerHashMap.put(employee.getId(), marker);
//                                    mMap.clear();
//                                    getDeviceLocation();
//                                    return;
                                }
                                return;
                            }

                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCurrentUser() {
        employer_ref
                .get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
//                                loader.dismiss();
                            Log.d(TAG, "Task" +
                                    "<DataSnapshot> " +
                                    "task. " +
                                    "isSuccessful");
                            DataSnapshot dataSnapshot1 = task.getResult();
                            Log.d(TAG, "loadCurrentUser dataSnapshot1" +
                                    " getKey :: " + dataSnapshot1.getKey());
                            Log.d(TAG, "loadCurrentUser dataSnapshot1" +
                                    " getValue :: " + dataSnapshot1.getValue());
                            employer =
                                    task.getResult().getValue(Employer.class);

                            if (employer != null) {
                                Gson gson = new Gson();
                                employerMarkerInfo =
                                        gson.toJson(employer);

                                Log.d(TAG,
                                        "loadCurrentUser Employee :: " + employer.getName());
                                Log.d(TAG,
                                        "loadCurrentUser Employee :: " + employer.getPhone());
                                Log.d(TAG,
                                        "loadCurrentUser Employee :: " + employer.getEmail());
                            } else {
                                Log.d(TAG,
                                        "loadCurrentUser Employee :: employer != null");
                            }
                        }
                    }
                });
    }

    private void requestEmployee() {
        Log.d(TAG, "requestEmployee");
        int selectId = radioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = findViewById(selectId);
        if (radioButton == null) {
            HomeApp_Settings.showAlert(EmployerMapActivity.this, "Select Category",
                    "FreeLance or " +
                            "Repairs");
            return;
        }
        if (radioButton.getText() == null) {
            Log.d(TAG, "radioButton.getText() == null");
            return;
        }
        requestService = radioButton.getText().toString();
        requestBol = true;
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation");
        try {
            if (locationPermissionGranted) {
                Log.d(TAG, "getDeviceLocation :: locationPermissionGranted");
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.d(TAG, "getDeviceLocation :: onComplete");

                        if (task.isSuccessful()) {
                            Log.d(TAG, "getDeviceLocation :: isSuccessful");

                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                Log.d(TAG, "getDeviceLocation :: lastKnownLocation != null");

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), FAR_ZOOM));
                                final GeoFire geoFireAvailable = new GeoFire(employees_searching);
                                final GeoFire geoFireWorking = new GeoFire(working);
                                if (!employer_working) {
                                    employer_working = true;
                                    Log.d(TAG, "getDeviceLocation :: isSuccessful");

                                    pickupLocation =
                                            new LatLng(lastKnownLocation.getLatitude(),
                                                    lastKnownLocation.getLongitude());
                                    Bitmap bitmap = null;
                                    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                                    Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
                                    Canvas canvas1 = new Canvas(bmp);
                                    // paint defines the text color, stroke width and size
                                    Paint color = new Paint();
                                    color.setTextSize(35);
                                    color.setColor(Color.BLACK);


//                                    if (currentUser.getPhotoUrl() != null) {
//                                        try {
//                                            // Setting image on image view using Bitmap
//                                            bitmap = MediaStore
//                                                    .Images
//                                                    .Media
//                                                    .getBitmap(
//                                                            getContentResolver(),
//                                                            currentUser.getPhotoUrl());
//
//                                        } catch (IOException e) {
//                                            // Log the exception
//                                            Log.d(TAG, " Bitmap bitmap :: ", e.getCause());
////                                        e.printStackTrace();
//                                        }
//                                    }
                                    MarkerOptions employerOptions = new MarkerOptions()
                                            .position(pickupLocation)
                                            .title("Job location")
                                            // Specifies the anchor to be at a particular point in the marker image.
                                            .anchor(0.5f, 1);
                                    if (!(bitmap == null)) {
                                        Log.d(TAG, " Bitmap bitmap!=null :: ");

                                        // modify canvas
                                        canvas1.drawBitmap(bitmap, 0, 0, color);
                                        canvas1.drawText("Job location", 30, 40, color);
                                        // add marker to Map
                                    } else {
                                        Log.d(TAG, " Bitmap bitmap==null :: ");
                                    }
                                    employerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.jobicon));
                                    employerMarker = mMap.addMarker(employerOptions);

                                    request_btn.setText(getResources().getText(R.string.searching));
                                    employees_searching.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                if (data.getKey() != null) {
                                                    Log.d(TAG,
                                                            "employees_searching getKey :: " + data.getKey());
                                                    Log.d(TAG,
                                                            "employees_searching getValue :: " + data.getValue());
                                                    EmployerRequest employerRequest = data.getValue(EmployerRequest.class);
                                                    List<Double> map = (List<Double>) employerRequest.getL();
                                                    double locationLat = 0;
                                                    double locationLng = 0;
                                                    if (map.get(0) != null) {
                                                        locationLat = Double.parseDouble(map.get(0).toString());
                                                    }
                                                    if (map.get(1) != null) {
                                                        locationLng = Double.parseDouble(map.get(1).toString());
                                                    }
                                                    LatLng employeeLatLng = new LatLng(locationLat, locationLng);
                                                    if (employeeMarker != null) {
                                                        employeeMarker.remove();
                                                    }

                                                    Location loc1 = new Location("");
                                                    loc1.setLatitude(pickupLocation.latitude);
                                                    loc1.setLongitude(pickupLocation.longitude);

                                                    Location loc2 = new Location("");
                                                    loc2.setLatitude(employeeLatLng.latitude);
                                                    loc2.setLongitude(employeeLatLng.longitude);

                                                    float distance = loc1.distanceTo(loc2);

                                                    FirebaseDatabase
                                                            .getInstance()
                                                            .getReference("Users")
                                                            .child("Employee")
                                                            .child(data.getKey())
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Log.d(TAG, "Task" +
                                                                                "<DataSnapshot> " +
                                                                                "task. " +
                                                                                "isSuccessful");
                                                                        DataSnapshot dataSnapshot1 = task.getResult();
                                                                        Log.d(TAG, "dataSnapshot1" +
                                                                                " getKey :: " + dataSnapshot1.getKey());
                                                                        Log.d(TAG, "dataSnapshot1" +
                                                                                " getValue :: " + dataSnapshot1.getValue());
                                                                        Employee employee =
                                                                                task.getResult().getValue(Employee.class);
                                                                        if (employee != null) {
                                                                            Log.d(TAG,
                                                                                    "Employee :: " + employee.getName());
                                                                            Log.d(TAG,
                                                                                    "Employee :: " + employee.getPhone());
                                                                            Log.d(TAG,
                                                                                    "Employee :: " + employee.getProfileImageUrl());


                                                                            if (employee.getService().equals(requestService)) {
                                                                                Gson gson = new Gson();

                                                                                if (distance < 100) {
                                                                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                                                                            new LatLng(lastKnownLocation.getLatitude(),
                                                                                                    lastKnownLocation.getLongitude()), CLOSE_ZOOM));
                                                                                    request_btn.setText("Employee's Here");
                                                                                } else {
                                                                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                                                                            new LatLng(lastKnownLocation.getLatitude(),
                                                                                                    lastKnownLocation.getLongitude()), MID_ZOOM));
                                                                                    request_btn.setText("Employee Found: " + distance / 1000 + " KM away");
                                                                                }
                                                                                rating_ref
                                                                                        .child(employee.getId())
                                                                                        .addValueEventListener(
                                                                                                new ValueEventListener() {
                                                                                                    @Override
                                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                        float rate = 0;
                                                                                                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                                                                                            for (DataSnapshot next : dataSnapshot.getChildren()) {
                                                                                                                Log.d(TAG, "ratingsListener :: getKey   = " + next.getKey());
                                                                                                                Log.d(TAG, "ratingsListener :setaaating: getValue = " + next.getValue());
                                                                                                                if (next.getKey().equals("rate")) {
                                                                                                                    rate += Float.parseFloat(next.getValue().toString());
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        employee.setRatings(String.valueOf(rate));
                                                                                                        String markerInfoString =
                                                                                                                gson.toJson(employee);
                                                                                                        MarkerOptions m = new MarkerOptions()
                                                                                                                .position(employeeLatLng)
                                                                                                                .title(employee.getName())
                                                                                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.proicon))
                                                                                                                .snippet(markerInfoString);
                                                                                                        Marker marker =
                                                                                                                mMap.addMarker(m);

                                                                                                        stringMarkerHashMap.put(employee.getId(), marker);

                                                                                                    }

                                                                                                    @Override
                                                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                                                    }
                                                                                                });

                                                                            }

                                                                        }

                                                                    } else {
                                                                        Log.d(TAG, "Task" +
                                                                                "<DataSnapshot> " +
                                                                                "task. " +
                                                                                "Not isSuccessful");
                                                                        Log.e(TAG, Objects.requireNonNull(task.getException()).getMessage(),
                                                                                task.getException());
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "getDeviceLocation :: onCancelled");
                                            Log.e(TAG, error.getMessage(), error.toException());
                                        }
                                    });
                                }
                            } else {
                                Log.d(TAG, "getDeviceLocation :: lastKnownLocation == null");

                            }
                        } else {
                            Log.d(TAG, "getDeviceLocation " + task.getException().getMessage());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void endRide() {
        request_btn.setBackground(getResources().getDrawable(R.drawable.button_green));
        request_btn.setText(getResources().getText(R.string.search_employee));
        requestBol = true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(EmployerMapActivity.this));
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Toast.makeText(EmployerMapActivity.this, "Long press to hire",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(@NonNull Marker marker) {
                        Gson gson = new Gson();
                        Employee employee = gson.fromJson(marker.getSnippet(), Employee.class);
                        new AlertDialog.Builder(EmployerMapActivity.this)
                                .setTitle("Hire this Employee")
                                .setMessage("Would you like to notify this employee?")
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Log.d(TAG, "setOnInfoWindowLongClickListener :: AlertDialog");
                                        SendSms mSmsTask =
                                                new SendSms(EmployerMapActivity.this,
                                                        currentUser.getDisplayName(),
                                                        currentUser.getUid(),
                                                        employee.getId(),
                                                        pickupLocation,
                                                        employees_notified, marker);
                                        Employee[] emps = {employee};

                                        try {
                                            String re = mSmsTask.execute(emps).get(2,
                                                    TimeUnit.SECONDS);
                                            Log.d(TAG, "setOnInfoWindowLongClickListener :: " +
                                                    "mSmsTask.execute: " + re);
                                        } catch (ExecutionException e) {
                                            Log.e(TAG,
                                                    "setOnInfoWindowLongClickListener :ExecutionException: " + e.getMessage(), e.fillInStackTrace());
                                        } catch (InterruptedException e) {
                                            Log.e(TAG,
                                                    "setOnInfoWindowLongClickListener :InterruptedException: " + e.getMessage(), e.fillInStackTrace());
                                        } catch (TimeoutException e) {
                                            Log.e(TAG,
                                                    "setOnInfoWindowLongClickListener :TimeoutException: " + e.getMessage(), e.fillInStackTrace());
                                        }

                                        if (mSmsTask.getStatus() == AsyncTask.Status.FINISHED) {
                                            Log.d(TAG, "setOnInfoWindowLongClickListener :: AsyncTask.Status.FINISHED");
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.no,
                                        null)
                                .setNeutralButton(R.string.rate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(EmployerMapActivity.this,
                                                EmployeeRateActivity.class);
                                        intent.putExtra("employee", employee.getId());
                                        intent.putExtra("employer", currentUser.getUid());
                                        startActivityForResult(intent, RATE_REQUEST);
                                        marker.showInfoWindow();
                                    }
                                }).show();
                        Log.d(TAG, "setOnInfoWindowLongClickListener :: onInfoWindowLongClick");
                    }
                });
                request_btn.setEnabled(true);
            } else {
                request_btn.setEnabled(false);
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode == RATE_REQUEST) {
            getDeviceLocation();

        }
    }

    private void updateEmployeeRef(Employee employee) {
        final GeoFire geoFireNotified = new GeoFire(employees_notified);
        if (pickupLocation == null) {
            Log.e(TAG, "geoFireNotified.updateEmployeeRef :: pickupLocation==null");
        } else {
            Log.e(TAG, "geoFireNotified.updateEmployeeRef :: pickupLocation");
        }
        if (employee == null) {
            Log.e(TAG, "geoFireNotified.updateEmployeeRef :: employee==null");
        } else {
            Log.e(TAG, "geoFireNotified.updateEmployeeRef :: employee " + employee.getName());
            Log.d(TAG, "geoFireNotified.updateEmployeeRef :: getId " + employee.getId());
        }
        String employee_id = "" + employee.getId();
        geoFireNotified.setLocation(employee_id, new GeoLocation(pickupLocation.latitude,
                pickupLocation.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Log.d(TAG, error.getMessage(), error.toException());
                } else {
                    Log.d(TAG, "geoFireNotified.setLocation :: " +
                            key);
                    Log.d(TAG,
                            "successfully setLocation " +
                                    "geoFireNotified :: " + currentUser.getUid());
                    HashMap<String, Object> emp = new HashMap<>();
                    emp.put("employer_uuid", currentUser.getUid());
                    employees_notified
                            .child(employee.getId())
                            .updateChildren(emp)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "employees_notified :: isSuccessful");
                                    } else {
                                        if (task.getException() != null) {
                                            Log.d(TAG, "employees_notified :: getException",
                                                    task.getException());
                                        } else {
                                            Log.d(TAG, "employees_notified :: getException ERROR " +
                                                    "EER");
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.d(TAG, "locationPermissionGranted");

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    updateLocationUI();
                }
            }
            default:
                Log.d(TAG, "onRequestPermissionsResult");
        }
        updateLocationUI();
    }
}