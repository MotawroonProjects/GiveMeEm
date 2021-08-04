package com.endpoint.giveme.models;

import java.io.Serializable;

public class FeedbackModel implements Serializable {
    private int id;
    private String order_id;
    private String driver_id;
    private String user_id;
    private String comment;
    private String reason;
    private String rate;
    private UserModel.User client;
    private UserModel.User driver;

    public int getId() {
        return id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getComment() {
        return comment;
    }

    public String getReason() {
        return reason;
    }

    public String getRate() {
        return rate;
    }

    public UserModel.User getClient() {
        return client;
    }

    public UserModel.User getDriver() {
        return driver;
    }
}
