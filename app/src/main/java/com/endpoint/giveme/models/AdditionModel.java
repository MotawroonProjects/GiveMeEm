package com.endpoint.giveme.models;

import java.io.Serializable;

public class AdditionModel implements Serializable {
    private int id;
    private String product_id;
    private String market_id;
    private String title;
    private String image;
    private String price;

    public int getId() {
        return id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getMarket_id() {
        return market_id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getPrice() {
        return price;
    }


}
