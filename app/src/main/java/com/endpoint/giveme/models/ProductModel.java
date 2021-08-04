package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductModel implements Serializable {
    private int id;
    private String title;
    private String image;
    private String sub_department_id;
    private String market_id;
    private String price;
    private String price_old;
    private String have_offer;
    private String offer_type;
    private String offer_value;
    private String details;
    private double total_cost;
    private List<AdditionModel> addtions;
    private List<AdditionModel> selectedAdditions = new ArrayList<>();
    private int count = 0;
    private ShopDepartments sub_department;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getSub_department_id() {
        return sub_department_id;
    }

    public String getMarket_id() {
        return market_id;
    }

    public String getPrice() {
        return price;
    }

    public String getPrice_old() {
        return price_old;
    }

    public String getHave_offer() {
        return have_offer;
    }

    public String getOffer_type() {
        return offer_type;
    }

    public String getOffer_value() {
        return offer_value;
    }

    public String getDetails() {
        return details;
    }

    public ShopDepartments getSub_department() {
        return sub_department;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public List<AdditionModel> getAddtions() {
        return addtions;
    }

    public List<AdditionModel> getSelectedAdditions() {
        return selectedAdditions;
    }

    public void setSelectedAdditions(List<AdditionModel> selectedAdditions) {
        this.selectedAdditions = selectedAdditions;
    }

    public double getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(double total_cost) {
        this.total_cost = total_cost;
    }
}
