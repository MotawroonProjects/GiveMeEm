package com.endpoint.giveme.activities_fragments.activity_confirm_code_success;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_login.LoginActivity;
import com.endpoint.giveme.activities_fragments.activity_sign_up.SignUpActivity;
import com.endpoint.giveme.activities_fragments.activity_splash_loading.SplashLoadingActivity;
import com.endpoint.giveme.databinding.ActivityConfirmCodeSuccessBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmCodeSuccessActivity extends AppCompatActivity {
    private ActivityConfirmCodeSuccessBinding binding;
    private String phone_code = "";
    private String phone = "";
    private String country_id="";
    private Preferences preferences;
    private String lang;
    private boolean fromSplash = true;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            phone_code = intent.getStringExtra("phone_code");
            phone = intent.getStringExtra("phone");
            country_id = intent.getStringExtra("country_id");
            fromSplash = intent.getBooleanExtra("from",true);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_confirm_code_success);
        getDataFromIntent();
        initView();
    }

    private void initView() {
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        preferences = Preferences.getInstance();
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        binding.setLang(lang);
        login();
    }

    private void login() {
        Api.getService(Tags.base_url)
                .login(phone_code, phone)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            preferences.create_update_userdata(ConfirmCodeSuccessActivity.this, response.body());
                            new Handler()
                                    .postDelayed(()->{
                                        binding.progBar.setVisibility(View.GONE);
                                        binding.image.setVisibility(View.VISIBLE);
                                        binding.tvStatus.setTextColor(ContextCompat.getColor(ConfirmCodeSuccessActivity.this,R.color.colorPrimary));
                                        binding.tvStatus.setText(getString(R.string.confirmed));
                                        if (fromSplash){

                                            navigateToSplashLoading();

                                        }else {
                                            finish();
                                        }

                                    },1500);

                        } else {



                            if (response.code() == 500) {
                                Toast.makeText(ConfirmCodeSuccessActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                navigateToLoginActivity();

                            } else if (response.code() == 404) {
                                new Handler()
                                        .postDelayed(()->{
                                            binding.progBar.setVisibility(View.GONE);
                                            binding.image.setVisibility(View.VISIBLE);
                                            binding.tvStatus.setTextColor(ContextCompat.getColor(ConfirmCodeSuccessActivity.this,R.color.colorPrimary));
                                            binding.tvStatus.setText(getString(R.string.confirmed));
                                            navigateToSignUpActivity();

                                        },1500);
                            } else {
                                navigateToLoginActivity();
                                Toast.makeText(ConfirmCodeSuccessActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            Log.e("1","1");
                            binding.progBar.setVisibility(View.GONE);
                            binding.image.setVisibility(View.VISIBLE);
                            binding.tvStatus.setTextColor(ContextCompat.getColor(ConfirmCodeSuccessActivity.this,R.color.colorPrimary));
                            binding.tvStatus.setText(getString(R.string.confirmed));
                            navigateToLoginActivity();

                            if (t.getMessage() != null) {

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ConfirmCodeSuccessActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ConfirmCodeSuccessActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage() + "__");
                        }
                    }
                });
    }


    private void navigateToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void navigateToSplashLoading() {
        Intent intent = new Intent(this, SplashLoadingActivity.class);
        startActivity(intent);
        finish();

    }

    private void navigateToSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        intent.putExtra("phone_code",phone_code);
        intent.putExtra("phone",phone);
        intent.putExtra("country_id",country_id);
        intent.putExtra("from",fromSplash);
        startActivity(intent);
        finish();

    }

}