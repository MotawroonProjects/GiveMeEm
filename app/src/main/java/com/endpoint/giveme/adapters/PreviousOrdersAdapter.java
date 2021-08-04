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
import com.endpoint.giveme.activities_fragments.activity_old_orders.OldOrdersActivity;
import com.endpoint.giveme.databinding.LoadMoreRowBinding;
import com.endpoint.giveme.databinding.PreviousOrderRowBinding;
import com.endpoint.giveme.models.OrderModel;

import java.util.List;

public class PreviousOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<OrderModel> list;
    private Context context;
    private LayoutInflater inflater;
    private OldOrdersActivity activity;

    public PreviousOrdersAdapter(List<OrderModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (OldOrdersActivity) context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            PreviousOrderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.previous_order_row, parent, false);
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

            if (orderModel.getOrder_status().equals("client_end_and_rate")||orderModel.getOrder_status().equals("driver_end_rate")){
                myHolder.binding.icon.setImageResource(R.drawable.ic_checked);
                myHolder.binding.icon.setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary));
                myHolder.binding.tvState.setText(context.getString(R.string.done));
            }if (orderModel.getOrder_status().equals("client_cancel")){
                myHolder.binding.icon.setImageResource(R.drawable.ic_error);
                myHolder.binding.icon.setColorFilter(ContextCompat.getColor(context,R.color.color_red));
                myHolder.binding.tvState.setText(context.getString(R.string.cancel));
            }

            myHolder.binding.flResend.setOnClickListener(v -> {
                OrderModel orderModel1 = list.get(holder.getAdapterPosition());
                activity.resendOrder(orderModel1);
            });

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
        private PreviousOrderRowBinding binding;

        public MyHolder(PreviousOrderRowBinding binding) {
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
