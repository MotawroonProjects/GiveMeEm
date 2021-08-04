package com.endpoint.giveme.models;

import java.io.Serializable;

public class CouponModel implements Serializable {
    private int id;
    private String from_date;
    private String to_date;
    private String coupon_num;
    private String coupon_type;
    private String coupon_value;


    public int getId() {
        return id;
    }

    public String getFrom_date() {
        return from_date;
    }

    public String getTo_date() {
        return to_date;
    }

    public String getCoupon_num() {
        return coupon_num;
    }

    public String getCoupon_type() {
        return coupon_type;
    }

    public String getCoupon_value() {
        return coupon_value;
    }
}
