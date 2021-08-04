package com.endpoint.giveme.activities_fragments.activity_home.fragments.fragment_driver_order;

import android.app.Activity;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_home.HomeActivity;
import com.endpoint.giveme.adapters.DriverDeliveryOrdersAdapter;
import com.endpoint.giveme.databinding.FragmentDriverMyDeliverOrdersBinding;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.models.OrdersDataModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Driver_Deliver_Order extends Fragment {
    private FragmentDriverMyDeliverOrdersBinding binding;
    private HomeActivity activity;
    private DriverDeliveryOrdersAdapter adapter;
    private List<OrderModel> orderModelList;
    private int current_page = 1;
    private boolean isLoading = false;
    private Preferences preferences;
    private UserModel userModel;
    private Call<OrdersDataModel> loadMoreCall;


    public static Fragment_Driver_Deliver_Order newInstance() {
        return new Fragment_Driver_Deliver_Order();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_my_deliver_orders, container, false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        activity = (HomeActivity) getActivity();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(activity);
        orderModelList = new ArrayList<>();
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary));
        binding.recView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new DriverDeliveryOrdersAdapter(orderModelList, activity, this);
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

        binding.swipeRefresh.setOnRefreshListener(this::getOrders);
        getOrders();
    }

    public void getOrders() {
        if (loadMoreCall!=null){
            loadMoreCall.cancel();
            if (orderModelList.size()>0&&orderModelList.get(orderModelList.size()-1)==null){
                orderModelList.remove(orderModelList.size() - 1);
                adapter.notifyItemRemoved(orderModelList.size() - 1);
            }
        }

        Api.getService(Tags.base_url).getDriverDeliveryOrder(userModel.getUser().getToken(), userModel.getUser().getId(), 1, "on", 10)
                .enqueue(new Callback<OrdersDataModel>() {
                    @Override
                    public void onResponse(Call<OrdersDataModel> call, Response<OrdersDataModel> response) {
                        binding.prgBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                if (response.body().getData().size() > 0) {
                                    binding.llNoOrder.setVisibility(View.GONE);
                                    try {
                                        updateDataDistance(response.body().getData(), false);
                                        current_page = response.body().getCurrent_page();
                                    }catch (Exception e){

                                    }

                                } else {
                                    orderModelList.clear();
                                    adapter.notifyDataSetChanged();
                                    binding.llNoOrder.setVisibility(View.VISIBLE);

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

        loadMoreCall = Api.getService(Tags.base_url).getDriverDeliveryOrder(userModel.getUser().getToken(), userModel.getUser().getId(), page, "on", 10);
        loadMoreCall.enqueue(new Callback<OrdersDataModel>() {
            @Override
            public void onResponse(Call<OrdersDataModel> call, Response<OrdersDataModel> response) {
                isLoading = false;
                if (orderModelList.get(orderModelList.size() - 1) == null) {
                    orderModelList.remove(orderModelList.size() - 1);
                    adapter.notifyItemRemoved(orderModelList.size() - 1);
                }
                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        if (response.body().getData().size() > 0) {
                            current_page = response.body().getCurrent_page();
                            updateDataDistance(response.body().getData(), true);
                        }


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

    private void updateDataDistance(List<OrderModel> data, boolean isLoadMore) {
        LatLng user_location = new LatLng(activity.user_lat, activity.user_lng);
        for (int index = 0; index < data.size(); index++) {
            OrderModel orderModel = data.get(index);
            orderModel.setPick_up_distance(calculateDistance(user_location, new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude()))));
            orderModel.setDrop_off_distance(calculateDistance(user_location, new LatLng(Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude()))));
            data.set(index, orderModel);
        }

        if (!isLoadMore) {
            orderModelList.clear();
            orderModelList.addAll(data);
            adapter.notifyDataSetChanged();
        } else {
            int old_pos = orderModelList.size() - 1;
            orderModelList.addAll(data);
            int new_pos = orderModelList.size();
            adapter.notifyItemRangeInserted(old_pos, new_pos);
        }
    }

    private String calculateDistance(LatLng latLng1, LatLng latLng2) {
        return String.format(Locale.ENGLISH, "%s %s", String.format(Locale.ENGLISH, "%.2f", (SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000)), getString(R.string.km));

    }

    public void setItemData(OrderModel orderModel1) {
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra("order_id", orderModel1.getId());
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Log.e("rr","rr");
            getOrders();
        }
    }
}
