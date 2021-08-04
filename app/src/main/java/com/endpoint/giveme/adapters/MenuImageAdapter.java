package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_custom_shop_details.CustomShopDetailsActivity;
import com.endpoint.giveme.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.endpoint.giveme.databinding.MenuImageRowBinding;
import com.endpoint.giveme.models.CustomPlaceModel;

import java.util.List;

public class MenuImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CustomPlaceModel.MenuImage> list;
    private Context context;
    private LayoutInflater inflater;
    private AppCompatActivity activity;

    public MenuImageAdapter(List<CustomPlaceModel.MenuImage> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (AppCompatActivity) context;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        MenuImageRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.menu_image_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));
        myHolder.itemView.setOnClickListener(v -> {
            if (activity instanceof ShopDetailsActivity){
                ShopDetailsActivity shopDetailsActivity = (ShopDetailsActivity) activity;
                CustomPlaceModel.MenuImage menuImage = list.get(myHolder.getAdapterPosition());
                shopDetailsActivity.setMenuItem(menuImage,myHolder.binding.image);
            }else {
                CustomShopDetailsActivity customShopDetailsActivity = (CustomShopDetailsActivity) activity;
                CustomPlaceModel.MenuImage menuImage = list.get(myHolder.getAdapterPosition());
                customShopDetailsActivity.setMenuItem(menuImage,myHolder.binding.image);
            }

        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public MenuImageRowBinding binding;

        public MyHolder(@NonNull MenuImageRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
