package com.endpoint.giveme.activities_fragments.activity_map_show_location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.ActivityMapShowLocationBinding;
import com.endpoint.giveme.interfaces.Listeners;
import com.endpoint.giveme.language.Language;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import io.paperdb.Paper;

public class MapShowLocationActivity extends AppCompatActivity implements OnMapReadyCallback, Listeners.BackListener {
    private ActivityMapShowLocationBinding binding;
    private String lang;
    private double lat = 0.0, lng = 0.0;
    private String address = "";
    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 15.0f;



    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_map_show_location);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        lat = intent.getDoubleExtra("lat",0.0);
        lng = intent.getDoubleExtra("lng",0.0);

    }

    private void initView() {

        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setListener(this);
        binding.setAddress(address);
        updateUI();






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
            AddMarker(lat,lng);



        }
    }




    private void AddMarker(double lat, double lng) {

        this.lat = lat;
        this.lng = lng;



        if (marker == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.map_pin,null);
            IconGenerator iconGenerator = new IconGenerator(this);
            iconGenerator.setBackground(null);
            iconGenerator.setContentView(view);
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())));
        }
        marker.setDraggable(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }





    @Override
    public void back() {
        super.onBackPressed();

    }


    @Override
    public void onBackPressed() {
        back();
    }



}


