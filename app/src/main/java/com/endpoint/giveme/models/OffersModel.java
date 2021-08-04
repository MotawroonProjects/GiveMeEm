package com.endpoint.giveme.models;

import java.io.Serializable;

public class OffersModel implements Serializable {
    private int id;
    private String driver_id;
    private String user_id;
    private String order_id;
    private String offer_value;
    private String tax_value;
    private String status;
    private String offer_time;
    private String order_time;
    private String distance;
    private String min_offer;
    private UserModel.User client;
    private UserModel.User driver;


    public int getId() {
        return id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getOffer_value() {
        return offer_value;
    }

    public String getTax_value() {
        return tax_value;
    }

    public String getStatus() {
        return status;
    }

    public String getOffer_time() {
        return offer_time;
    }

    public String getOrder_time() {
        return order_time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setOrder_time(String order_time) {
        this.order_time = order_time;
    }

    public UserModel.User getClient() {
        return client;
    }

    public UserModel.User getDriver() {
        return driver;
    }

    public String getMin_offer() {
        return min_offer;
    }
}
