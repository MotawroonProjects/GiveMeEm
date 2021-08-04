package com.endpoint.giveme.adapters.shop_products_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.GroupTitleRowBinding;
import com.endpoint.giveme.models.ShopDepartments;

import java.util.List;

import io.paperdb.Paper;

public class ProductSectionAdapter extends RecyclerView.Adapter<ShopGroupViewHolder> {
    private Context context;
    private String currency;
    private List<ShopDepartments> list;
    private String lang;

    public ProductSectionAdapter(Context context, String currency, List<ShopDepartments> list) {
        this.context = context;
        this.currency = currency;
        this.list = list;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");

    }

    @NonNull
    @Override
    public ShopGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GroupTitleRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.group_title_row,parent,false);
        return new ShopGroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopGroupViewHolder holder, int position) {
        ShopDepartments departments = list.get(position);
        String title = "";
        if (lang.equals("ar")){
            title = departments.getTitle_ar();
        }else {
            title = departments.getTitle_en();
        }
        holder.binding.setTitle(title);
        ProductAdapter adapter = new ProductAdapter(context,currency,departments.getProducts_list(),position);
        holder.binding.recView.setLayoutManager(new LinearLayoutManager(context));
        holder.binding.recView.setAdapter(adapter);
        holder.binding.llExpand.setOnClickListener(v -> {
            if (holder.binding.expandLayout.isExpanded()){
                holder.binding.expandLayout.collapse(true);
                holder.binding.imageArrow.clearAnimation();
                holder.binding.imageArrow.animate().setDuration(200).rotation(180).start();
            }else {
                holder.binding.expandLayout.expand(true);
                holder.binding.imageArrow.animate().setDuration(200).rotation(0).start();


            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
