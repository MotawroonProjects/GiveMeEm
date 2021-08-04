package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_order_products.AddOrderProductActivity;
import com.endpoint.giveme.databinding.AddOrderSelectedProductRowBinding;
import com.endpoint.giveme.models.AdditionModel;
import com.endpoint.giveme.models.ProductModel;

import java.util.List;

import io.paperdb.Paper;

public class AddOrderSelectedProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProductModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String currency;
    private AddOrderProductActivity activity;
    private String lang;

    public AddOrderSelectedProductAdapter(List<ProductModel> list, Context context, String currency) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        Paper.init(context);
        this.currency = currency;
        activity = (AddOrderProductActivity) context;
        lang = Paper.book().read("lang","ar");


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        AddOrderSelectedProductRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.add_order_selected_product_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        ProductModel model = list.get(position);
        myHolder.binding.setModel(model);
        myHolder.binding.setCurrency(currency);
        myHolder.binding.setAdditions(getAdditions(model));
        myHolder.binding.tvIncrease.setOnClickListener(v -> {
            int pos = myHolder.getAdapterPosition();
            ProductModel productModel = list.get(pos);
            int count = productModel.getCount()+1;
            productModel.setCount(count);
            myHolder.binding.setModel(productModel);
            activity.updateItemCount(productModel,pos);
            notifyItemChanged(pos);

        });

        myHolder.binding.tvDecrease.setOnClickListener(v -> {
            int pos = myHolder.getAdapterPosition();
            ProductModel productModel = list.get(pos);
            int count = productModel.getCount();
            if (count>1){
                count -=1;
                productModel.setCount(count);
                myHolder.binding.setModel(productModel);
                activity.updateItemCount(productModel,pos);
                notifyItemChanged(pos);

            }

        });

        myHolder.itemView.setOnClickListener(v -> {
            int pos = myHolder.getAdapterPosition();
            ProductModel productModel = list.get(pos);
            if (productModel.getSelectedAdditions().size()>0){
                activity.setItemProduct(productModel);
            }
        });

    }
    private String getAdditions(ProductModel productModel){
        String additions="";
        if (productModel.getSelectedAdditions().size()>0){
            for (AdditionModel additionModel:productModel.getSelectedAdditions()){
                additions += additionModel.getTitle();
                additions +=",";
            }
        }

        return additions;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public AddOrderSelectedProductRowBinding binding;

        public MyHolder(@NonNull AddOrderSelectedProductRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
