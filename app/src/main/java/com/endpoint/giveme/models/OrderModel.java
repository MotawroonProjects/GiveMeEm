package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class OrderModel implements Serializable {
    private int id;
    private String order_code;
    private String order_status;
    private String order_type;
    private String user_id;
    private String driver_id;
    private String market_id="0";
    private String google_palce_id="";
    private String bill_cost;
    private String bill_step;
    private String payment_method;
    private String bill_image;
    private String room_id;
    private String app_rate;
    private String app_rate_amount;
    private String client_address;
    private String client_latitude;
    private String client_longitude;
    private String market_name;
    private String market_address;
    private String market_latitude;
    private String market_longitude;
    private String order_time_arrival;
    private String order_date;
    private String coupon_id;
    private String details;
    private String notes;
    private String offers_count;

    private CouponModel coupon;
    private MarketModel market;
    private UserModel.User client;
    private UserModel.User driver;
    private String pick_up_distance="0.0";
    private String drop_off_distance = "0.0";
    private DriverLastOffer driver_last_offer;
    private DriverLastOffer order_offer;
    private List<OrderImages>order_images;
    private RangeOfferModel limit_offer;

    public String getOffers_count() {
        return offers_count;
    }

    public int getId() {
        return id;
    }

    public String getOrder_code() {
        return order_code;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getOrder_type() {
        return order_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public String getMarket_id() {
        return market_id;
    }

    public String getGoogle_palce_id() {
        return google_palce_id;
    }

    public String getBill_cost() {
        return bill_cost;
    }

    public String getBill_step() {
        return bill_step;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public String getBill_image() {
        return bill_image;
    }

    public String getRoom_id() {
        return room_id;
    }

    public String getApp_rate() {
        return app_rate;
    }

    public String getApp_rate_amount() {
        return app_rate_amount;
    }

    public String getClient_address() {
        return client_address;
    }

    public String getClient_latitude() {
        return client_latitude;
    }

    public String getClient_longitude() {
        return client_longitude;
    }

    public String getMarket_name() {
        return market_name;
    }

    public String getMarket_address() {
        return market_address;
    }

    public String getMarket_latitude() {
        return market_latitude;
    }

    public String getMarket_longitude() {
        return market_longitude;
    }

    public String getOrder_time_arrival() {
        return order_time_arrival;
    }

    public String getOrder_date() {
        return order_date;
    }

    public String getCoupon_id() {
        return coupon_id;
    }

    public String getDetails() {
        return details;
    }

    public String getNotes() {
        return notes;
    }

    public CouponModel getCoupon() {
        return coupon;
    }

    public MarketModel getMarket() {
        return market;
    }

    public UserModel.User getClient() {
        return client;
    }

    public UserModel.User getDriver() {
        return driver;
    }

    public DriverLastOffer getOrder_offer() {
        return order_offer;
    }

    public RangeOfferModel getLimit_offer() {
        return limit_offer;
    }

    public List<OrderImages> getOrder_images() {
        return order_images;
    }


    public DriverLastOffer getDriver_last_offer() {
        return driver_last_offer;
    }

    public String getPick_up_distance() {
        return pick_up_distance;
    }

    public void setPick_up_distance(String pick_up_distance) {
        this.pick_up_distance = pick_up_distance;
    }

    public String getDrop_off_distance() {
        return drop_off_distance;
    }

    public void setDrop_off_distance(String drop_off_distance) {
        this.drop_off_distance = drop_off_distance;
    }

    public static class OrderImages implements Serializable{
        private int id;
        private String image;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }


    }

    public static class DriverLastOffer implements Serializable{
        private int id;
        private String driver_id;
        private String user_id;
        private String order_id;
        private String offer_value;
        private String tax_value;
        private String status;
        private String offer_time;

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



    }
}
