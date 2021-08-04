package com.endpoint.giveme.activities_fragments.activity_add_order;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_coupon.AddCouponActivity;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_map_search.MapSearchActivity;
import com.endpoint.giveme.activities_fragments.activity_package_map.PackageMapActivity;
import com.endpoint.giveme.activities_fragments.activity_shops.ShopsActivity;
import com.endpoint.giveme.adapters.ChatBotAdapter;
import com.endpoint.giveme.databinding.ActivityAddOrderBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.AddOrderTextModel;
import com.endpoint.giveme.models.ChatBotModel;
import com.endpoint.giveme.models.CouponModel;
import com.endpoint.giveme.models.FavoriteLocationModel;
import com.endpoint.giveme.models.NearbyModel;
import com.endpoint.giveme.models.SingleOrderDataModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOrderActivity extends AppCompatActivity {
    private ActivityAddOrderBinding binding;
    private ChatBotAdapter adapter;
    private List<ChatBotModel> chatBotModelList;
    private double user_lat;
    private double user_lng;
    private int shopListPos = -1;
    private int write_order_details_pos = -1;
    private int order_details_pos = -1;
    private int drop_off_pos = -1;
    private int share_location_pos = -1;
    private int coupon_pos = -1;
    private boolean mapLocation = false;
    private AddOrderTextModel addOrderTextModel;
    private FavoriteLocationModel fromLocation, toLocation;
    private boolean isPackageOrder = false;
    private Preferences preferences;
    private UserModel userModel;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new TransitionSet());
            getWindow().setExitTransition(new TransitionSet());

        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_order);
        getDataFromIntent();
        initView();
    }


    private void getDataFromIntent() {
        Intent intent = getIntent();
        user_lat = intent.getDoubleExtra("lat", 0.0);
        user_lng = intent.getDoubleExtra("lng", 0.0);
    }

    private void initView()
    {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        chatBotModelList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
        String time = dateFormat.format(new Date(calendar.getTimeInMillis()));
        binding.tvTime.setText(time);
        String am_pm = time.substring(time.length() - 2);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatBotAdapter(this, chatBotModelList, userModel.getUser().getName(),userModel.getUser().getLogo(), am_pm.toLowerCase());
        binding.recView.setAdapter(adapter);
        startChat();


        binding.cardRestart.setOnClickListener(v -> startChat());
        binding.close.setOnClickListener(v -> super.onBackPressed());
        binding.imageCloseSheet.setOnClickListener(v -> onBackPressed());
        binding.edtDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    binding.btnDone.setBackgroundResource(R.drawable.small_rounded_dark_gray);
                } else {
                    binding.btnDone.setBackgroundResource(R.drawable.small_rounded_primary);

                }
            }
        });

        binding.btnDone.setOnClickListener(v -> {
            String details = binding.edtDetails.getText().toString().trim();
            if (!details.isEmpty()) {
                addOrderDetails(details);
            }
        });

    }
    private void startChat()
    {
        try {
            shopListPos =-1;
            write_order_details_pos = -1;
            order_details_pos = -1;
            drop_off_pos = -1;
            share_location_pos = -1;
            coupon_pos = -1;
            isPackageOrder = false;
            mapLocation = false;
            binding.cardRestart.setVisibility(View.GONE);
            binding.edtDetails.setText(null);
            chatBotModelList.clear();
            adapter.notifyDataSetChanged();
            addOrderTextModel = null;
            addOrderTextModel = new AddOrderTextModel();
            addOrderTextModel.setOrder_type("google_market");

            ChatBotModel chatBotModel = createInstance(ChatBotAdapter.empty);
            chatBotModelList.add(chatBotModel);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);

            chatBotModelList.add(null);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);


            new Handler()
                    .postDelayed(() -> {
                        chatBotModelList.remove(chatBotModelList.size() - 1);
                        adapter.notifyItemRemoved(chatBotModelList.size() - 1);
                        ChatBotModel chatBotModel1 = createInstance(ChatBotAdapter.greeting);
                        chatBotModelList.add(chatBotModel1);
                        adapter.notifyItemInserted(chatBotModelList.size() - 1);

                        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                        new Handler().postDelayed(() -> {

                            new Handler().postDelayed(() -> {

                                chatBotModelList.add(null);
                                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                new Handler().postDelayed(() -> {

                                    chatBotModelList.remove(chatBotModelList.size() - 1);
                                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                                    ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.welcome);
                                    chatBotModelList.add(chatBotModel2);
                                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                    new Handler().postDelayed(() -> {

                                        chatBotModelList.add(null);
                                        adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                        new Handler().postDelayed(() -> {
                                            chatBotModelList.remove(chatBotModelList.size() - 1);
                                            adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                                            ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.help);
                                            chatBotModelList.add(chatBotModel3);
                                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                        }, 1000);


                                    }, 1000);


                                }, 1000);


                            }, 1000);


                        }, 1000);


                    }, 3000);
        }catch (Exception e){

        }

    }
    private ChatBotModel createInstance(int type)
    {
        ChatBotModel chatBotModel = new ChatBotModel();
        chatBotModel.setType(type);
        return chatBotModel;
    }
    public void addOrder_Package(String action, int adapterPosition)
    {
        try {
            binding.cardRestart.setVisibility(View.VISIBLE);
            ChatBotModel chatBotModel1 = chatBotModelList.get(adapterPosition);
            chatBotModel1.setEnabled(false);
            chatBotModelList.set(adapterPosition, chatBotModel1);
            adapter.notifyItemChanged(adapterPosition);


            ChatBotModel chatBotModel = createInstance(ChatBotAdapter.new_order);
            chatBotModel.setText(action);
            chatBotModelList.add(chatBotModel);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


            new Handler().postDelayed(() -> {
                chatBotModelList.add(null);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                new Handler().postDelayed(() -> {
                    chatBotModelList.remove(chatBotModelList.size() - 1);
                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                    ChatBotModel chatBotModel2;
                    if (action.equals(getString(R.string.new_order))) {

                        chatBotModel2 = createInstance(ChatBotAdapter.store);

                    } else {
                        chatBotModel2 = createInstance(ChatBotAdapter.share_location);
                        addOrderTextModel.setOrder_type("tard_emdad");


                    }

                    chatBotModelList.add(chatBotModel2);
                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                }, 1000);
            }, 1000);
        }catch (Exception e){}


    }
    public void openShops_Maps(int adapterPosition, String action)
    {
        try {
            shopListPos = adapterPosition;

            if (action.equals(getString(R.string.shop_list))) {
                Intent intent = new Intent(this, ShopsActivity.class);
                intent.putExtra("lat", user_lat);
                intent.putExtra("lng", user_lng);
                startActivityForResult(intent, 100);
            } else {
                navigateToMapSearch(500);

            }
        }catch (Exception e){}



    }
    private void addOrderDetails(String details)
    {
        try {
            addOrderTextModel.setOrder_text(details);
            Common.CloseKeyBoard(this, binding.edtDetails);
            closeSheet();

            if (order_details_pos == -1) {
                ChatBotModel chatBotModel2 = chatBotModelList.get(write_order_details_pos);
                chatBotModel2.setEnabled(false);
                chatBotModelList.set(write_order_details_pos, chatBotModel2);
                adapter.notifyItemChanged(write_order_details_pos);


                ChatBotModel chatBotModel = createInstance(ChatBotAdapter.order_details);
                chatBotModel.setText(details);

                chatBotModelList.add(chatBotModel);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                new Handler()
                        .postDelayed(() -> {
                            chatBotModelList.add(null);
                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                            new Handler()
                                    .postDelayed(() -> {
                                        chatBotModelList.remove(chatBotModelList.size() - 1);
                                        adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                                        if (isPackageOrder){

                                            ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.use_coupon);
                                            chatBotModelList.add(chatBotModel3);
                                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                            new Handler().postDelayed(() -> {
                                                chatBotModelList.add(null);
                                                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                                                new Handler().postDelayed(() -> {
                                                    chatBotModelList.remove(chatBotModelList.size() - 1);
                                                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);


                                                    ChatBotModel chatBotModel4 = createInstance(ChatBotAdapter.add_coupon);
                                                    chatBotModelList.add(chatBotModel4);
                                                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                                }, 1000);


                                            }, 1000);

                                        }else {
                                            ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.drop_off_location);
                                            chatBotModelList.add(chatBotModel3);
                                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                                        }


                                    }, 1000);


                        }, 1000);
            } else {
                ChatBotModel chatBotModel = chatBotModelList.get(order_details_pos);
                chatBotModel.setText(details);
                chatBotModelList.set(order_details_pos, chatBotModel);
                adapter.notifyItemChanged(order_details_pos);

            }
        }catch (Exception e){}


    }
    public void openDropOffLocationMap(int adapterPosition)
    {
        this.drop_off_pos = adapterPosition;
        navigateToMapSearch(200);
    }
    public void addCoupon(String coupon, int adapterPosition)
    {
        this.coupon_pos = adapterPosition;
        if (coupon.equals(getString(R.string.don_t_have_coupon))) {
            addOrderTextModel.setCoupon_id("0");
            updateCouponAction(coupon);
        }
        else {
            Intent intent = new Intent(this, AddCouponActivity.class);
            startActivityForResult(intent,400);
        }
    }
    private void updateCouponAction(String coupon)
    {
        try {
            ChatBotModel chatBotModel2 = chatBotModelList.get(coupon_pos);
            chatBotModel2.setEnabled(false);
            chatBotModelList.set(coupon_pos, chatBotModel2);
            adapter.notifyItemChanged(coupon_pos);

            ChatBotModel chatBotModel = createInstance(ChatBotAdapter.coupon_details);
            chatBotModel.setText(coupon);
            chatBotModelList.add(chatBotModel);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

            new Handler().postDelayed(() -> {
                chatBotModelList.add(null);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                new Handler().postDelayed(() -> {
                    chatBotModelList.remove(chatBotModelList.size() - 1);
                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                    ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.payment);
                    chatBotModelList.add(chatBotModel3);
                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                }, 1000);
            }, 1000);
        }catch (Exception e){}

    }
    public void payment(int adapterPosition)
    {

        try {
            addOrderTextModel.setPayment("cash");
            ChatBotModel chatBotModel = chatBotModelList.get(adapterPosition);
            chatBotModel.setEnabled(false);
            chatBotModelList.set(adapterPosition, chatBotModel);
            adapter.notifyItemChanged(adapterPosition);

            ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.payment_details);
            chatBotModel2.setText(getString(R.string.cash));
            chatBotModelList.add(chatBotModel2);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


            chatBotModelList.add(null);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

            new Handler().postDelayed(() -> {
                chatBotModelList.remove(chatBotModelList.size() - 1);
                adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.finish_order);
                chatBotModelList.add(chatBotModel3);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

            }, 1000);
        }catch (Exception e){}


    }
    private void updateSelectedShopListUi()
    {
        try {
            ChatBotModel chatBotModel1 = chatBotModelList.get(shopListPos);
            chatBotModel1.setEnabled(false);
            chatBotModelList.set(shopListPos, chatBotModel1);
            adapter.notifyItemChanged(shopListPos);


            ChatBotModel chatBotModel = createInstance(ChatBotAdapter.store_details);
            chatBotModel.setText(getString(R.string.shop_list));
            chatBotModelList.add(chatBotModel);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);
        }catch (Exception e){}

    }
    public void shareLocation(int adapterPosition)
    {
        share_location_pos = adapterPosition;
        navigateToPackageMapActivity(300);

    }
    public void cancelOrder()
    {
        try {
            ChatBotModel chatBotModel = createInstance(ChatBotAdapter.new_order);
            chatBotModel.setText(getString(R.string.order_canceled));
            chatBotModelList.add(chatBotModel);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


            new Handler().postDelayed(() -> {
                chatBotModelList.add(null);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                new Handler().postDelayed(() -> {
                    chatBotModelList.remove(chatBotModelList.size() - 1);
                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                    ChatBotModel chatBotModel2;
                    chatBotModel2 = createInstance(ChatBotAdapter.new_order);


                    chatBotModelList.add(chatBotModel2);
                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);
                    startChat();
                }, 1000);
            }, 1000);
        }catch (Exception e){}


    }
    public void submitOrder() {

        try {
            ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
            dialog.setCancelable(false);
            dialog.show();
            Api.getService(Tags.base_url)
                    .sendTextOrder(userModel.getUser().getToken(),userModel.getUser().getId(),addOrderTextModel.getOrder_type(),addOrderTextModel.getMarket_id(),addOrderTextModel.getPlace_id(),"0",addOrderTextModel.getTo_address(),addOrderTextModel.getTo_lat(),addOrderTextModel.getTo_lng(),addOrderTextModel.getPlace_name(),addOrderTextModel.getPlace_address(),addOrderTextModel.getPlace_lat(),addOrderTextModel.getPlace_lng(),"1",addOrderTextModel.getCoupon_id(),addOrderTextModel.getOrder_text(),addOrderTextModel.getComments())
                    .enqueue(new Callback<SingleOrderDataModel>() {
                        @Override
                        public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                            dialog.dismiss();
                            if (response.isSuccessful()&&response.body()!=null)
                            {
                                Intent intent =new Intent(AddOrderActivity.this, ChatActivity.class);
                                intent.putExtra("order_id",response.body().getOrder().getId());
                                startActivity(intent);
                                finish();

                            }else
                            {
                                if (response.code()==500)
                                {
                                    Toast.makeText(AddOrderActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                }else if(response.code()==406){
                                    Common.CreateDialogAlertOrder(AddOrderActivity.this,getString(R.string.no_courier));

                                    //  Toast.makeText(AddOrderActivity.this, R.string.no_courier, Toast.LENGTH_SHORT).show();
                                } else
                                {
                                    Toast.makeText(AddOrderActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }

                                try {
                                    Log.e("error",response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                            try {
                                dialog.dismiss();
                                if (t.getMessage() != null) {
                                    Log.e("msg_category_error", t.getMessage() + "__");

                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        Toast.makeText(AddOrderActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddOrderActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }catch (Exception e)
                            {
                                Log.e("Error",e.getMessage()+"__");
                            }
                        }
                    });
        }catch (Exception e){}

    }

    private void navigateToPackageMapActivity(int req)
    {
        Intent intent = new Intent(this, PackageMapActivity.class);
        startActivityForResult(intent, req);
    }
    private void navigateToMapSearch(int req)
    {
        Intent intent = new Intent(this, MapSearchActivity.class);
        startActivityForResult(intent, req);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null)
        {
            try {
                NearbyModel.Result result = (NearbyModel.Result) data.getSerializableExtra("data");
                addOrderTextModel.setPlace_id(result.getPlace_id());
                if (result.getCustomPlaceModel()!=null){

                    addOrderTextModel.setOrder_type("emdad_market");
                    addOrderTextModel.setMarket_id(result.getCustomPlaceModel().getId());
                }else {
                    addOrderTextModel.setOrder_type("google_market");
                    addOrderTextModel.setMarket_id(0);

                }
                addOrderTextModel.setPlace_name(result.getName());
                addOrderTextModel.setPlace_address(result.getVicinity());
                addOrderTextModel.setPlace_lat(result.getGeometry().getLocation().getLat());
                addOrderTextModel.setPlace_lng(result.getGeometry().getLocation().getLng());


                updateSelectedShopUI(result);
            }catch (Exception e){}


        } else if (requestCode == 200 && resultCode == RESULT_OK && data != null) {

            try {
                FavoriteLocationModel favoriteLocationModel = (FavoriteLocationModel) data.getSerializableExtra("data");
                ChatBotModel chatBotModel1 = chatBotModelList.get(drop_off_pos);
                chatBotModel1.setEnabled(false);
                chatBotModelList.set(drop_off_pos, chatBotModel1);
                adapter.notifyItemChanged(drop_off_pos);

                ChatBotModel chatBotModel2;
                if (mapLocation){
                    addOrderTextModel.setTo_address(favoriteLocationModel.getAddress());
                    addOrderTextModel.setTo_lat(favoriteLocationModel.getLat());
                    addOrderTextModel.setTo_lng(favoriteLocationModel.getLng());

                    chatBotModel2 = createInstance(ChatBotAdapter.drop_location_details);
                    chatBotModel2.setTo_address(favoriteLocationModel.getAddress());
                    chatBotModel2.setTo_lat(favoriteLocationModel.getLat());
                    chatBotModel2.setTo_lng(favoriteLocationModel.getLng());
                }else {


                    addOrderTextModel.setTo_address(favoriteLocationModel.getAddress());
                    addOrderTextModel.setTo_lat(favoriteLocationModel.getLat());
                    addOrderTextModel.setTo_lng(favoriteLocationModel.getLng());


                    chatBotModel2 = createInstance(ChatBotAdapter.pick_up_location_details);
                    chatBotModel2.setFrom_address(favoriteLocationModel.getAddress());
                    chatBotModel2.setFrom_lat(favoriteLocationModel.getLat());
                    chatBotModel2.setFrom_lng(favoriteLocationModel.getLng());
                }


                chatBotModelList.add(chatBotModel2);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);


                chatBotModelList.add(null);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                new Handler()
                        .postDelayed(() -> {
                            chatBotModelList.remove(chatBotModelList.size() - 1);
                            adapter.notifyItemRemoved(chatBotModelList.size() - 1);


                            ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.use_coupon);
                            chatBotModelList.add(chatBotModel3);
                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                            new Handler().postDelayed(() -> {
                                chatBotModelList.add(null);
                                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                                new Handler().postDelayed(() -> {
                                    chatBotModelList.remove(chatBotModelList.size() - 1);
                                    adapter.notifyItemRemoved(chatBotModelList.size() - 1);


                                    ChatBotModel chatBotModel4 = createInstance(ChatBotAdapter.add_coupon);
                                    chatBotModelList.add(chatBotModel4);
                                    adapter.notifyItemInserted(chatBotModelList.size() - 1);
                                    binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                                }, 1000);


                            }, 1000);


                        }, 1000);

            }catch (Exception e){

            }


        } else if (requestCode == 300 && resultCode == RESULT_OK && data != null) {

            try {
                isPackageOrder = true;
                fromLocation = (FavoriteLocationModel) data.getSerializableExtra("data1");
                toLocation = (FavoriteLocationModel) data.getSerializableExtra("data2");


                addOrderTextModel.setPlace_id("0");
                addOrderTextModel.setMarket_id(0);
                addOrderTextModel.setPlace_name(fromLocation.getAddress());
                addOrderTextModel.setPlace_address(fromLocation.getAddress());
                addOrderTextModel.setPlace_lat(fromLocation.getLat());
                addOrderTextModel.setPlace_lng(fromLocation.getLng());

                addOrderTextModel.setTo_address(toLocation.getAddress());
                addOrderTextModel.setTo_lat(toLocation.getLat());
                addOrderTextModel.setTo_lng(toLocation.getLng());



                ChatBotModel chatBotModel1 = chatBotModelList.get(share_location_pos);
                chatBotModel1.setEnabled(false);
                chatBotModelList.set(share_location_pos, chatBotModel1);
                adapter.notifyItemChanged(share_location_pos);

                ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.share_location_details);
                chatBotModel2.setFrom_address(fromLocation.getAddress());
                chatBotModel2.setFrom_lat(fromLocation.getLat());
                chatBotModel2.setFrom_lng(fromLocation.getLng());
                chatBotModel2.setTo_address(toLocation.getAddress());
                chatBotModel2.setTo_lat(toLocation.getLat());
                chatBotModel2.setTo_lng(toLocation.getLng());
                chatBotModel2.setDistance(calculateDistance(new LatLng(fromLocation.getLat(), fromLocation.getLng()), new LatLng(toLocation.getLat(), toLocation.getLng())));


                chatBotModelList.add(chatBotModel2);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);


                chatBotModelList.add(null);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                new Handler()
                        .postDelayed(() -> {
                            chatBotModelList.remove(chatBotModelList.size() - 1);
                            adapter.notifyItemRemoved(chatBotModelList.size() - 1);

                            ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.needs);
                            chatBotModelList.add(chatBotModel3);
                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);



                        }, 1000);
            }catch (Exception e){}


        }else if (requestCode == 400 && resultCode == RESULT_OK && data != null) {
            CouponModel couponModel = (CouponModel) data.getSerializableExtra("data");

           addOrderTextModel.setCoupon_id(String.valueOf(couponModel.getId()));
           if (couponModel.getCoupon_type().equals("per")){
               String discount = getString(R.string.you_got)+" "+couponModel.getCoupon_value()+"% "+getString(R.string.discount)+" "+getString(R.string.on_delivery);
               updateCouponAction(discount);

           }else {
               String discount = getString(R.string.you_got)+" "+couponModel.getCoupon_value()+" "+getString(R.string.sar)+" "+getString(R.string.discount)+" "+getString(R.string.on_delivery);;
               updateCouponAction(discount);
           }

        }else if (requestCode==500 && resultCode==RESULT_OK && data!=null){
            try {
                mapLocation = true;

                FavoriteLocationModel favoriteLocationModel = (FavoriteLocationModel) data.getSerializableExtra("data");

                addOrderTextModel.setPlace_id("0");
                addOrderTextModel.setPlace_name(favoriteLocationModel.getAddress());
                addOrderTextModel.setOrder_type("google_market");
                addOrderTextModel.setPlace_address(favoriteLocationModel.getAddress());
                addOrderTextModel.setPlace_lat(favoriteLocationModel.getLat());
                addOrderTextModel.setPlace_lng(favoriteLocationModel.getLng());

                ChatBotModel chatBotModel1 = chatBotModelList.get(shopListPos);
                chatBotModel1.setEnabled(false);
                chatBotModelList.set(shopListPos, chatBotModel1);
                adapter.notifyItemChanged(shopListPos);

                ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.pick_up_location_details);
                chatBotModel2.setFrom_address(favoriteLocationModel.getAddress());
                chatBotModel2.setTo_lat(favoriteLocationModel.getLat());
                chatBotModel2.setTo_lng(favoriteLocationModel.getLng());
                chatBotModelList.add(chatBotModel2);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);


                chatBotModelList.add(null);
                adapter.notifyItemInserted(chatBotModelList.size() - 1);
                binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                new Handler()
                        .postDelayed(() -> {
                            chatBotModelList.remove(chatBotModelList.size() - 1);
                            adapter.notifyItemRemoved(chatBotModelList.size() - 1);


                            ChatBotModel chatBotModel3 = createInstance(ChatBotAdapter.needs);

                            chatBotModelList.add(chatBotModel3);
                            adapter.notifyItemInserted(chatBotModelList.size() - 1);
                            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);



                        }, 1000);
            }catch (Exception e){}


        }
    }
    private void updateSelectedShopUI(NearbyModel.Result result)
    {

        try {
            updateSelectedShopListUi();
            ChatBotModel chatBotModel = createInstance(ChatBotAdapter.place_details);
            chatBotModel.setText(result.getName());
            chatBotModel.setFrom_lat(result.getGeometry().getLocation().getLat());
            chatBotModel.setFrom_lng(result.getGeometry().getLocation().getLng());
            chatBotModel.setDistance(result.getDistance());
            chatBotModel.setFrom_address(result.getVicinity());
            chatBotModel.setRate(result.getRating());

            if (result.getPhotos() != null && result.getPhotos().size() > 0) {
                chatBotModel.setImage_url(result.getPhotos().get(0).getPhoto_reference());
            } else {
                chatBotModel.setImage_url(result.getIcon());

            }
            chatBotModelList.add(chatBotModel);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

            chatBotModelList.add(null);
            adapter.notifyItemInserted(chatBotModelList.size() - 1);
            binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

            new Handler()
                    .postDelayed(() -> {
                        chatBotModelList.remove(chatBotModelList.size() - 1);
                        adapter.notifyItemRemoved(chatBotModelList.size() - 1);


                        ChatBotModel chatBotModel2 = createInstance(ChatBotAdapter.needs);

                        chatBotModelList.add(chatBotModel2);
                        adapter.notifyItemInserted(chatBotModelList.size() - 1);
                        binding.recView.smoothScrollToPosition(chatBotModelList.size() - 1);

                    }, 1000);
        }catch (Exception e){

        }



    }
    public void writeOrderDetails(int adapterPosition)
    {
        write_order_details_pos = adapterPosition;
        openSheet();
    }
    public void changeOrderDetails(int adapterPosition)
    {
        order_details_pos = adapterPosition;
        openSheet();

    }
    private void openSheet()
    {
        binding.root.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.root.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.root.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private void closeSheet()
    {
        binding.root.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.root.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.root.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private double calculateDistance(LatLng latLng1, LatLng latLng2)
    {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000;
    }

    @Override
    public void onBackPressed() {
        if (binding.root.getVisibility() == View.VISIBLE) {
            closeSheet();
        } else {
            super.onBackPressed();

        }

    }



}
