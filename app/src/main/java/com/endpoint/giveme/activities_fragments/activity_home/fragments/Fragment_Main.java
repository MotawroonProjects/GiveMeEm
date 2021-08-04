package com.endpoint.giveme.activities_fragments.activity_home.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_delegate_orders.DelegateOrdersActivity;
import com.endpoint.giveme.activities_fragments.activity_home.HomeActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_custom_query.ShopsCustomQueryActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_map.ShopMapActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_products.ShopProductActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_query.ShopsQueryActivity;
import com.endpoint.giveme.activities_fragments.activity_shops.ShopsActivity;
import com.endpoint.giveme.adapters.MainAdapter;
import com.endpoint.giveme.databinding.FragmentMainBinding;
import com.endpoint.giveme.models.CategoryModel;
import com.endpoint.giveme.models.CustomPlaceModel;
import com.endpoint.giveme.models.CustomShopDataModel;
import com.endpoint.giveme.models.HourModel;
import com.endpoint.giveme.models.MainItemData;
import com.endpoint.giveme.models.NearbyModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class Fragment_Main extends Fragment {
    private final static String TAG1 = "lat";
    private final static String TAG2 = "lng";
    private double user_lat = 0.0, user_lng = 0.0;
    private HomeActivity activity;
    private FragmentMainBinding binding;
    private String lang;
    private List<MainItemData> mainItemDataList;
    private MainAdapter mainAdapter;
    private Preferences preferences;
    private UserModel userModel;
    private String currency;



    public static Fragment_Main newInstance(double user_lat, double user_lng) {
        Bundle bundle = new Bundle();
        bundle.putDouble(TAG1, user_lat);
        bundle.putDouble(TAG2, user_lng);
        Fragment_Main fragment_main = new Fragment_Main();
        fragment_main.setArguments(bundle);
        return fragment_main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        mainItemDataList = new ArrayList<>();

        activity = (HomeActivity) getActivity();
        preferences= Preferences.getInstance();
        userModel = preferences.getUserData(activity);
        Paper.init(activity);
        lang = Paper.book().read("lang", "ar");
        Bundle bundle = getArguments();
        if (bundle != null) {
            user_lat = bundle.getDouble(TAG1);
            user_lng = bundle.getDouble(TAG2);
        }


        binding.recViewCategory.setLayoutManager(new LinearLayoutManager(activity));
        MainItemData itemData1 = new MainItemData(0);
        mainItemDataList.add(itemData1);
        currency=getString(R.string.sar);
        getUserData();

        mainAdapter = new MainAdapter(mainItemDataList,activity,this,user_lat,user_lng,currency);
        binding.recViewCategory.setAdapter(mainAdapter);

        MainItemData itemData2 = new MainItemData(1);
        mainItemDataList.add(itemData2);
        mainAdapter.notifyItemInserted(mainItemDataList.size()-1);
        binding.consSearch.setOnClickListener(v -> {

            Intent intent = new Intent(activity, ShopsActivity.class);
            intent.putExtra("lat", user_lat);
            intent.putExtra("lng", user_lng);
            intent.putExtra("type", true);
            startActivity(intent);
        });
        binding.imageDelegateOrders.setOnClickListener(v -> {
            Intent intent = new Intent(activity, DelegateOrdersActivity.class);
            startActivity(intent);
        });

    }

    private void getUserData() {

        if (userModel!=null){
            //currency = userModel.getUser().getCountry().getWord().getCurrency();
            binding.setModel(userModel);
        }
    }

    public void updateUserData(UserModel userModel){
        this.userModel= userModel;
        getUserData();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void stopTimer() {
        if (mainAdapter!=null){
            mainAdapter.stopTimer();
        }
    }


    public void placeItemData(NearbyModel.Result placeModel) {


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
                Intent intent = new Intent(activity, ShopProductActivity.class);
                intent.putExtra("data",customShopDataModel);
                startActivity(intent);


            }else {
                Log.e("Ddd","fff");
                Intent intent = new Intent(activity, ShopDetailsActivity.class);
                intent.putExtra("data",placeModel);
                startActivity(intent);
            }

        }else {
            Log.e("place_id",placeModel.getPlace_id());
            Intent intent = new Intent(activity, ShopMapActivity.class);
            intent.putExtra("data",placeModel);
            startActivity(intent);
        }


    }

    private List<HourModel> getHours(NearbyModel.Result placeModel) {
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

    private boolean isRestaurant(NearbyModel.Result result){

        for (String type :result.getTypes()){
            if (type.equals("restaurant")){
                return true;
            }
        }

        return false;
    }

    public void setCategoryData(CategoryModel categoryModel) {
        Intent intent;
        if (categoryModel.getType().equals("google")){
            intent = new Intent(activity, ShopsQueryActivity.class);

        }else {
            intent = new Intent(activity, ShopsCustomQueryActivity.class);

        }


        intent.putExtra("lat",user_lat);
        intent.putExtra("lng",user_lng);

        intent.putExtra("data",categoryModel);
        startActivity(intent);

    }
}
