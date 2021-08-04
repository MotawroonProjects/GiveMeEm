package com.endpoint.giveme.activities_fragments.activity_package_map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_map_search.MapSearchActivity;
import com.endpoint.giveme.adapters.FavoriteLocationAdapter;
import com.endpoint.giveme.databinding.ActivityPackageMapBinding;
import com.endpoint.giveme.databinding.DialogFavLocationBinding;
import com.endpoint.giveme.interfaces.Listeners;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.DefaultSettings;
import com.endpoint.giveme.models.FavoriteLocationModel;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class PackageMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, Listeners.BackListener {
    private ActivityPackageMapBinding binding;
    private String lang;
    private double lat = 0.0, lng = 0.0;
    private String address = "";
    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 15.0f;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private boolean canSelectLocation = false;
    private Preferences preferences;
    private FavoriteLocationAdapter adapter;
    private List<FavoriteLocationModel> favoriteLocationModelList;
    private DefaultSettings defaultSettings;
    private boolean canDraggable = false;
    private int type = 1;
    private AlertDialog dialog;
    private FavoriteLocationModel fromLocationModel, toLocationModel;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_package_map);
        initView();
    }


    private void initView() {
        favoriteLocationModelList = new ArrayList<>();
        preferences = Preferences.getInstance();
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.setLang(lang);
        binding.setListener(this);
        defaultSettings = preferences.getAppSetting(this);

        binding.llFromLocation.setOnClickListener(v -> {
            if (defaultSettings != null) {
                favoriteLocationModelList.clear();
                favoriteLocationModelList.addAll(defaultSettings.getFavoriteLocationModelList());

            } else {
                navigateToMapSearchActivity(200);

            }

            type = 1;
            if (favoriteLocationModelList.size() > 0) {
                if (dialog != null) {
                    dialog.show();
                } else {
                    createDialogAlert(getString(R.string.pick_up));

                }
            } else {
                navigateToMapSearchActivity(200);
            }
        });

        binding.llToLocation.setOnClickListener(v -> {
            if (defaultSettings != null) {
                favoriteLocationModelList.clear();
                favoriteLocationModelList.addAll(defaultSettings.getFavoriteLocationModelList());

            } else {
                navigateToMapSearchActivity(300);

            }

            type = 2;
            if (favoriteLocationModelList.size() > 0) {
                if (dialog != null) {
                    dialog.show();
                } else {
                    createDialogAlert(getString(R.string.drop_off));

                }
            } else {
                navigateToMapSearchActivity(300);

            }
        });

        binding.btnOk.setOnClickListener(v -> {
            if (canSelectLocation){
                Intent intent = getIntent();
                intent.putExtra("data1",fromLocationModel);
                intent.putExtra("data2",toLocationModel);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        updateUI();


    }

    private void navigateToMapSearchActivity(int req) {
        Intent intent = new Intent(this, MapSearchActivity.class);
        startActivityForResult(intent, req);
    }


    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setTrafficEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setIndoorEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if (canDraggable) {
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                addMarker(new LatLng(fromLocationModel.getLat(),fromLocationModel.getLng()),new LatLng(toLocationModel.getLat(),toLocationModel.getLng()));

            } else {

                checkPermission();

            }


        }
    }


    private void addMarker(LatLng latLng1, LatLng latLng2) {
        mMap.clear();
        View view1 = LayoutInflater.from(this).inflate(R.layout.map_pin_from_loc, null);
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setBackground(null);
        iconGenerator.setContentView(view1);
        mMap.addMarker(new MarkerOptions().position(latLng1).icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())));

        View view2 = LayoutInflater.from(this).inflate(R.layout.map_pin_to_loc, null);
        IconGenerator iconGenerator2 = new IconGenerator(this);
        iconGenerator2.setBackground(null);
        iconGenerator2.setContentView(view2);
        mMap.addMarker(new MarkerOptions().position(latLng2).icon(BitmapDescriptorFactory.fromBitmap(iconGenerator2.makeIcon())));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng1);
        builder.include(latLng2);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),150));

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
                        status.startResolutionForResult(PackageMapActivity.this, 100);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        });


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
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 20.0f));
        if (googleApiClient != null) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }

    private void createDialogAlert(String title) {

        dialog = new AlertDialog.Builder(this)
                .create();

        DialogFavLocationBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_fav_location, null, false);
        binding.tvTitle.setText(title);
        adapter = new FavoriteLocationAdapter(favoriteLocationModelList, this);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setAdapter(adapter);

        binding.imageClose.setOnClickListener(v -> dialog.dismiss());
        binding.flSelectLocation.setOnClickListener(v -> {
            dialog.dismiss();
            if (type == 1) {
                navigateToMapSearchActivity(200);
            } else {
                navigateToMapSearchActivity(300);
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    public void setFavoriteItem(FavoriteLocationModel model) {
        dialog.dismiss();
        if (type == 1) {
            fromLocationModel = model;
            binding.tvFromLocation.setText(fromLocationModel.getAddress());

        } else {
            toLocationModel = model;
            binding.tvToLocation.setText(toLocationModel.getAddress());

        }
        updateActionUi();
    }


    private void updateActionUi() {
        if (fromLocationModel != null && toLocationModel != null) {

            double distance = calculateDistance();
            binding.tvDistance.setText(String.format(Locale.ENGLISH,"%.2f %s",distance,getString(R.string.km)));
            onMapReady(mMap);
            canDraggable = true;

            if (distance>0.0){
                canSelectLocation = true;
                binding.btnOk.setBackgroundResource(R.drawable.small_rounded_primary);
                binding.btnOk.setTextColor(ContextCompat.getColor(this, R.color.white));
            }else {
                canSelectLocation = false;
                binding.btnOk.setBackgroundResource(R.drawable.small_rounded_gray);
                binding.btnOk.setTextColor(ContextCompat.getColor(this, R.color.gray9));

            }

        } else {
            canSelectLocation = false;
            binding.btnOk.setBackgroundResource(R.drawable.small_rounded_gray);
            binding.btnOk.setTextColor(ContextCompat.getColor(this, R.color.gray9));

        }


    }

    private double calculateDistance(){
        double dis = SphericalUtil.computeDistanceBetween(new LatLng(fromLocationModel.getLat(),fromLocationModel.getLng()),new LatLng(toLocationModel.getLat(),toLocationModel.getLng()))/1000;
        return dis;
        //return String.format(Locale.ENGLISH,"%.2f %s",dis,getString(R.string.km));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                initGoogleApi();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            startLocationUpdate();
        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            fromLocationModel = (FavoriteLocationModel) data.getSerializableExtra("data");
            binding.tvFromLocation.setText(fromLocationModel.getAddress());

            updateActionUi();
        } else if (requestCode == 300 && resultCode == Activity.RESULT_OK && data != null) {
            toLocationModel = (FavoriteLocationModel) data.getSerializableExtra("data");
            binding.tvToLocation.setText(toLocationModel.getAddress());

            updateActionUi();

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
        if (googleApiClient != null) {
            if (locationCallback != null) {
                LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
                googleApiClient.disconnect();
                googleApiClient = null;
            }
        }
    }


}


