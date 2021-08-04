package com.endpoint.giveme.activities_fragments.activity_driver_update_location;

import androidx.annotation.NonNull;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.ActivityDriverUpdateLocationBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverUpdateLocationActivity extends AppCompatActivity implements OnMapReadyCallback  {
    private ActivityDriverUpdateLocationBinding binding;
    private String lang;
    private GoogleMap mMap;
    private String currency;
    private UserModel userModel;
    private Preferences preferences;
    private OrderModel orderModel;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private LatLng oldLatLng,newLatLng=null;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_update_location);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        orderModel = (OrderModel) intent.getSerializableExtra("data");


    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        currency = getString(R.string.sar);
        /*if (userModel!=null){
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }*/
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setModel(orderModel);
        binding.close.setOnClickListener(v -> {super.onBackPressed();});
        updateUI();

    }

    private void checkPermission()
    {
        if (ActivityCompat.checkSelfPermission(this,fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{fineLocPerm}, loc_req);
        } else {
            mMap.setMyLocationEnabled(true);
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
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            addMarker(Double.parseDouble(orderModel.getMarket_latitude()),Double.parseDouble(orderModel.getMarket_longitude()),1);
            addMarker(Double.parseDouble(orderModel.getClient_latitude()),Double.parseDouble(orderModel.getClient_longitude()),2);
            checkPermission();
            mMap.setOnMyLocationChangeListener(location -> updateLocation(location));


        }
    }

    private void updateLocation(Location location) {
        if (oldLatLng==null){
            oldLatLng = new LatLng(location.getLatitude(),location.getLongitude());
            sendLocation(oldLatLng);
        }else {
            newLatLng = new LatLng(location.getLatitude(),location.getLongitude());
            double distance = getDistance(oldLatLng,newLatLng);

            if (distance>=2){
                sendLocation(newLatLng);
            }
        }




    }

    private void sendLocation(LatLng latLng){
        Api.getService(Tags.base_url).updateDriverLocation(userModel.getUser().getToken(), orderModel.getDriver().getId(), orderModel.getId(),latLng.latitude,latLng.longitude)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                oldLatLng = latLng;
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
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(DriverUpdateLocationActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(DriverUpdateLocationActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
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

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mMap.setMyLocationEnabled(true);
            }else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private double getDistance(LatLng latLng1, LatLng latLng2) {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2);
    }
}




