package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddOrderProductsModel implements Serializable {
    private int user_id;
    private String shop_id;
    private int market_id;
    private String shop_name;
    private String shop_icon;
    private String shop_address;
    private double shop_lat;
    private double shop_lng;
    private double to_address;
    private double to_lat;
    private double to_lng;
    private int count;
    private double total_cost;
    private List<ProductModel> productModelList = new ArrayList<>();


    public String getShop_id() {
        return shop_id;
    }

    public void setShop_id(String shop_id) {
        this.shop_id = shop_id;
    }

    public int getMarket_id() {
        return market_id;
    }

    public void setMarket_id(int market_id) {
        this.market_id = market_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getShop_icon() {
        return shop_icon;
    }

    public void setShop_icon(String shop_icon) {
        this.shop_icon = shop_icon;
    }

    public String getShop_address() {
        return shop_address;
    }

    public void setShop_address(String shop_address) {
        this.shop_address = shop_address;
    }

    public double getShop_lat() {
        return shop_lat;
    }

    public void setShop_lat(double shop_lat) {
        this.shop_lat = shop_lat;
    }

    public double getShop_lng() {
        return shop_lng;
    }

    public void setShop_lng(double shop_lng) {
        this.shop_lng = shop_lng;
    }

    public double getTo_address() {
        return to_address;
    }

    public void setTo_address(double to_address) {
        this.to_address = to_address;
    }

    public double getTo_lat() {
        return to_lat;
    }

    public void setTo_lat(double to_lat) {
        this.to_lat = to_lat;
    }

    public double getTo_lng() {
        return to_lng;
    }

    public void setTo_lng(double to_lng) {
        this.to_lng = to_lng;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(double total_cost) {
        this.total_cost = total_cost;
    }

    public List<ProductModel> getProductModelList() {
        return productModelList;
    }

    public void setProductModelList(List<ProductModel> productModelList) {
        this.productModelList = productModelList;
    }
}
