package com.endpoint.giveme.activities_fragments.activity_setting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.endpoint.giveme.BuildConfig;
import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_intro_slider.IntroSliderActivity;
import com.endpoint.giveme.activities_fragments.activity_language.LanguageActivity;
import com.endpoint.giveme.activities_fragments.activity_login.LoginActivity;
import com.endpoint.giveme.activities_fragments.activity_sign_up.SignUpActivity;
import com.endpoint.giveme.activities_fragments.activity_sign_up_delegate.SignUpDelegateActivity;
import com.endpoint.giveme.databinding.ActivitySettingsBinding;
import com.endpoint.giveme.interfaces.Listeners;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.DefaultSettings;
import com.endpoint.giveme.models.SettingModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;

import java.io.IOException;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity implements Listeners.SettingAction {
    private ActivitySettingsBinding binding;
    private String lang;
    private DefaultSettings defaultSettings;
    private Preferences preferences;
    private UserModel userModel;
    private SettingModel settingModel;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        userModel = preferences.getUserData(this);


    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        defaultSettings = preferences.getAppSetting(this);
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.setLang(lang);
        binding.setActions(this);
        binding.close.setOnClickListener(v -> finish());
        binding.tvVersion.setText(BuildConfig.VERSION_NAME);

        if (defaultSettings!=null){
            if (defaultSettings.getRingToneName()!=null&&!defaultSettings.getRingToneName().isEmpty()){
                binding.tvRingtoneName.setText(defaultSettings.getRingToneName());
            }else {
                binding.tvRingtoneName.setText(getString(R.string.default1));
            }
        }else {
            binding.tvRingtoneName.setText(getString(R.string.default1));

        }

        if (userModel!=null){
            getUserData();


            if (userModel.getUser().getUser_type().equals("driver")){
                binding.llBeDriver.setVisibility(View.GONE);
                binding.viewBeDriver.setVisibility(View.GONE);
            }else {
                binding.llBeDriver.setVisibility(View.VISIBLE);
                binding.viewBeDriver.setVisibility(View.VISIBLE
                );
            }


        }
        getSetting();





    }

    private void getUserData() {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .getUserById(userModel.getUser().getToken(),lang,userModel.getUser().getId())
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {

                            userModel = response.body();
                            preferences.create_update_userdata(SettingsActivity.this,userModel);

                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error", response.code() + "__" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (response.code() == 500) {
                                Toast.makeText(SettingsActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SettingsActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage() + "__");
                        }
                    }
                });
    }

    private void getSetting(){
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url).getSetting(lang)
                .enqueue(new Callback<SettingModel>() {
                    @Override
                    public void onResponse(Call<SettingModel> call, Response<SettingModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                settingModel = response.body();


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
                    public void onFailure(Call<SettingModel> call, Throwable t) {
                        try {
                            dialog.dismiss();

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SettingsActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(SettingsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }


    @Override
    public void onTone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onComplaint() {
        if (settingModel!=null){
            Intent intent = new Intent(this, SignUpDelegateActivity.class);
            String url = Tags.base_url+settingModel.getSettings().getComplaint_list();
            intent.putExtra("url",url);
            startActivity(intent);
        }else {
            Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditProfile() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent,400);
    }

    @Override
    public void onLanguageSetting() {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivityForResult(intent, 200);
    }

    @Override
    public void onTerms() {
        if (settingModel!=null){
            Intent intent = new Intent(this, SignUpDelegateActivity.class);
            String url = Tags.base_url+settingModel.getSettings().getTerms_and_conditions();
            intent.putExtra("url",url);
            startActivity(intent);
        }else {
            Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPrivacy() {
        if (settingModel!=null){
            Intent intent = new Intent(this, SignUpDelegateActivity.class);
            String url = Tags.base_url+settingModel.getSettings().getPrivacy_policy();
            intent.putExtra("url",url);
            startActivity(intent);
        }else {
            Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRate() {
        String appId = getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        final List<ResolveInfo> otherApps = getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                rateIntent.setComponent(componentName);
                startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
            startActivity(webIntent);
        }
    }

    @Override
    public void onTour() {
        Intent intent = new Intent(this, IntroSliderActivity.class);
        intent.putExtra("type",1);
        startActivity(intent);
    }

    @Override
    public void onDelegate() {
        if (userModel!=null){
            if (userModel.getUser().getRegister_link()!=null&&!userModel.getUser().getRegister_link().isEmpty()){
                Intent intent = new Intent(this, SignUpDelegateActivity.class);
                String url = Tags.base_url+userModel.getUser().getRegister_link()+"&lang="+lang;
                intent.putExtra("url",url);
                startActivityForResult(intent,300);
            }else {
                Common.CreateDialogAlert(this,getString(R.string.inv_url));
            }


        }else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("from", false);
            startActivity(intent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);


            if (uri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(this,uri);
                String name = ringtone.getTitle(this);
                binding.tvRingtoneName.setText(name);

                if (defaultSettings==null){
                  defaultSettings = new DefaultSettings();
                }

                defaultSettings.setRingToneUri(uri.toString());
                defaultSettings.setRingToneName(name);
                preferences.createUpdateAppSetting(this,defaultSettings);


            }
        } else if (requestCode == 200 && resultCode == RESULT_OK ) {

            Intent intent = getIntent();
            intent.putExtra("action","language");
            setResult(RESULT_OK,intent);
            finish();
        }

        else if (requestCode == 300 && resultCode == RESULT_OK ) {
            userModel.getUser().setUser_type("driver");
            preferences.create_update_userdata(this,userModel);
            Intent intent = getIntent();
            intent.putExtra("action","update_user");
            setResult(RESULT_OK,intent);
            finish();
        }
        else if (requestCode == 400 && resultCode == RESULT_OK ) {
            userModel = preferences.getUserData(this);
            Intent intent = getIntent();
            intent.putExtra("action","update_user");
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}