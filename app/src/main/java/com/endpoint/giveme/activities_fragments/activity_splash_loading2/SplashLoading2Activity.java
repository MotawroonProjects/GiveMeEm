package com.endpoint.giveme.activities_fragments.activity_splash_loading2;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_map.ShopMapActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_products.ShopProductActivity;
import com.endpoint.giveme.databinding.ActivitySplashLoading2Binding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.location_service.LocationService;
import com.endpoint.giveme.models.CustomPlaceDataModel;
import com.endpoint.giveme.models.CustomPlaceModel;
import com.endpoint.giveme.models.CustomShopDataModel;
import com.endpoint.giveme.models.HourModel;
import com.endpoint.giveme.models.NearbyModel;
import com.endpoint.giveme.models.PhotosModel;
import com.endpoint.giveme.models.PlaceDetailsModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashLoading2Activity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    private ActivitySplashLoading2Binding binding;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String gps_perm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 22;
    private UserModel userModel;
    private double lat=0.0,lng=0.0;
    private Preferences preferences;
    private String place_id="";
    private String lang="ar";

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash_loading2);
        preferences = Preferences.getInstance();
        Paper.init(this);
        lang =Paper.book().read("lang","ar");
        getDataFromIntent();
        userModel = preferences.getUserData(this);
        CheckPermission();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri!=null){
            place_id = uri.getLastPathSegment();
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
                        status.startResolutionForResult(SplashLoading2Activity.this, 1255);
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
        lat = location.getLatitude();
        lng = location.getLongitude();
        /*Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("lat",location.getLatitude());
        intent.putExtra("lng",location.getLongitude());
        startActivity(intent);
        finish();*/
        getPlaceDetails();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        if (locationCallback != null) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        }
    }


    private void getPlaceDetails()
    {

        String fields = "icon,opening_hours,photos,reviews,place_id,type,vicinity,name,rating,geometry";

        Api.getService("https://maps.googleapis.com/maps/api/")
                .getPlaceDetails(place_id, fields, lang, getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDetailsModel>() {
                    @Override
                    public void onResponse(Call<PlaceDetailsModel> call, Response<PlaceDetailsModel> response) {
                        if (response.isSuccessful() && response.body() != null&&response.body().getResult()!=null&&response.body().getStatus().equals("OK")) {
                            List<NearbyModel.Photo> photoList = new ArrayList<>();
                            for (PhotosModel photosModel:response.body().getResult().getPhotos()){
                                NearbyModel.Photo photo = new NearbyModel.Photo(photosModel.getPhoto_reference());
                                photoList.add(photo);
                            }
                            NearbyModel.Result result = new NearbyModel.Result(response.body().getResult().getPlace_id(),response.body().getResult().getIcon(),response.body().getResult().getName(),response.body().getResult().getPlace_id(),response.body().getResult().getRating(),response.body().getResult().getVicinity(),photoList,response.body().getResult().getGeometry(),response.body().getResult().getTypes(),getDistance(new LatLng(lat,lng),new LatLng(response.body().getResult().getGeometry().getLocation().getLat(),response.body().getResult().getGeometry().getLocation().getLng())),response.body().getResult().getOpening_hours().isOpen_now(),response.body().getResult().getOpening_hours(),response.body().getResult().getPhotos(),response.body().getResult().getReviews(),null);

                            getPlaceDataByGooglePlaceId(result,response.body(),place_id);
                        } else {

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceDetailsModel> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SplashLoading2Activity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SplashLoading2Activity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });
    }

    private void getPlaceDataByGooglePlaceId(NearbyModel.Result result, PlaceDetailsModel body, String place_id) {
        Api.getService(Tags.base_url)
                .getCustomPlaceByGooglePlaceId(place_id)
                .enqueue(new Callback<CustomPlaceDataModel>() {
                    @Override
                    public void onResponse(Call<CustomPlaceDataModel> call, Response<CustomPlaceDataModel> response) {
                        if (response.isSuccessful()) {
                            result.setCustomPlaceModel(response.body().getData());
                            updateUiData(result);
                        } else {


                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<CustomPlaceDataModel> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SplashLoading2Activity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SplashLoading2Activity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });
    }

    private void updateUiData(NearbyModel.Result placeModel) {
        if (isRestaurant(placeModel)){

            if (placeModel.getCustomPlaceModel()!=null&&Integer.parseInt(placeModel.getCustomPlaceModel().getProducts_count())>0){

                String max_Offer_value="";
                if (placeModel.getCustomPlaceModel().getDelivery_offer()!=null){
                    max_Offer_value = placeModel.getCustomPlaceModel().getDelivery_offer().getLess_value();
                }
                String comment_count = "0";
                CustomPlaceModel.DeliveryOffer deliveryOffer = null;
                List<CustomPlaceModel.Days> days = null;

                if (placeModel.getCustomPlaceModel()!=null){
                    comment_count = placeModel.getCustomPlaceModel().getComments_count();
                    deliveryOffer = placeModel.getCustomPlaceModel().getDelivery_offer();
                    days = placeModel.getCustomPlaceModel().getDays();
                }

                CustomShopDataModel customShopDataModel = new CustomShopDataModel(placeModel.getPlace_id(),placeModel.getCustomPlaceModel().getId(),placeModel.getName(),placeModel.getVicinity(),placeModel.getGeometry().getLocation().getLat(),placeModel.getGeometry().getLocation().getLng(),max_Offer_value,placeModel.isOpen(),comment_count,String.valueOf(placeModel.getRating()),"custom",deliveryOffer,getHours(placeModel),days);
                Intent intent = new Intent(SplashLoading2Activity.this, ShopProductActivity.class);
                intent.putExtra("data",customShopDataModel);
                startActivity(intent);


            }else {
                Intent intent = new Intent(SplashLoading2Activity.this, ShopDetailsActivity.class);
                intent.putExtra("data",placeModel);
                startActivity(intent);

            }

        }else {
            Intent intent = new Intent(SplashLoading2Activity.this, ShopMapActivity.class);
            intent.putExtra("data",placeModel);
            startActivity(intent);

        }
        finish();
    }
    private double getDistance(LatLng latLng1, LatLng latLng2) {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000;
    }
    private boolean isRestaurant(NearbyModel.Result result){

        for (String type :result.getTypes()){
            if (type.equals("restaurant")){
                return true;
            }
        }

        return false;
    }
    private List<HourModel> getHours(NearbyModel.Result placeModel)
    {
        List<HourModel> list = new ArrayList<>();

        if (placeModel!=null&&placeModel.getWork_hours()!=null&&placeModel.getWork_hours().getWeekday_text()!=null){
            for (String time: placeModel.getWork_hours().getWeekday_text()){

                String day = time.split(":", 2)[0].trim();
                String t = time.split(":",2)[1].trim();
                HourModel hourModel = new HourModel(day,t);
                list.add(hourModel);




            }
        }


        return list;
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