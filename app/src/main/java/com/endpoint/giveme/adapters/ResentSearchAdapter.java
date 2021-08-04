package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_shops.ShopsActivity;
import com.endpoint.giveme.databinding.RecentSearchRowBinding;

import java.util.List;

public class ResentSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> list;
    private Context context;
    private LayoutInflater inflater;
    private ShopsActivity activity;

    public ResentSearchAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (ShopsActivity) context;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        RecentSearchRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.recent_search_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setTitle(list.get(position));



        myHolder.itemView.setOnClickListener(v -> {
            String title = list.get(myHolder.getAdapterPosition());
            activity.setRecentSearchItem(title);


        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public RecentSearchRowBinding binding;

        public MyHolder(@NonNull RecentSearchRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
