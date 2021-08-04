package com.endpoint.giveme.activities_fragments.activity_map_delivery_location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.ActivityMapDeliveryLocationBinding;
import com.endpoint.giveme.databinding.DialogDelivryTimeBinding;
import com.endpoint.giveme.interfaces.Listeners;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.AddOrderProductsModel;
import com.endpoint.giveme.models.FavoriteLocationModel;
import com.endpoint.giveme.models.PlaceGeocodeData;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDeliveryLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, Listeners.BackListener {
    private ActivityMapDeliveryLocationBinding binding;
    private String lang;
    private double drop_off_lat = 0.0, drop_off_lng = 0.0;
    private String address = "";
    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 15.0f;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private Preferences preferences;
    private AddOrderProductsModel addOrderProductsModel;
    private boolean canSelect = false;
    private int time = 1;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_map_delivery_location);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        addOrderProductsModel = (AddOrderProductsModel) intent.getSerializableExtra("data");

    }

    private void initView() {
        preferences = Preferences.getInstance();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setListener(this);
        binding.setModel(addOrderProductsModel);
        binding.tvTime.setText(getString(R.string.hour1));
        binding.btnConfirm.setOnClickListener(v -> {
            if (canSelect){
                FavoriteLocationModel favoriteLocationModel = new FavoriteLocationModel("","",address,drop_off_lat,drop_off_lng);
                Intent intent = getIntent();
                intent.putExtra("data",favoriteLocationModel);
                intent.putExtra("time",time);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        binding.llTime.setOnClickListener(v -> createTimeDialogAlert());
        updateUI();






    }

    private void checkPermission()
    {
        if (ActivityCompat.checkSelfPermission(this,fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{fineLocPerm}, loc_req);
        } else {
            mMap.setMyLocationEnabled(true);
            initGoogleApi();
        }
    }
    private void initGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    private void updateUI() {

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);


    }

    private void createTimeDialogAlert() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogDelivryTimeBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_delivry_time, null, false);
        List<String> times = new ArrayList<>();
        times.add(getString(R.string.hour1));
        times.add(getString(R.string.hour2));
        times.add(getString(R.string.hour3));
        times.add(getString(R.string.day1));
        times.add(getString(R.string.day2));
        times.add(getString(R.string.day3));


        String[] values = new String[times.size()];

        binding.picker.setMinValue(0);
        binding.picker.setMaxValue(times.size() - 1);
        binding.picker.setDisplayedValues(times.toArray(values));
        binding.picker.setValue(1);
        binding.imageUp.setOnClickListener(v -> {
            binding.picker.setValue(binding.picker.getValue() - 1);
        });

        binding.imageDown.setOnClickListener(v -> {
            binding.picker.setValue(binding.picker.getValue() + 1);
        });


        binding.btnOk.setOnClickListener(v ->
                {
                    MapDeliveryLocationActivity.this.time = binding.picker.getValue()+1;
                    dialog.dismiss();
                    String time = values[binding.picker.getValue()];
                    MapDeliveryLocationActivity.this.binding.tvTime.setText(time);
                }
        );

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setTrafficEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setIndoorEnabled(true);


            checkPermission();

            mMap.setOnMapClickListener(latLng -> {
                drop_off_lat = latLng.latitude;
                drop_off_lng = latLng.longitude;
                AddMarker(drop_off_lat, drop_off_lng);
                getGeoData(drop_off_lat, drop_off_lng);

            });
            mMap.setOnCameraMoveListener(() -> {
                mMap.clear();
                binding.pin.setVisibility(View.VISIBLE);
            });

            mMap.setOnCameraIdleListener(() ->{
                mMap.clear();
                drop_off_lat = mMap.getCameraPosition().target.latitude;
                drop_off_lng = mMap.getCameraPosition().target.longitude;
                getGeoData(drop_off_lat, drop_off_lng);
                binding.pin.setVisibility(View.VISIBLE);
            });
        }
    }

    private void getGeoData(final double lat, double lng) {
        binding.progBar.setVisibility(View.VISIBLE);
        binding.imagePin.setVisibility(View.GONE);
        String location = lat + "," + lng;
        Log.e("add",location);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .getGeoData(location, lang, getString(R.string.search_key))
                .enqueue(new Callback<PlaceGeocodeData>() {
                    @Override
                    public void onResponse(Call<PlaceGeocodeData> call, Response<PlaceGeocodeData> response) {
                        binding.progBar.setVisibility(View.GONE);
                        binding.imagePin.setVisibility(View.VISIBLE);

                        if (response.isSuccessful() && response.body() != null) {

                            if (response.body().getResults().size() > 0) {

                                if (response.body().getResults().size()>1){
                                    address = response.body().getResults().get(1).getFormatted_address();

                                }else {
                                    address = response.body().getResults().get(0).getFormatted_address();

                                }
                                canSelect = true;
                                binding.tvAddress.setText(address + "");
                                binding.btnConfirm.setBackgroundResource(R.drawable.small_rounded_primary);
                            }
                        } else {

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceGeocodeData> call, Throwable t) {
                        try {
                            binding.imagePin.setVisibility(View.VISIBLE);
                            binding.progBar.setVisibility(View.GONE);

                            Toast.makeText(MapDeliveryLocationActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void AddMarker(double lat, double lng) {

        this.drop_off_lat = lat;
        this.drop_off_lng = lng;



        if (marker == null) {
            binding.pin.setVisibility(View.GONE);
            View view = LayoutInflater.from(this).inflate(R.layout.map_pin,null);
            IconGenerator iconGenerator = new IconGenerator(this);
            iconGenerator.setBackground(null);
            iconGenerator.setContentView(view);
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())));
        }
        marker.setDraggable(false);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
    }

    private void initLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(60000);
        LocationSettingsRequest.Builder request = new LocationSettingsRequest.Builder();
        request.addLocationRequest(locationRequest);
        request.setAlwaysShow(false);


        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request.build());
        result.setResultCallback(locationSettingsResult -> {
            Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdate();
                    break;

                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(MapDeliveryLocationActivity.this,100);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient!=null)
        {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdate()
    {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    @Override
    public void onLocationChanged(Location location) {
        drop_off_lat = location.getLatitude();
        drop_off_lng = location.getLongitude();

        AddMarker(drop_off_lat, drop_off_lng);
        getGeoData(drop_off_lat, drop_off_lng);

        if (googleApiClient!=null)
        {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mMap.setMyLocationEnabled(true);
                initGoogleApi();
            }else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100&&resultCode== Activity.RESULT_OK)
        {

            startLocationUpdate();
        }

    }





    @Override
    public void back() {
        super.onBackPressed();

    }


    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient!=null)
        {
            if (locationCallback!=null)
            {
                LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
                googleApiClient.disconnect();
                googleApiClient = null;
            }
        }
    }


}


