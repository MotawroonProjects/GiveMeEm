package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.SelectedAdditionRowBinding;
import com.endpoint.giveme.models.AdditionModel;

import java.util.List;

import io.paperdb.Paper;

public class SelectedAdditionProductAdapter extends RecyclerView.Adapter<SelectedAdditionProductAdapter.MyHolder> {
    private Context context;
    private List<AdditionModel> list;
    private String lang;
    private String currency;

    public SelectedAdditionProductAdapter(Context context, List<AdditionModel> list, String currency) {
        this.context = context;
        this.list = list;
        Paper.init(context);
        lang = Paper.book().read("lang", "ar");
        this.currency = currency;


    }

    @NonNull
    @Override
    public SelectedAdditionProductAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SelectedAdditionRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.selected_addition_row, parent, false);
        return new MyHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedAdditionProductAdapter.MyHolder holder, int position) {
        AdditionModel model = list.get(position);
        holder.binding.setCurrency(currency);
        holder.binding.setModel(model);



    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private SelectedAdditionRowBinding binding;

        public MyHolder(@NonNull SelectedAdditionRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }



    @Override
    public int getItemCount() {
        return list.size();
    }
}
