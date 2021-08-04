package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddOrderTextModel implements Serializable {
    private int user_id;
    private String place_id;
    private int market_id;
    private String place_name;
    private String place_address;
    private double place_lat;
    private double place_lng;
    private String to_address;
    private double to_lat;
    private double to_lng;
    private String payment;
    private String comments;
    private String order_text;
    private String order_type;
    private String coupon_id="0";
    private int time;
    private List<String> images = new ArrayList<>();

    public int getUser_id() {
        return user_id;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public int getMarket_id() {
        return market_id;
    }

    public void setMarket_id(int market_id) {
        this.market_id = market_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public String getPlace_address() {
        return place_address;
    }

    public void setPlace_address(String place_address) {
        this.place_address = place_address;
    }

    public double getPlace_lat() {
        return place_lat;
    }

    public void setPlace_lat(double place_lat) {
        this.place_lat = place_lat;
    }

    public double getPlace_lng() {
        return place_lng;
    }

    public void setPlace_lng(double place_lng) {
        this.place_lng = place_lng;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
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

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getOrder_text() {
        return order_text;
    }

    public void setOrder_text(String order_text) {
        this.order_text = order_text;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public String getCoupon_id() {
        return coupon_id;
    }

    public void setCoupon_id(String coupon_id) {
        this.coupon_id = coupon_id;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
