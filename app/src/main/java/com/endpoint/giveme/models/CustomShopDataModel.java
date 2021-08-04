package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class CustomShopDataModel implements Serializable {
    private String shop_id;
    private int market_id;
    private String shop_name;
    private String shop_address;
    private double shop_lat;
    private double shop_lng;
    private String max_offer_value;
    private boolean isOpen;
    private String comments_count;
    private String rate;
    private String place_type;
    private CustomPlaceModel.DeliveryOffer deliveryOffer;
    private List<ShopDepartments> shopDepartmentsList;
    private List<ProductModel> productModelList;
    private List<HourModel> hourModelList;
    private List<CustomPlaceModel.Days> days;

    public CustomShopDataModel(String shop_id,int market_id ,String shop_name, String shop_address, double shop_lat, double shop_lng, String max_offer_value, boolean isOpen, String comments_count, String rate, String place_type, CustomPlaceModel.DeliveryOffer deliveryOffer, List<HourModel> hourModelList, List<CustomPlaceModel.Days> days) {
        this.shop_id = shop_id;
        this.market_id = market_id;
        this.shop_name = shop_name;
        this.shop_address = shop_address;
        this.shop_lat = shop_lat;
        this.shop_lng = shop_lng;
        this.max_offer_value = max_offer_value;
        this.isOpen = isOpen;
        this.comments_count = comments_count;
        this.rate = rate;
        this.place_type = place_type;
        this.deliveryOffer = deliveryOffer;
        this.hourModelList = hourModelList;
        this.days = days;
    }


    public String getShop_id() {
        return shop_id;
    }

    public void setShop_id(String shop_id) {
        this.shop_id = shop_id;
    }

    public String getShop_name() {
        return shop_name;
    }

    public String getShop_address() {
        return shop_address;
    }

    public double getShop_lat() {
        return shop_lat;
    }

    public double getShop_lng() {
        return shop_lng;
    }

    public String getMax_offer_value() {
        return max_offer_value;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getComments_count() {
        return comments_count;
    }

    public String getRate() {
        return rate;
    }

    public String getPlace_type() {
        return place_type;
    }

    public CustomPlaceModel.DeliveryOffer getDeliveryOffer() {
        return deliveryOffer;
    }

    public List<ShopDepartments> getShopDepartmentsList() {
        return shopDepartmentsList;
    }

    public List<ProductModel> getProductModelList() {
        return productModelList;
    }

    public List<HourModel> getHourModelList() {
        return hourModelList;
    }

    public List<CustomPlaceModel.Days> getDays() {
        return days;
    }

    public void setShopDepartmentsList(List<ShopDepartments> shopDepartmentsList) {
        this.shopDepartmentsList = shopDepartmentsList;
    }

    public void setProductModelList(List<ProductModel> productModelList) {
        this.productModelList = productModelList;
    }

    public void setHourModelList(List<HourModel> hourModelList) {
        this.hourModelList = hourModelList;
    }

    public int getMarket_id() {
        return market_id;
    }
}
