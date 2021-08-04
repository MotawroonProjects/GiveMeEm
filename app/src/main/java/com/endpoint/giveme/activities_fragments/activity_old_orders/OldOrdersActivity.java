package com.endpoint.giveme.activities_fragments.activity_old_orders;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_resend_order.ResendOrderTextActivity;
import com.endpoint.giveme.adapters.PreviousOrdersAdapter;
import com.endpoint.giveme.databinding.ActivityOldOrdersBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.NotFireModel;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.models.OrdersDataModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OldOrdersActivity extends AppCompatActivity {
    private ActivityOldOrdersBinding binding;
    private String lang;
    private PreviousOrdersAdapter adapter;
    private List<OrderModel> orderModelList;
    private int current_page = 1;
    private boolean isLoading = false;
    private Preferences preferences;
    private UserModel userModel;
    private boolean isDataChanged = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_old_orders);
        initView();
    }

    private void initView() {
        orderModelList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);

        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.setLang(lang);

        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreviousOrdersAdapter(orderModelList, this);
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
        getOrders();

        binding.flBack.setOnClickListener(v -> onBackPressed());

        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

    }

    private void getOrders() {

        Api.getService(Tags.base_url).getClientOrder(userModel.getUser().getToken(), userModel.getUser().getId(), "old", 1, "on", 20)
                .enqueue(new Callback<OrdersDataModel>() {
                    @Override
                    public void onResponse(Call<OrdersDataModel> call, Response<OrdersDataModel> response) {
                        binding.prgBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().getData().size() > 0) {
                                binding.llNoOrder.setVisibility(View.GONE);
                                orderModelList.clear();
                                orderModelList.addAll(response.body().getData());
                                adapter.notifyDataSetChanged();
                                current_page = response.body().getCurrent_page();

                            } else {
                                binding.llNoOrder.setVisibility(View.VISIBLE);
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
                                    Toast.makeText(OldOrdersActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(OldOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
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

        Api.getService(Tags.base_url).getClientOrder(userModel.getUser().getToken(), userModel.getUser().getId(), "old", page, "on", 20)
                .enqueue(new Callback<OrdersDataModel>() {
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
                                    Toast.makeText(OldOrdersActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(OldOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    public void resendOrder(OrderModel orderModel) {
        isDataChanged = true;
        Intent intent = new Intent(this, ResendOrderTextActivity.class);
        intent.putExtra("data",orderModel);
        startActivity(intent);
    }

    public void setItemData(OrderModel orderModel) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("order_id", orderModel.getId());
        startActivityForResult(intent, 100);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            getOrders();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderUpdated(NotFireModel notFireModel){
        getOrders();
    }
    @Override
    public void onBackPressed() {
        if (isDataChanged) {
            setResult(RESULT_OK);
        }
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}