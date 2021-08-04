package com.endpoint.giveme.activities_fragments.activity_home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionSet;
import android.util.Log;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_order.AddOrderActivity;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Main;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Notifications;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Profile;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Order;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.fragment_driver_order.Fragment_Driver_Order;
import com.endpoint.giveme.activities_fragments.activity_login.LoginActivity;
import com.endpoint.giveme.databinding.ActivityHomeBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.location_service.LocationService;
import com.endpoint.giveme.models.LocationModel;
import com.endpoint.giveme.models.NotFireModel;
import com.endpoint.giveme.models.UnReadCountModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private FragmentManager fragmentManager;
    private Fragment_Main fragment_main;
    private Fragment_Order fragment_order;
    private Fragment_Notifications fragment_notifications;
    private Fragment_Profile fragment_profile;
    private Fragment_Driver_Order fragment_driver_order;
    private UserModel userModel;
    private Preferences preferences;
    public double user_lat =0.0,user_lng=0.0;
    private String type = "";

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        userModel = preferences.getUserData(this);
        if (userModel!=null){
            if (userModel.getUser().getLogo() != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + userModel.getUser().getLogo())).placeholder(R.drawable.image_avatar).into(binding.imageProfile);
            } else {
                Picasso.get().load(R.drawable.image_avatar).into(binding.imageProfile);

            }
            updateFirebaseToken();

            if (fragment_profile!=null&&fragment_profile.isAdded()){
                fragment_profile.updateUi(userModel);
            }
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new TransitionSet());
            getWindow().setExitTransition(new TransitionSet());

        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        getDataFromIntent();
        initView();
        if (savedInstanceState==null){
            if (type.equals("notification")){
                displayFragmentNotification();
            }else if (type.equals("order")){
                if (userModel.getUser().getUser_type().equals("client")){
                    displayFragmentOrder();

                }else {
                    displayFragmentDriverOrder();
                }
            }else {
                displayFragmentMain();

            }
        }

    }
    private void getDataFromIntent()
    {
        Intent intent = getIntent();
        user_lat = intent.getDoubleExtra("lat",0.0);
        user_lng = intent.getDoubleExtra("lng",0.0);
        type = intent.getStringExtra("type");
    }
    private void initView()
    {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager!=null){
            manager.cancel(Tags.not_tag,Tags.not_id);
        }
        fragmentManager = getSupportFragmentManager();

        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
      //  binding.fab.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);


        binding.llStore.setOnClickListener(v -> {
            displayFragmentMain();
        });

        binding.llNotification.setOnClickListener(v -> {
            if (userModel == null) {
                navigateToLoginActivity(true);
            } else {
                displayFragmentNotification();

            }
        });

        binding.llOrder.setOnClickListener(v -> {
            if (userModel == null) {
                navigateToLoginActivity(true);
            } else {
                if (userModel.getUser().getUser_type().equals("client")){
                    displayFragmentOrder();

                }else {
                    displayFragmentDriverOrder();
                }

            }
        });

        binding.llProfile.setOnClickListener(v -> {
            if (userModel == null) {
                navigateToLoginActivity(true);
            } else {
                displayFragmentProfile();

            }
        });

        binding.fab2.setOnClickListener(v -> {

            if (userModel == null) {
                navigateToLoginActivity(true);
            } else {


                Intent intent = new Intent(this, AddOrderActivity.class);
                intent.putExtra("lat", user_lat);
                intent.putExtra("lng", user_lng);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, binding.fab2, binding.fab2.getTransitionName());
                    startActivity(intent, options.toBundle());

                } else {
                    startActivity(intent);

                }

            }



        });


        if (userModel != null) {

            if (userModel.getUser().getUser_type().equals("driver")){
                EventBus.getDefault().register(this);
            }

            if (userModel.getUser().getLogo() != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + userModel.getUser().getLogo())).placeholder(R.drawable.image_avatar).into(binding.imageProfile);
            } else {
                Picasso.get().load(R.drawable.image_avatar).into(binding.imageProfile);

            }
            getNotificationCount();
            updateFirebaseToken();
        }


        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

    }
    public void getNotificationCount()
    {
        Api.getService(Tags.base_url).getNotificationCount(userModel.getUser().getToken(),userModel.getUser().getId())
                .enqueue(new Callback<UnReadCountModel>() {
                    @Override
                    public void onResponse(Call<UnReadCountModel> call, Response<UnReadCountModel> response) {

                        if (response.isSuccessful()) {
                            int count = response.body().getCount_unread();
                            updateNotificationCount(count);
                        } else {

                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<UnReadCountModel> call, Throwable t) {
                        try {

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(HomeActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(HomeActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }
    public void updateNotificationCount(int count)
    {
        binding.setNotCount(count);

    }
    private void updateFirebaseToken()
    {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult().getToken();
                        try {
                            Api.getService(Tags.base_url)
                                    .updatePhoneToken(userModel.getUser().getToken(), token, userModel.getUser().getId(), "android")
                                    .enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                userModel.getUser().setFireBaseToken(token);
                                                preferences.create_update_userdata(HomeActivity.this, userModel);

                                                Log.e("token", "updated successfully");
                                            } else {
                                                try {

                                                    Log.e("errorToken", response.code() + "_" + response.errorBody().string());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            try {

                                                if (t.getMessage() != null) {
                                                    Log.e("errorToken2", t.getMessage());
                                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                                        Toast.makeText(HomeActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(HomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            } catch (Exception e) {
                                            }
                                        }
                                    });
                        } catch (Exception e) {

                        }
                    }
                });

    }
    public void updateUserData(UserModel userModel)
    {
        this.userModel = userModel;
        try {
            if (fragment_main!=null&&fragment_main.isAdded()){
                fragment_main.updateUserData(userModel);
            }

            if (fragment_notifications!=null&&fragment_notifications.isAdded()){
                fragment_notifications.updateUserData(userModel);
            }

        }catch (Exception e){

        }


    }
    public void displayFragmentMain()
    {
        updateMainUi();

        if (fragment_main == null) {
            fragment_main = Fragment_Main.newInstance(user_lat, user_lng);

        }

        if (fragment_order != null && fragment_order.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_order).commit();
        }
        if (fragment_driver_order!=null&&fragment_driver_order.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_driver_order).commit();

        }

        if (fragment_notifications != null && fragment_notifications.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_notifications).commit();
        }

        if (fragment_profile != null && fragment_profile.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_profile).commit();
        }


        if (fragment_main.isAdded()) {
            fragmentManager.beginTransaction().show(fragment_main).commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.fragment_app, fragment_main, "fragment_main").commit();
        }


    }
    private void displayFragmentNotification()
    {
        updateNotificationUi();


        if (fragment_notifications == null) {
            fragment_notifications = Fragment_Notifications.newInstance();
        }

        if (fragment_order != null && fragment_order.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_order).commit();
        }
        if (fragment_driver_order!=null&&fragment_driver_order.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_driver_order).commit();

        }

        if (fragment_main != null && fragment_main.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_main).commit();
        }

        if (fragment_profile != null && fragment_profile.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_profile).commit();
        }


        if (fragment_notifications.isAdded()) {
            fragmentManager.beginTransaction().show(fragment_notifications).commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.fragment_app, fragment_notifications, "fragment_notifications").commit();
        }

    }
    private void displayFragmentOrder()
    {
        updateOrderUi();

        if (fragment_order == null) {
            fragment_order = Fragment_Order.newInstance();
        }

        if (fragment_main != null && fragment_main.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_main).commit();
        }

        if (fragment_notifications != null && fragment_notifications.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_notifications).commit();
        }

        if (fragment_profile != null && fragment_profile.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_profile).commit();
        }


        if (fragment_order.isAdded()) {
            fragmentManager.beginTransaction().show(fragment_order).commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.fragment_app, fragment_order, "fragment_order").commit();
        }

    }
    private void displayFragmentDriverOrder()
    {
        updateOrderUi();

        if (fragment_driver_order == null) {
            fragment_driver_order = Fragment_Driver_Order.newInstance();
        }

        if (fragment_main != null && fragment_main.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_main).commit();
        }

        if (fragment_order != null && fragment_order.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_order).commit();
        }

        if (fragment_notifications != null && fragment_notifications.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_notifications).commit();
        }

        if (fragment_profile != null && fragment_profile.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_profile).commit();
        }


        if (fragment_driver_order.isAdded()) {
            fragmentManager.beginTransaction().show(fragment_driver_order).commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.fragment_app, fragment_driver_order, "fragment_driver_order").commit();
        }

    }
    private void displayFragmentProfile()
    {
        updateProfileUi();

        if (fragment_profile == null) {
            fragment_profile = Fragment_Profile.newInstance();
        }

        if (fragment_order != null && fragment_order.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_order).commit();
        }

        if (fragment_driver_order!=null&&fragment_driver_order.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_driver_order).commit();

        }

        if (fragment_notifications != null && fragment_notifications.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_notifications).commit();
        }

        if (fragment_main != null && fragment_main.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment_main).commit();
        }


        if (fragment_profile.isAdded()) {
            fragmentManager.beginTransaction().show(fragment_profile).commit();
            fragment_profile.getBalance();
        } else {
            fragmentManager.beginTransaction().add(R.id.fragment_app, fragment_profile, "fragment_profile").commit();
        }




    }
    private void updateMainUi()
    {
        binding.iconStore.setImageResource(R.drawable.shop1);
        binding.iconNotification.setImageResource(R.drawable.mega_phone2);
        binding.iconOrder.setImageResource(R.drawable.truck2);


        binding.tvStore.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.gray11));

    }
    private void updateNotificationUi()
    {

        binding.iconStore.setImageResource(R.drawable.shop2);
        binding.iconNotification.setImageResource(R.drawable.mega_phone1);
        binding.iconOrder.setImageResource(R.drawable.truck2);

        binding.tvStore.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.gray11));

    }
    private void updateOrderUi()
    {


        binding.iconStore.setImageResource(R.drawable.shop2);
        binding.iconNotification.setImageResource(R.drawable.mega_phone2);
        binding.iconOrder.setImageResource(R.drawable.truck1);

        binding.tvStore.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.gray11));
    }
    private void updateProfileUi()
    {

        binding.iconStore.setImageResource(R.drawable.shop2);
        binding.iconNotification.setImageResource(R.drawable.mega_phone2);
        binding.iconOrder.setImageResource(R.drawable.truck2);

        binding.tvStore.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this, R.color.gray11));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void driverLocationUpdate(LocationModel locationModel)
    {
        user_lat = locationModel.getLat();
        user_lng = locationModel.getLng();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderUpdated(NotFireModel notFireModel)
    {
        getNotificationCount();
        if (fragment_notifications!=null&&fragment_notifications.isAdded()){
            fragment_notifications.getNotifications();
        }
        if (fragment_order!=null&&fragment_order.isAdded()){
            fragment_order.getOrders();
        }

        if (fragment_driver_order!=null&&fragment_driver_order.isAdded()){
            fragment_driver_order.updateData();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }
    public void navigateToLoginActivity(boolean hasData)
    {

        Intent intent = new Intent(this, LoginActivity.class);
        if (hasData) {
            intent.putExtra("from", false);
        }
        startActivity(intent);
        if (!hasData) {
            finish();

        }
    }
    public void refreshActivity()
    {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    public void logout()
    {
        if (userModel != null) {

            try {
                if (userModel.getUser().getUser_type().equals("driver")){
                    Intent intent = new Intent(this,LocationService.class);
                    stopService(intent);
                }
            }catch (Exception e){}


            ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
            dialog.show();

            Api.getService(Tags.base_url)
                    .logout(userModel.getUser().getToken(), userModel.getUser().getFireBaseToken(), "android")
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            dialog.dismiss();
                            if (response.isSuccessful()) {
                                preferences.clear(HomeActivity.this);
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                if (manager != null) {
                                    manager.cancel(Tags.not_tag, Tags.not_id);
                                }
                                navigateToLoginActivity(false);


                            } else {
                                dialog.dismiss();
                                try {
                                    Log.e("error", response.code() + "__" + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (response.code() == 500) {
                                    Toast.makeText(HomeActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(HomeActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(HomeActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(HomeActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("Error", e.getMessage() + "__");
                            }
                        }
                    });
        }

    }

    @Override
    public void onBackPressed()
    {
        if (fragment_main != null && fragment_main.isAdded() && fragment_main.isVisible()) {
            if (userModel == null) {
                navigateToLoginActivity(false);
            } else {
                finish();
            }
        } else {
            displayFragmentMain();
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}
