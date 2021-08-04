package com.endpoint.giveme.models;

import java.io.Serializable;

public class RangeOfferModel implements Serializable {
    private String min_offer;
    private String max_offer;

    public String getMin_offer() {
        return min_offer;
    }

    public String getMax_offer() {
        return max_offer;
    }
}
