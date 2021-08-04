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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_home.HomeActivity;
import com.endpoint.giveme.activities_fragments.activity_old_orders.OldOrdersActivity;
import com.endpoint.giveme.adapters.OrdersAdapter;
import com.endpoint.giveme.adapters.RateReasonAdapter;
import com.endpoint.giveme.databinding.FragmentOrdersBinding;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.models.OrdersDataModel;
import com.endpoint.giveme.models.RateModel;
import com.endpoint.giveme.models.RateReason;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Order extends Fragment {
    private FragmentOrdersBinding binding;
    private HomeActivity activity;
    private OrdersAdapter adapter;
    private List<OrderModel> orderModelList;
    private int current_page = 1;
    private boolean isLoading = false;
    private Preferences preferences;
    private UserModel userModel;
    private Call<OrdersDataModel> loadMoreCall;
    private RateReasonAdapter rateReasonAdapter;
    private RateModel rateModel;
    private OrderModel orderModel;
    private int pos = -1;


    public static Fragment_Order newInstance() {
        return new Fragment_Order();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_orders, container, false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        rateModel = new RateModel();
        activity = (HomeActivity) getActivity();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(activity);
        orderModelList = new ArrayList<>();
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary));
        binding.recView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new OrdersAdapter(orderModelList, activity, this);
        binding.recView.setAdapter(adapter);
        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    LinearLayoutManager manager = (LinearLayoutManager) binding.recView.getLayoutManager();
                    int last_item_pos = manager.findLastCompletelyVisibleItemPosition();
                    int total_items_count = binding.recView.getAdapter().getItemCount();
                    if (last_item_pos == (total_items_count - 2) && !isLoading) {
                        int page = current_page + 1;
                        loadMore(page);
                    }
                }
            }
        });

        binding.flOldOrders.setOnClickListener(v -> {
            binding.flRate.setVisibility(View.GONE);
            Intent intent = new Intent(activity, OldOrdersActivity.class);
            startActivityForResult(intent, 100);
        });

        binding.recViewRateReason.setLayoutManager(new GridLayoutManager(activity,2));
        rateReasonAdapter = new RateReasonAdapter(new ArrayList<>(),activity,this);
        binding.recViewRateReason.setAdapter(rateReasonAdapter);
        binding.swipeRefresh.setOnRefreshListener(this::getOrders);
        binding.btnRate.setOnClickListener(v -> {
            String comment = binding.edtRateComment.getText().toString();
            rateModel.setComment(comment);
            closeRateActionSheet();
            binding.flRate.setVisibility(View.GONE);
            rate();
        });

        binding.tvNotNow.setOnClickListener(v -> closeRateActionSheet());
        binding.swipeRefresh.setOnRefreshListener(this::getOrders);
        binding.btnBack.setOnClickListener(v -> activity.displayFragmentMain());
        binding.imageCloseSheet.setOnClickListener(v -> closeRateActionSheet());
        getOrders();
    }

    public void getOrders() {
        if (loadMoreCall != null) {
            loadMoreCall.cancel();
            if (orderModelList.size() > 0 && orderModelList.get(orderModelList.size() - 1) == null) {
                orderModelList.remove(orderModelList.size() - 1);
                adapter.notifyItemRemoved(orderModelList.size() - 1);
            }
        }
        Api.getService(Tags.base_url).getClientOrder(userModel.getUser().getToken(), userModel.getUser().getId(), "current", 1, "on", 20)
                .enqueue(new Callback<OrdersDataModel>() {
                    @Override
                    public void onResponse(Call<OrdersDataModel> call, Response<OrdersDataModel> response) {
                        binding.prgBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                orderModelList.clear();

                                if (response.body().getData().size() > 0) {
                                    binding.llNoOrder.setVisibility(View.GONE);
                                    orderModelList.addAll(response.body().getData());
                                    current_page = response.body().getCurrent_page();
                                } else {
                                    binding.llNoOrder.setVisibility(View.VISIBLE);

                                }

                                adapter.notifyDataSetChanged();



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
                    public void onFailure(Call<OrdersDataModel> call, Throwable t) {
                        binding.prgBar.setVisibility(View.GONE);
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

    private void loadMore(int page) {
        orderModelList.add(null);
        adapter.notifyItemInserted(orderModelList.size() - 1);
        isLoading = true;

        loadMoreCall = Api.getService(Tags.base_url).getClientOrder(userModel.getUser().getToken(), userModel.getUser().getId(), "current", page, "on", 20);
        loadMoreCall.enqueue(new Callback<OrdersDataModel>() {
            @Override
            public void onResponse(Call<OrdersDataModel> call, Response<OrdersDataModel> response) {
                isLoading = false;
                if (orderModelList.get(orderModelList.size() - 1) == null) {
                    orderModelList.remove(orderModelList.size() - 1);
                    adapter.notifyItemRemoved(orderModelList.size() - 1);
                }
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getData().size() > 0) {
                        current_page = response.body().getCurrent_page();
                        int old_pos = orderModelList.size() - 1;
                        orderModelList.addAll(response.body().getData());
                        int new_pos = orderModelList.size();
                        adapter.notifyItemRangeInserted(old_pos, new_pos);

                    }

                } else {
                    isLoading = false;
                    if (orderModelList.get(orderModelList.size() - 1) == null) {
                        orderModelList.remove(orderModelList.size() - 1);
                        adapter.notifyItemRemoved(orderModelList.size() - 1);
                    }
                    try {
                        Log.e("error_code", response.code() + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<OrdersDataModel> call, Throwable t) {
                isLoading = false;
                if (orderModelList.get(orderModelList.size() - 1) == null) {
                    orderModelList.remove(orderModelList.size() - 1);
                    adapter.notifyItemRemoved(orderModelList.size() - 1);
                }
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

    public void setItemData(OrderModel orderModel1) {
        binding.flRate.setVisibility(View.GONE);
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra("order_id", orderModel1.getId());
        startActivityForResult(intent, 100);
    }

    private void rate1UI(){
        if (orderModel!=null&&orderModel.getDriver()!=null){
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL+orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).into(binding.driverImage);

        }
        binding.emoji1.setImageResource(R.drawable.sad2);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(activity,R.color.black));
        binding.tv2.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(1,getString(R.string.flirt),false));
        rateReasonList.add(new RateReason(2,getString(R.string.rude),false));
        rateReasonList.add(new RateReason(3,getString(R.string.smoked),false));
        rateReasonList.add(new RateReason(4,getString(R.string.late),false));
        rateReasonList.add(new RateReason(5,getString(R.string.not_adhering),false));
        rateReasonAdapter.addData(rateReasonList);
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(1);


    }
    private void rate2UI(){
        if (orderModel!=null&&orderModel.getDriver()!=null){
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL+orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).into(binding.driverImage);

        }
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad2);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(activity,R.color.black));
        binding.tv3.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(1,getString(R.string.flirt),false));
        rateReasonList.add(new RateReason(2,getString(R.string.rude),false));
        rateReasonList.add(new RateReason(3,getString(R.string.smoked),false));
        rateReasonList.add(new RateReason(4,getString(R.string.late),false));
        rateReasonList.add(new RateReason(5,getString(R.string.not_adhering),false));
        rateReasonAdapter.addData(rateReasonList);
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(2);

    }
    private void rate3UI(){
        if (orderModel!=null&&orderModel.getDriver()!=null){
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL+orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).into(binding.driverImage);

        }
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad4);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(activity,R.color.black));
        binding.tv4.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(1,getString(R.string.flirt),false));
        rateReasonList.add(new RateReason(2,getString(R.string.rude),false));
        rateReasonList.add(new RateReason(3,getString(R.string.smoked),false));
        rateReasonList.add(new RateReason(4,getString(R.string.late),false));
        rateReasonList.add(new RateReason(5,getString(R.string.not_adhering),false));
        rateReasonAdapter.addData(rateReasonList);
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(3);


    }
    private void rate4UI(){
        if (orderModel!=null&&orderModel.getDriver()!=null){
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL+orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).into(binding.driverImage);

        }
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile2);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(activity,R.color.black));
        binding.tv5.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(6,getString(R.string.get_angry),false));
        rateReasonList.add(new RateReason(4,getString(R.string.late),false));
        rateReasonAdapter.addData(rateReasonList);
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(4);


    }
    private void rate5UI(){
        if (orderModel!=null&&orderModel.getDriver()!=null){
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL+orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).into(binding.driverImage);

        }
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile4);
        binding.tv1.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(activity,R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(activity,R.color.black));
        List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(7,getString(R.string.fast_service),false));
        rateReasonList.add(new RateReason(8,getString(R.string.respectable),false));
        rateReasonList.add(new RateReason(9,getString(R.string.professional),false));
        rateReasonList.add(new RateReason(10,getString(R.string.responsive),false));
        rateReasonAdapter.addData(rateReasonList);
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(5);


    }


    public void updateRateUi(OrderModel orderModel, int adapterPosition, int emoji) {
        openRateSheet();
        this.pos =adapterPosition;
        this.orderModel = orderModel;
        switch (emoji){
            case 1:
                rate1UI();
                break;
            case 2:
                rate2UI();
                break;
            case 3:
                rate3UI();
                break;
            case 4:
                rate4UI();
                break;
            case 5:
                rate5UI();
                break;
        }

    }

    public void setRateItem(RateReason reason) {
        rateModel.setReason(reason.getId());

    }

    private void rate() {

        ProgressDialog dialog = Common.createProgressDialog(activity, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientRate(userModel.getUser().getToken(), orderModel.getDriver().getId(), orderModel.getClient().getId(), orderModel.getId(), rateModel.getRate(),rateModel.getReason(),rateModel.getComment())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (pos!=-1){
                                    orderModelList.remove(pos);
                                    adapter.notifyItemRemoved(pos);
                                    if (orderModelList.size()>0){
                                        binding.llNoOrder.setVisibility(View.GONE);
                                    }else {
                                        binding.llNoOrder.setVisibility(View.VISIBLE);

                                    }
                                    pos = -1;
                                    orderModel = null;

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
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
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

    private void openRateSheet() {

        binding.flRate.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.slide_up);
        binding.flRate.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.flRate.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void closeRateActionSheet() {

        binding.flRate.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.slide_down);
        binding.flRate.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flRate.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            getOrders();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeRateActionSheet();
    }
}
