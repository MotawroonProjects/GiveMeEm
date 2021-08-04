package com.endpoint.giveme.activities_fragments.activity_splash_loading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_home.HomeActivity;
import com.endpoint.giveme.databinding.ActivitySplashLoadingBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.location_service.LocationService;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import io.paperdb.Paper;

public class SplashLoadingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    private ActivitySplashLoadingBinding binding;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String gps_perm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 22;
    private UserModel userModel;
    private Preferences preferences;
    private String type = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash_loading);
        getDataFromIntent();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        CheckPermission();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra("notification")){
            type = "notification";
        }else if (intent.hasExtra("order")){
            type = "order";
        }
    }

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, gps_perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{gps_perm}, loc_req);
        } else {

            initGoogleApiClient();
            if (userModel!=null&&userModel.getUser().getUser_type().equals("driver")){
               try {
                   Intent intent = new Intent(this, LocationService.class);
                   startService(intent);
               }catch (Exception e){}

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == loc_req) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initGoogleApiClient();
                if (userModel!=null&&userModel.getUser().getUser_type().equals("driver")){
                    try {
                        Intent intent = new Intent(this, LocationService.class);
                        startService(intent);
                    }catch (Exception e){}
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(60000);
        LocationSettingsRequest.Builder request = new LocationSettingsRequest.Builder();
        request.addLocationRequest(locationRequest);
        request.setAlwaysShow(false);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request.build());

        result.setResultCallback(result1 -> {

            Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdate();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(SplashLoadingActivity.this, 1255);
                    } catch (Exception e) {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e("not available", "not available");
                    break;
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("lat",location.getLatitude());
        intent.putExtra("lng",location.getLongitude());
        intent.putExtra("type",type);
        startActivity(intent);
        finish();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        if (locationCallback != null) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1255 && resultCode == RESULT_OK) {
            startLocationUpdate();
            if (userModel!=null&&userModel.getUser().getUser_type().equals("driver")){
                try {
                    Intent intent = new Intent(this, LocationService.class);
                    startService(intent);
                }catch (Exception e){}
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleApiClient!=null){
            googleApiClient.disconnect();
            googleApiClient=null;
        }

        if (locationCallback!=null){
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);

        }
    }
}