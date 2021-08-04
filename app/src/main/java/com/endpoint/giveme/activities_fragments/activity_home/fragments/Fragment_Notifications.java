package com.endpoint.giveme.activities_fragments.activity_home.fragments;

import android.app.ProgressDialog;
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
import com.endpoint.giveme.adapters.NotificationAdapter;
import com.endpoint.giveme.databinding.FragmentNotificationBinding;
import com.endpoint.giveme.models.NotificationDataModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Notifications extends Fragment {
    private HomeActivity activity;
    private FragmentNotificationBinding binding;
    private UserModel userModel;
    private Preferences preferences;
    private List<NotificationDataModel.NotificationModel> notificationModelList;
    private NotificationAdapter adapter;
    private int current_page = 1;
    private boolean isLoading = false;
    private Call<NotificationDataModel> loadMoreCall;

    public static Fragment_Notifications newInstance(){
        return new Fragment_Notifications();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        notificationModelList = new ArrayList<>();
        activity = (HomeActivity) getActivity();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(activity);
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary));
        binding.recView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new NotificationAdapter(notificationModelList, activity, this);
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

        binding.tvClearAll.setOnClickListener(v -> {
            deleteAllNotification();
        });
        binding.swipeRefresh.setOnRefreshListener(this::getNotifications);
        getNotifications();
    }

    public void updateUserData(UserModel userModel){
        this.userModel = userModel;
    }
    public void getNotifications() {
        updateNotificationCount();
        Api.getService(Tags.base_url).getNotification(userModel.getUser().getToken(), userModel.getUser().getId(), 1, "on", 20)
                .enqueue(new Callback<NotificationDataModel>() {
                    @Override
                    public void onResponse(Call<NotificationDataModel> call, Response<NotificationDataModel> response) {
                        binding.progBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                notificationModelList.clear();

                                if (response.body().getData().size() > 0) {
                                    binding.llNoData.setVisibility(View.GONE);
                                    notificationModelList.addAll(response.body().getData());
                                    current_page = response.body().getCurrent_page();
                                    binding.tvClearAll.setVisibility(View.VISIBLE);
                                } else {
                                    binding.llNoData.setVisibility(View.VISIBLE);
                                    binding.tvClearAll.setVisibility(View.GONE);

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
                    public void onFailure(Call<NotificationDataModel> call, Throwable t) {
                        binding.progBar.setVisibility(View.GONE);
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
        notificationModelList.add(null);
        adapter.notifyItemInserted(notificationModelList.size() - 1);
        isLoading = true;

        loadMoreCall = Api.getService(Tags.base_url).getNotification(userModel.getUser().getToken(), userModel.getUser().getId(), page, "on", 20);
        loadMoreCall.enqueue(new Callback<NotificationDataModel>() {
            @Override
            public void onResponse(Call<NotificationDataModel> call, Response<NotificationDataModel> response) {
                isLoading = false;
                if (notificationModelList.get(notificationModelList.size() - 1) == null) {
                    notificationModelList.remove(notificationModelList.size() - 1);
                    adapter.notifyItemRemoved(notificationModelList.size() - 1);
                }
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getData().size() > 0) {
                        current_page = response.body().getCurrent_page();
                        int old_pos = notificationModelList.size() - 1;
                        notificationModelList.addAll(response.body().getData());
                        int new_pos = notificationModelList.size();
                        adapter.notifyItemRangeInserted(old_pos, new_pos);

                    }
                } else {
                    isLoading = false;
                    if (notificationModelList.get(notificationModelList.size() - 1) == null) {
                        notificationModelList.remove(notificationModelList.size() - 1);
                        adapter.notifyItemRemoved(notificationModelList.size() - 1);
                    }
                    try {
                        Log.e("error_code", response.code() + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<NotificationDataModel> call, Throwable t) {
                isLoading = false;
                if (notificationModelList.get(notificationModelList.size() - 1) == null) {
                    notificationModelList.remove(notificationModelList.size() - 1);
                    adapter.notifyItemRemoved(notificationModelList.size() - 1);
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

    private void updateNotificationCount()
    {
        Api.getService(Tags.base_url).readNotification(userModel.getUser().getToken(),userModel.getUser().getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.isSuccessful()) {
                            activity.updateNotificationCount(0);
                        } else {

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

    public void setItemData(NotificationDataModel.NotificationModel model) {
        if (!model.getAction().equals("admin")){
            Intent intent =new Intent(activity, ChatActivity.class);
            intent.putExtra("order_id",Integer.parseInt(model.getOrder_id()));
            startActivity(intent);
        }


        /*if (model.getAction().equals("resend_offer")){

        }else {
            Intent intent =new Intent(activity, ChatActivity.class);
            intent.putExtra("order_id",Integer.parseInt(model.getOrder_id()));
            startActivity(intent);
        }*/
    }

    public void deleteNotification(NotificationDataModel.NotificationModel model, int adapterPosition) {
        ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url).deleteNotification(userModel.getUser().getToken(),model.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                notificationModelList.remove(adapterPosition);
                                adapter.notifyItemRemoved(adapterPosition);
                                if (notificationModelList.size()>0){
                                    binding.llNoData.setVisibility(View.GONE);
                                }else {
                                    binding.llNoData.setVisibility(View.VISIBLE);

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

    public void deleteAllNotification() {
        ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url).deleteAllNotification(userModel.getUser().getToken(),userModel.getUser().getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                notificationModelList.clear();
                                adapter.notifyDataSetChanged();
                                binding.llNoData.setVisibility(View.VISIBLE);
                                binding.tvClearAll.setVisibility(View.GONE);


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

}
