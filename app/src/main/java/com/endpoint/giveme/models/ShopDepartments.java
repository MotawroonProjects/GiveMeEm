package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class ShopDepartments implements Serializable {
    private int id;
    private String market_id;
    private String title_ar;
    private String title_en;
    private String image;
    private int count;
    private List<ProductModel>products_list;

    public int getId() {
        return id;
    }

    public String getMarket_id() {
        return market_id;
    }

    public String getTitle_ar() {
        return title_ar;
    }

    public String getTitle_en() {
        return title_en;
    }

    public String getImage() {
        return image;
    }

    public List<ProductModel> getProducts_list() {
        return products_list;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
