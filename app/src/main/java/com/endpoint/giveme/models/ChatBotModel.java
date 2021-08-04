package com.endpoint.giveme.models;

import java.io.Serializable;

public class ChatBotModel implements Serializable {
    private String text;
    private String image_url;
    private String from_address;
    private String to_address;
    private double from_lat;
    private double from_lng;
    private double to_lat;
    private double to_lng;
    private double distance;
    private double rate;
    private int type;
    private boolean enabled = true;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public double getFrom_lat() {
        return from_lat;
    }

    public void setFrom_lat(double from_lat) {
        this.from_lat = from_lat;
    }

    public double getFrom_lng() {
        return from_lng;
    }

    public void setFrom_lng(double from_lng) {
        this.from_lng = from_lng;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }



    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
