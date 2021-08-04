package com.endpoint.giveme.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_shop_custom_query.ShopsCustomQueryActivity;
import com.endpoint.giveme.databinding.CustomShopRowBinding;
import com.endpoint.giveme.databinding.LoadMoreRowBinding;
import com.endpoint.giveme.models.CustomPlaceModel;

import java.util.List;

public class CustomShopsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<CustomPlaceModel> placeModelList;
    private Context context;
    private LayoutInflater inflater;
    private ShopsCustomQueryActivity activity;
    private String currency ="";

    public CustomShopsAdapter(List<CustomPlaceModel> placeModelList, Context context, String currency) {
        this.placeModelList = placeModelList;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (ShopsCustomQueryActivity) context;
        this.currency = currency;


    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            CustomShopRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.custom_shop_row, parent, false);
            return new MyHolder(binding);
        }else {
            LoadMoreRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.load_more_row, parent, false);
            return new LoadMoreHolder(binding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            CustomPlaceModel placeModel = placeModelList.get(position);
            myHolder.binding.setModel(placeModel);

            holder.itemView.setOnClickListener(v -> {
                CustomPlaceModel customPlaceModel = placeModelList.get(myHolder.getAdapterPosition());
                activity.setShopData(customPlaceModel);

            });
        }else if (holder instanceof LoadMoreHolder){
            LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
            loadMoreHolder.binding.prgBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            loadMoreHolder.binding.prgBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return placeModelList.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private CustomShopRowBinding binding;

        public MyHolder(CustomShopRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
        private LoadMoreRowBinding binding;

        public LoadMoreHolder(LoadMoreRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    @Override
    public int getItemViewType(int position) {
        if (placeModelList.get(position)==null){
            return LOAD;
        }else {
            return DATA;
        }
    }
}
