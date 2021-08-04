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
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.databinding.LoadMoreRowBinding;
import com.endpoint.giveme.databinding.OfferRowBinding;
import com.endpoint.giveme.models.OffersModel;

import java.util.List;
import java.util.Locale;

public class OffersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<OffersModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String currency;
    private ChatActivity activity;

    public OffersAdapter(List<OffersModel> list, Context context,String currency) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.currency = currency;
        this.activity = (ChatActivity) context;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            OfferRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.offer_row, parent, false);
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
            OffersModel offersModel = list.get(position);
            myHolder.binding.setModel(offersModel);
            double cost = Double.parseDouble(offersModel.getOffer_value())+Double.parseDouble(offersModel.getTax_value());
            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s",String.format(Locale.ENGLISH,"%.2f",cost),currency));

            if (Double.parseDouble(offersModel.getOffer_value())>Double.parseDouble(offersModel.getMin_offer())){
                myHolder.binding.btnCancel.setText(R.string.less_offer);
            }else {
                myHolder.binding.btnCancel.setText(R.string.cancel);

            }


            myHolder.binding.btnCancel.setOnClickListener(v -> {
                OffersModel offersModel1 = list.get(holder.getAdapterPosition());

                if (Double.parseDouble(offersModel.getOffer_value())>Double.parseDouble(offersModel1.getMin_offer())){
                    activity.clientRefuseOffer(offersModel1,"yes");
                }else {
                    activity.deleteOrderActionBeforeDriverAcceptOrderActions(offersModel1);
                }


            });
            myHolder.binding.btnAccept.setOnClickListener(v -> {
                OffersModel offersModel1 = list.get(holder.getAdapterPosition());
                activity.clientAcceptOffer(offersModel1);
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
        private OfferRowBinding binding;

        public MyHolder(OfferRowBinding binding) {
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
