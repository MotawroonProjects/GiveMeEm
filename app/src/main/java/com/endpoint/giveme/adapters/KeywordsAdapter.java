package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_filter.FilterActivity;
import com.endpoint.giveme.databinding.KeywordRowBinding;
import com.endpoint.giveme.models.KeywordModel;

import java.util.List;

public class KeywordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<KeywordModel> list;
    private Context context;
    private LayoutInflater inflater;
    private FilterActivity activity;
    private int selected_pos = -1;

    public KeywordsAdapter(List<KeywordModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (FilterActivity) context;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        KeywordRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.keyword_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setKeyword(list.get(position).getName());
        if (selected_pos == position){
            myHolder.binding.rb.setChecked(true);
        }else {
            myHolder.binding.rb.setChecked(false);

        }

        myHolder.binding.rb.setOnClickListener(v -> {
            selected_pos = myHolder.getAdapterPosition();
            activity.setItemData(list.get(myHolder.getAdapterPosition()));
            notifyDataSetChanged();
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public KeywordRowBinding binding;

        public MyHolder(@NonNull KeywordRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
