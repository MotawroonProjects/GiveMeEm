package com.endpoint.giveme.activities_fragments.activity_follow_order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.ActivityFollowOrderBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.FavoriteLocationModel;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.models.PlaceDirectionModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowOrderActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityFollowOrderBinding binding;
    private String lang;
    private GoogleMap mMap;
    private UserModel userModel;
    private Preferences preferences;
    private OrderModel orderModel;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private LatLng startPosition,endPosition = null;
    private float v;
    private Marker marker;
    private List<LatLng> latLngList,latLngList2;
    private int index = 0,next=0;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_follow_order);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        orderModel = (OrderModel) intent.getSerializableExtra("data");


    }

    private void initView() {
        latLngList = new ArrayList<>();
        latLngList2 = new ArrayList<>();

        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.close.setOnClickListener(v -> {super.onBackPressed();});
        updateUI();
        binding.setModel(orderModel);
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

    }

    private void checkPermission()
    {
        if (ActivityCompat.checkSelfPermission(this,fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{fineLocPerm}, loc_req);
        }
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
            LatLng latLng1 = new LatLng(Double.parseDouble(orderModel.getMarket_latitude()),Double.parseDouble(orderModel.getMarket_longitude()));
            LatLng latLng2 = new LatLng(Double.parseDouble(orderModel.getClient_latitude()),Double.parseDouble(orderModel.getClient_longitude()));

            addMarker(latLng1.latitude,latLng1.longitude,1);
            addMarker(latLng2.latitude,latLng2.longitude,2);

            getDirection(latLng1,latLng2);


            checkPermission();
        }
    }

    private void addMarker(double lat ,double lng,int type) {
        View view = LayoutInflater.from(this).inflate(R.layout.map_add_offer_location_row,null);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        if (type==1){
            tvTitle.setText(getString(R.string.pick_up));
            tvTitle.setBackgroundResource(R.drawable.rounded_primary);
        }else {
            tvTitle.setText(getString(R.string.drop_off));
            tvTitle.setBackgroundResource(R.drawable.rounded_second);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(Double.parseDouble(orderModel.getMarket_latitude()),Double.parseDouble(orderModel.getMarket_longitude())));
            builder.include(new LatLng(Double.parseDouble(orderModel.getClient_latitude()),Double.parseDouble(orderModel.getClient_longitude())));

            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),150));

            }catch (Exception e){

            }

        }
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentPadding(2,2,2,2);
        iconGenerator.setBackground(null);
        iconGenerator.setContentView(view);

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).position(new LatLng(lat,lng)));



    }

    private void addCarMarker() {

        View view = LayoutInflater.from(this).inflate(R.layout.car_pin,null);
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentPadding(2,2,2,2);
        iconGenerator.setBackground(null);
        iconGenerator.setContentView(view);
        Log.e("ddd",latLngList.get(0).latitude+"__"+latLngList.get(0).longitude);
        if (marker==null){
            marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).position(latLngList.get(0)));

        }
        marker.setFlat(true);
        marker.setAnchor(.5f,.5f);

        if (latLngList.size()>=2){
            animateCar();

        }



    }

    private void animateCar() {
        index = -1;
        next = 1;
        if (index < latLngList.size() - 1) {
            index++;
            next = index + 1;
        }
        if (index < latLngList.size() - 1) {
            startPosition = latLngList.get(index);
            endPosition = latLngList.get(next);
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(3000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            v = valueAnimator1.getAnimatedFraction();
            double lng = v * endPosition.longitude + (1 - v)
                    * startPosition.longitude;
            double lat = v * endPosition.latitude + (1 - v)
                    * startPosition.latitude;
            LatLng newPos = new LatLng(lat, lng);
            marker.setPosition(newPos);
            marker.setAnchor(0.5f, 0.5f);
            marker.setRotation(getBearing(startPosition, newPos));

        });
        valueAnimator.start();
    }


    private void getDirection(LatLng startPosition,LatLng endPosition) {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        String origin = "", dest = "";
        origin = startPosition.latitude + "," + startPosition.longitude;
        dest = endPosition.latitude + "," + endPosition.longitude;
        Log.e("origin",origin+"__"+dest);

        Api.getService("https://maps.googleapis.com/maps/api/")
                .getDirection(origin, dest, "rail", getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDirectionModel>() {
                    @Override
                    public void onResponse(Call<PlaceDirectionModel> call, Response<PlaceDirectionModel> response) {
                       dialog.dismiss();
                        if (response.body() != null && response.body().getRoutes().size() > 0) {
                            latLngList2.clear();
                            latLngList2.addAll(PolyUtil.decode(response.body().getRoutes().get(0).getOverview_polyline().getPoints()));
                            drawRoute(latLngList2);

                        } else {
                            dialog.dismiss();
                            Toast.makeText(FollowOrderActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PlaceDirectionModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            Toast.makeText(FollowOrderActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                        }
                    }
                });

    }

    private void drawRoute(List<LatLng> latLngList) {
        PolylineOptions options = new PolylineOptions();
        options.geodesic(true);
        options.color(ContextCompat.getColor(FollowOrderActivity.this, R.color.black));
        options.width(8.0f);
        options.startCap(new RoundCap());
        options.endCap(new RoundCap());
        options.jointType(JointType.ROUND);
        options.addAll(latLngList);
        mMap.addPolyline(options);
        getDriverLocation();


    }



    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req)
        {

        }
    }

    private void getDriverLocation(){
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Api.getService(Tags.base_url).getDriverLocation(userModel.getUser().getToken(),orderModel.getDriver().getId())
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                LatLng latLng = new LatLng(Double.parseDouble(response.body().getUser().getLatitude()),Double.parseDouble(response.body().getUser().getLongitude()));
                                if (latLngList.size() >= 2) {
                                    latLngList.remove(0);
                                }
                                latLngList.add(latLng);
                                addCarMarker();
                            }
                        } else {
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        dialog.dismiss();
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(FollowOrderActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(FollowOrderActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriverLocationChanged(FavoriteLocationModel favoriteLocationModel){

        LatLng latLng = new LatLng(favoriteLocationModel.getLat(),favoriteLocationModel.getLng());
        if (latLngList.size() >= 2) {
            latLngList.remove(0);
        }
        latLngList.add(latLng);
        addCarMarker();
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}




