package com.endpoint.giveme.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_shop_products.ShopProductActivity;
import com.endpoint.giveme.databinding.AdditionRowBinding;
import com.endpoint.giveme.models.AdditionModel;

import java.util.List;

import io.paperdb.Paper;

public class AdditionProductAdapter extends RecyclerView.Adapter<AdditionProductAdapter.MyHolder> {
    private Context context;
    private List<AdditionModel> list;
    private String lang;
    private String currency;
    private ShopProductActivity activity;
    private SparseBooleanArray sparseBooleanArray;

    public AdditionProductAdapter(Context context, List<AdditionModel> list,String currency) {
        this.context = context;
        this.list = list;
        Paper.init(context);
        lang = Paper.book().read("lang", "ar");
        this.currency = currency;
        activity = (ShopProductActivity) context;
        sparseBooleanArray = new SparseBooleanArray();

    }

    @NonNull
    @Override
    public AdditionProductAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdditionRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.addition_row, parent, false);
        return new MyHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdditionProductAdapter.MyHolder holder, int position) {
        AdditionModel model = list.get(position);
        holder.binding.setCurrency(currency);
        holder.binding.setModel(model);

        if (sparseBooleanArray.get(position,false)){
            holder.binding.checkbox.setChecked(true);
        }else {
            holder.binding.checkbox.setChecked(false);
        }

        holder.binding.checkbox.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (holder.binding.checkbox.isChecked()){
                sparseBooleanArray.put(pos,true);
                activity.setAdditionItem(list.get(pos),pos,true);

            }else {
                sparseBooleanArray.put(pos,false);
                activity.setAdditionItem(list.get(pos),pos,false);

            }
            notifyItemChanged(pos);

        });


    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private AdditionRowBinding binding;

        public MyHolder(@NonNull AdditionRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }



    @Override
    public int getItemCount() {
        return list.size();
    }
}
