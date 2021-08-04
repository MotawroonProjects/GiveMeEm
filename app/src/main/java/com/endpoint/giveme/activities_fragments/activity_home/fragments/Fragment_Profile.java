package com.endpoint.giveme.activities_fragments.activity_home.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_coupon.AddCouponActivity;
import com.endpoint.giveme.activities_fragments.activity_home.HomeActivity;
import com.endpoint.giveme.activities_fragments.activity_setting.SettingsActivity;
import com.endpoint.giveme.activities_fragments.activity_user_feedback.UserFeedbackActivity;
import com.endpoint.giveme.databinding.FragmentProfileBinding;
import com.endpoint.giveme.interfaces.Listeners;
import com.endpoint.giveme.location_service.LocationService;
import com.endpoint.giveme.models.BalanceModel;
import com.endpoint.giveme.models.SettingModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Profile extends Fragment implements Listeners.ProfileAction {
    private FragmentProfileBinding binding;
    private HomeActivity activity;
    private String lang;
    private UserModel userModel;
    private Preferences preferences;
    private String currency;

    public static Fragment_Profile newInstance(){
        return new Fragment_Profile();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        activity = (HomeActivity) getActivity();
        preferences = Preferences.getInstance();
        userModel =preferences.getUserData(activity);
        currency = getString(R.string.sar);
       /* if (userModel != null) {
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }*/
        binding.setRate(0.0);
        Paper.init(activity);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setActions(this);
        binding.setModel(userModel);

       updateUi(userModel);
       getBalance();

    }

    public void updateUi(UserModel userModel) {
        this.userModel = userModel;
        binding.setModel(userModel);

        if (userModel!=null){
            if (userModel.getUser().getLogo() != null) {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + userModel.getUser().getLogo())).placeholder(R.drawable.image_avatar).into(binding.image);
            } else {
                Picasso.get().load(R.drawable.image_avatar).into(binding.image);

            }

            if (userModel.getUser().getUser_type().equals("driver")){
                try {
                    Intent intent = new Intent(activity, LocationService.class);
                    activity.startService(intent);
                }catch (Exception e){}
            }
        }



        getBalance();
    }

    public void getBalance(){
        Api.getService(Tags.base_url).getUserBalance(userModel.getUser().getToken(), userModel.getUser().getId())
                .enqueue(new Callback<BalanceModel>() {
                    @Override
                    public void onResponse(Call<BalanceModel> call, Response<BalanceModel> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                binding.tvBalance.setText(String.format(Locale.ENGLISH,"%s %s",response.body().getUser_balance(),currency));
                                binding.tvTotalRevenue.setText(String.format(Locale.ENGLISH,"%s %s",response.body().getDelivery_fee(),currency));
                                binding.tvOrderNum.setText(String.format(Locale.ENGLISH,"%s %s",response.body().getOrders(),activity.getString(R.string.order2)));
                                binding.setRate(response.body().getMy_rate());
                                if (response.body().getUser_balance()>=0){
                                    binding.tvBalance.setTextColor(ContextCompat.getColor(activity,R.color.colorPrimary));
                                }else {
                                    binding.tvBalance.setTextColor(ContextCompat.getColor(activity,R.color.color_red));

                                }
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
                    public void onFailure(Call<BalanceModel> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    @Override
    public void onReviews() {

    }

    @Override
    public void onFeedback() {
        Intent intent = new Intent(activity, UserFeedbackActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCoupons() {

    }

    @Override
    public void onAddCoupon() {
        Intent intent = new Intent(activity, AddCouponActivity.class);
        startActivityForResult(intent,100);

    }

    @Override
    public void onSetting() {
        Intent intent = new Intent(activity, SettingsActivity.class);
        startActivityForResult(intent,200);
    }

    @Override
    public void onPayment() {

    }

    @Override
    public void onTelegram() {
        ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
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
                                String telegramUrl = response.body().getSettings().getTelegram();
                                String url_pattern ="^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
                                if (telegramUrl.matches(url_pattern)){
                                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(telegramUrl));
                                    startActivity(intent);
                                }else {
                                    Common.CreateDialogAlert(activity,getString(R.string.inv_telegram_url));
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
                    public void onFailure(Call<SettingModel> call, Throwable t) {
                        try {
                            dialog.dismiss();

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    @Override
    public void onNotification() {

    }

    @Override
    public void logout() {

        activity.logout();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK &&data!=null) {

            String action = data.getStringExtra("action");
            if (action.equals("language")){
                activity.refreshActivity();

            }else {

                userModel = preferences.getUserData(activity);
                binding.setModel(userModel);
                updateUi(userModel);
                activity.updateUserData(userModel);

            }
        }
    }
}
