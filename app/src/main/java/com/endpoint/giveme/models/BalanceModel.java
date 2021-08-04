package com.endpoint.giveme.models;

import java.io.Serializable;

public class BalanceModel implements Serializable {
    private double user_balance = 0.0;
    private double delivery_fee = 0.0;
    private int orders = 0;
    private double my_rate = 0.0;

    public double getUser_balance() {
        return user_balance;
    }

    public double getDelivery_fee() {
        return delivery_fee;
    }

    public int getOrders() {
        return orders;
    }

    public double getMy_rate() {
        return my_rate;
    }
}
