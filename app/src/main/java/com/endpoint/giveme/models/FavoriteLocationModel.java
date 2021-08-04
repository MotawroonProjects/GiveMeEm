package com.endpoint.giveme.models;

import java.io.Serializable;

public class FavoriteLocationModel implements Serializable {
    private String name;
    private String type;
    private String address;
    private double lat;
    private double lng;

    public FavoriteLocationModel(String name, String type, String address, double lat, double lng) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
