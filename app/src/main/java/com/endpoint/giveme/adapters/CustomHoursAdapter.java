package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.CustomWorkHourRowBinding;
import com.endpoint.giveme.models.CustomPlaceModel;

import java.util.List;

public class CustomHoursAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CustomPlaceModel.Days> list;
    private Context context;
    private LayoutInflater inflater;

    public CustomHoursAdapter(List<CustomPlaceModel.Days> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        CustomWorkHourRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.custom_work_hour_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public CustomWorkHourRowBinding binding;

        public MyHolder(@NonNull CustomWorkHourRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
