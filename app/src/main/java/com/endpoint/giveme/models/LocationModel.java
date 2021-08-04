package com.endpoint.giveme.models;

import java.io.Serializable;

public class LocationModel implements Serializable {
    private double lat;
    private double lng;

    public LocationModel(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
