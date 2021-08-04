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
import com.endpoint.giveme.activities_fragments.activity_delegate_orders.DelegateOrdersActivity;
import com.endpoint.giveme.databinding.DriverDeliveryRowBinding;
import com.endpoint.giveme.databinding.LoadMoreRowBinding;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.share.Time_Ago;

import java.util.List;

public class DriverComingOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<OrderModel> list;
    private Context context;
    private LayoutInflater inflater;
    private DelegateOrdersActivity activity;

    public DriverComingOrdersAdapter(List<OrderModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (DelegateOrdersActivity) context;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            DriverDeliveryRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.driver_delivery_row, parent, false);
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
            OrderModel orderModel = list.get(position);
            myHolder.binding.setModel(orderModel);
            myHolder.binding.tvSince.setText(Time_Ago.getTimeAgo(Long.parseLong(orderModel.getOrder_date())*1000,context));


            myHolder.itemView.setOnClickListener(v -> {
                OrderModel orderModel1 = list.get(holder.getAdapterPosition());
                activity.setItemData(orderModel1);

            });

        }else if (holder instanceof LoadMoreHolder){
            LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
            loadMoreHolder.binding.prgBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            loadMoreHolder.binding.prgBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private DriverDeliveryRowBinding binding;

        public MyHolder(DriverDeliveryRowBinding binding) {
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
        if (list.get(position)==null){
            return LOAD;
        }else {
            return DATA;
        }
    }
}
