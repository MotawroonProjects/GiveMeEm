package com.endpoint.giveme.adapters.shop_products_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_shop_products.ShopProductActivity;
import com.endpoint.giveme.databinding.ProductCategoryTabRowBinding;
import com.endpoint.giveme.models.ShopDepartments;

import java.util.List;

import io.paperdb.Paper;

public class ProductCategoryAdapter extends RecyclerView.Adapter<ProductCategoryAdapter.MyHolder> {
    private Context context;
    private List<ShopDepartments> list;
    private String lang;
    private int selectedPos = 0;
    private ShopProductActivity activity;

    public ProductCategoryAdapter(Context context, List<ShopDepartments> list) {
        this.context = context;
        this.list = list;
        Paper.init(context);
        lang = Paper.book().read("lang", "ar");
        activity = (ShopProductActivity) context;

    }

    @NonNull
    @Override
    public ProductCategoryAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProductCategoryTabRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.product_category_tab_row, parent, false);
        return new MyHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductCategoryAdapter.MyHolder holder, int position) {
        ShopDepartments departments = list.get(position);
        if (lang.equals("ar")) {
            holder.binding.setTitle(departments.getTitle_ar());
        } else {
            holder.binding.setTitle(departments.getTitle_en());

        }

        if (position==selectedPos){
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
            holder.binding.view.setVisibility(View.VISIBLE);
        }else {
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context,R.color.gray4));
            holder.binding.view.setVisibility(View.GONE);

        }

        holder.itemView.setOnClickListener(v -> {
            activity.setSelectedDepartmentPosition(holder.getAdapterPosition());
            setSelectedPos(holder.getAdapterPosition());
        });
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private ProductCategoryTabRowBinding binding;

        public MyHolder(@NonNull ProductCategoryTabRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
