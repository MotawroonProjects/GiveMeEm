package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Order;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.fragment_driver_order.Fragment_Driver_My_Order;
import com.endpoint.giveme.databinding.RateReasonRowBinding;
import com.endpoint.giveme.models.RateReason;

import java.util.List;

public class RateReasonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RateReason> list;
    private Context context;
    private LayoutInflater inflater;
    private AppCompatActivity appCompatActivity;
    private int selectedPos = -1;
    private Fragment fragment;


    public RateReasonAdapter(List<RateReason> list, Context context,Fragment fragment) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        appCompatActivity = (AppCompatActivity) context;
        this.fragment = fragment;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        RateReasonRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.rate_reason_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        RateReason rateReason = list.get(position);
        myHolder.binding.setModel(rateReason);
        if (rateReason.isSelected()){
            myHolder.binding.fl.setBackgroundResource(R.drawable.small_rounded_primary);
            myHolder.binding.tvTitle.setTextColor(ContextCompat.getColor(context,R.color.white));
        }else {
            myHolder.binding.fl.setBackgroundResource(R.drawable.small_stroke_white);
            myHolder.binding.tvTitle.setTextColor(ContextCompat.getColor(context,R.color.black));
        }

        myHolder.itemView.setOnClickListener(v -> {
            if (appCompatActivity instanceof ChatActivity){
                ChatActivity activity = (ChatActivity) appCompatActivity;
                if (selectedPos!=-1){
                    RateReason reason = list.get(selectedPos);
                    reason.setSelected(false);
                    list.set(selectedPos,reason);
                    notifyItemChanged(selectedPos);
                }
                selectedPos = myHolder.getAdapterPosition();
                RateReason reason = list.get(myHolder.getAdapterPosition());
                list.set(myHolder.getAdapterPosition(),reason);
                reason.setSelected(true);
                activity.setRateItem(reason);
                notifyItemChanged(myHolder.getAdapterPosition());
            }else {
                if (fragment!=null){
                    if (fragment instanceof Fragment_Driver_My_Order){
                        Fragment_Driver_My_Order fragment_driver_my_order = (Fragment_Driver_My_Order) fragment;
                        if (selectedPos!=-1){
                            RateReason reason = list.get(selectedPos);
                            reason.setSelected(false);
                            list.set(selectedPos,reason);
                            notifyItemChanged(selectedPos);
                        }
                        selectedPos = myHolder.getAdapterPosition();
                        RateReason reason = list.get(myHolder.getAdapterPosition());
                        list.set(myHolder.getAdapterPosition(),reason);
                        reason.setSelected(true);
                        fragment_driver_my_order.setRateItem(reason);
                        notifyItemChanged(myHolder.getAdapterPosition());
                    }else if (fragment instanceof Fragment_Order){
                        Fragment_Order fragment_order = (Fragment_Order) fragment;
                        if (selectedPos!=-1){
                            RateReason reason = list.get(selectedPos);
                            reason.setSelected(false);
                            list.set(selectedPos,reason);
                            notifyItemChanged(selectedPos);
                        }
                        selectedPos = myHolder.getAdapterPosition();
                        RateReason reason = list.get(myHolder.getAdapterPosition());
                        list.set(myHolder.getAdapterPosition(),reason);
                        reason.setSelected(true);
                        fragment_order.setRateItem(reason);
                        notifyItemChanged(myHolder.getAdapterPosition());
                    }
                }
            }


        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public RateReasonRowBinding binding;

        public MyHolder(@NonNull RateReasonRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


    public void addData(List<RateReason> rateReasonList){
        selectedPos = -1;
        list.clear();
        list.addAll(rateReasonList);
        notifyDataSetChanged();
    }


}
