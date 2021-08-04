package com.endpoint.giveme.adapters.shop_products_adapters;

import com.endpoint.giveme.databinding.ProductChildRowBinding;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class ShopChildViewHolder extends ChildViewHolder {
    public ProductChildRowBinding binding;
    public ShopChildViewHolder(ProductChildRowBinding binding) {
        super(binding.getRoot());
        this.binding =binding;
    }
}
