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
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Main;
import com.endpoint.giveme.databinding.LoadMoreRow2Binding;
import com.endpoint.giveme.databinding.ShopRowBinding;
import com.endpoint.giveme.models.NearbyModel;

import java.util.List;

public class NearbyAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<NearbyModel.Result> placeModelList;
    private Context context;
    private LayoutInflater inflater;
    private Fragment_Main fragment_main;
    private String currency="";
    public NearbyAdapter2(List<NearbyModel.Result> placeModelList, Context context, Fragment_Main fragment_main, String currency) {
        this.placeModelList = placeModelList;
        this.context = context;
        this.fragment_main = fragment_main;
        inflater = LayoutInflater.from(context);
        this.currency = currency;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            ShopRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.shop_row, parent, false);
            return new MyHolder(binding);
        }else {
            LoadMoreRow2Binding binding = DataBindingUtil.inflate(inflater, R.layout.load_more_row2, parent, false);
            return new LoadMoreHolder(binding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            NearbyModel.Result placeModel = placeModelList.get(position);
            myHolder.binding.setModel(placeModel);
            myHolder.binding.setCurrency(currency);

            holder.itemView.setOnClickListener(v -> {
                NearbyModel.Result placeModel1 = placeModelList.get(myHolder.getAdapterPosition());
                fragment_main.placeItemData(placeModel1);
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
        private ShopRowBinding binding;

        public MyHolder(ShopRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
        private LoadMoreRow2Binding binding;

        public LoadMoreHolder(LoadMoreRow2Binding binding) {
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
