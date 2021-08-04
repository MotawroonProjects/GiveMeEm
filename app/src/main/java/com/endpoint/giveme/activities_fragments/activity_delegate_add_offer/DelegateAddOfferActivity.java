package com.endpoint.giveme.activities_fragments.activity_delegate_add_offer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.ActivityDelegateAddOfferBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.FromToLocationModel;
import com.endpoint.giveme.models.RangeOfferModel;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DelegateAddOfferActivity extends AppCompatActivity implements OnMapReadyCallback{
    private ActivityDelegateAddOfferBinding binding;
    private String lang;
    private GoogleMap mMap;
    private FromToLocationModel fromToLocationModel;
    private int client_id,driver_id,order_id;
    private String user_token="";
    private String currency;
    private UserModel userModel;
    private Preferences preferences;
    private RangeOfferModel rangeOfferModel;



    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_delegate_add_offer);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        fromToLocationModel = (FromToLocationModel) intent.getSerializableExtra("data");
        client_id = intent.getIntExtra("client_id",0);
        driver_id = intent.getIntExtra("driver_id",0);
        order_id = intent.getIntExtra("order_id",0);
        user_token = intent.getStringExtra("user_token");

    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        currency = getString(R.string.sar);
       /* if (userModel!=null){
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }*/
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setModel(fromToLocationModel);
        binding.close.setOnClickListener(v -> {super.onBackPressed();});
        binding.tvDropOffLocationDistance.setText(String.format(Locale.ENGLISH,"%.2f %s",fromToLocationModel.getDistance_me_drop_off_location(),getString(R.string.km)));
        binding.tvPickUpLocationDistance.setText(String.format(Locale.ENGLISH,"%.2f %s",fromToLocationModel.getDistance_me_pick_up_location(),getString(R.string.km)));
        binding.tvSend.setOnClickListener(v -> {
            try {
                double offer = Double.parseDouble(binding.edtOffer.getText().toString().trim());
                if (rangeOfferModel!=null){
                    if (rangeOfferModel.getMax_offer().equals("0")){
                        if (offer >= Double.parseDouble(rangeOfferModel.getMin_offer())){
                            sendOffer(String.valueOf(offer));
                        }else {
                            Common.CreateDialogAlert(this,String.format(Locale.ENGLISH,"%s %s %s",getString(R.string.min_value),rangeOfferModel.getMin_offer(),currency));
                        }
                    }else {

                        if (offer >= Double.parseDouble(rangeOfferModel.getMin_offer())&& offer <= Double.parseDouble(rangeOfferModel.getMax_offer())){
                            sendOffer(String.valueOf(offer));

                        }else {
                            Common.CreateDialogAlert(this,String.format(Locale.ENGLISH,"%s %s %s %s %s %s",getString(R.string.min_value),rangeOfferModel.getMin_offer(),currency,getString(R.string.max_value),rangeOfferModel.getMax_offer(),currency));

                        }


                    }
                }else {
                    Common.CreateDialogAlert(this,getString(R.string.inv_offer));
                }
            }catch (Exception e){

            }


        });
        updateUI();
        getOfferRange();
    }

    private void getOfferRange() {
        try {
            ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
            Api.getService(Tags.base_url).getOfferRange(user_token,client_id,driver_id,order_id, fromToLocationModel.getFrom_location_to_location_distance())
                    .enqueue(new Callback<RangeOfferModel>() {
                        @Override
                        public void onResponse(Call<RangeOfferModel> call, Response<RangeOfferModel> response) {
                            dialog.dismiss();
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    rangeOfferModel = response.body();

                                    if (rangeOfferModel.getMax_offer().equals("0")){
                                        binding.edtOffer.setHint(String.format(Locale.ENGLISH,"%s %s %s",getString(R.string.min_value),response.body().getMin_offer(),currency));

                                    }else {
                                        binding.edtOffer.setHint(String.format(Locale.ENGLISH,"%s %s %s %s %s %s",getString(R.string.min_value),response.body().getMin_offer(),currency,getString(R.string.max_value),response.body().getMax_offer(),currency));

                                    }

                                }
                            } else {
                                dialog.dismiss();
                                try {
                                    Log.e("error_code", response.code() + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


                        }

                        @Override
                        public void onFailure(Call<RangeOfferModel> call, Throwable t) {
                            try {
                                dialog.dismiss();
                                if (t.getMessage() != null) {
                                    Log.e("error", t.getMessage() + "__");

                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        Toast.makeText(DelegateAddOfferActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                    } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                    } else {
                                        Toast.makeText(DelegateAddOfferActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    }
                                }


                            } catch (Exception e) {

                            }
                        }
                    });
        }catch (Exception e){

        }

    }

    private void sendOffer(String offer_value){
        try {
            ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
            Api.getService(Tags.base_url).sendDriverOffer(user_token,client_id,driver_id,order_id,offer_value,rangeOfferModel.getMin_offer(),"make_offer")
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            dialog.dismiss();
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            } else {

                                if (response.code()==406){
                                    Common.CreateDialogAlert(DelegateAddOfferActivity.this,getString(R.string.other_delegate_accept_order));
                                }else if (response.code()==409){
                                    Common.CreateDialogAlert(DelegateAddOfferActivity.this,getString(R.string.order_canceled2));

                                }
                                dialog.dismiss();
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
                                dialog.dismiss();
                                if (t.getMessage() != null) {
                                    Log.e("error", t.getMessage() + "__");

                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        Toast.makeText(DelegateAddOfferActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                    } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                    } else {
                                        Toast.makeText(DelegateAddOfferActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    }
                                }


                            } catch (Exception e) {

                            }
                        }
                    });
        }catch (Exception e ){

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
            addMarker(fromToLocationModel.getFromLat(),fromToLocationModel.getFromLng(),1);
            addMarker(fromToLocationModel.getToLat(),fromToLocationModel.getToLng(),2);

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
            builder.include(new LatLng(fromToLocationModel.getFromLat(),fromToLocationModel.getFromLng()));
            builder.include(new LatLng(fromToLocationModel.getToLat(),fromToLocationModel.getToLng()));

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




}