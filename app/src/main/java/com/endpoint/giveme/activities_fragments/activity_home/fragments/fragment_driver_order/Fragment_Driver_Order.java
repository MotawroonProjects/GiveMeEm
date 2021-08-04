package com.endpoint.giveme.activities_fragments.activity_home.fragments.fragment_driver_order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_home.HomeActivity;
import com.endpoint.giveme.activities_fragments.activity_old_orders.OldOrdersActivity;
import com.endpoint.giveme.adapters.MyPagerAdapter;
import com.endpoint.giveme.databinding.FragmentDriverOrdersBinding;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Driver_Order extends Fragment {
    private FragmentDriverOrdersBinding binding;
    private HomeActivity activity;
    private Preferences preferences;
    private UserModel userModel;
    private List<Fragment> fragmentList;
    private List<String> titles;
    private MyPagerAdapter adapter;



    public static Fragment_Driver_Order newInstance(){
        return new Fragment_Driver_Order();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_orders,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        fragmentList = new ArrayList<>();
        titles = new ArrayList<>();
        activity = (HomeActivity) getActivity();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(activity);
        fragmentList.add(Fragment_Driver_My_Order.newInstance());
        fragmentList.add(Fragment_Driver_Deliver_Order.newInstance());
        titles.add(getString(R.string.order));
        titles.add(getString(R.string.delivery_order));
        adapter = new MyPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,fragmentList,titles);
        binding.tab.setupWithViewPager(binding.pager);
        binding.pager.setAdapter(adapter);

        binding.flOldOrders.setOnClickListener(v -> {
            Intent intent = new Intent(activity, OldOrdersActivity.class);
            startActivityForResult(intent,100);
        });
    }


    public void updateData(){
        Fragment_Driver_My_Order fragment_driver_my_order = (Fragment_Driver_My_Order) fragmentList.get(0);
        fragment_driver_my_order.getOrders();
        Fragment_Driver_Deliver_Order fragment_driver_deliver_order = (Fragment_Driver_Deliver_Order) fragmentList.get(1);
        fragment_driver_deliver_order.getOrders();


    }

}
