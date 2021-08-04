package com.endpoint.giveme.models;

import java.io.Serializable;

public class FromToLocationModel implements Serializable {
    private double fromLat;
    private double fromLng;
    private String fromAddress;
    private double distance_me_pick_up_location;

    private double toLat;
    private double toLng;
    private String toAddress;
    private double distance_me_drop_off_location;
    private double from_location_to_location_distance;
    private double userLat;
    private double userLng;


    public FromToLocationModel(double fromLat, double fromLng, String fromAddress, double distance_me_pick_up_location, double toLat, double toLng, String toAddress, double distance_me_drop_off_location, double from_location_to_location_distance, double userLat, double userLng) {
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.fromAddress = fromAddress;
        this.distance_me_pick_up_location = distance_me_pick_up_location;
        this.toLat = toLat;
        this.toLng = toLng;
        this.toAddress = toAddress;
        this.distance_me_drop_off_location = distance_me_drop_off_location;
        this.from_location_to_location_distance = from_location_to_location_distance;
        this.userLat = userLat;
        this.userLng = userLng;
    }

    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double fromLat) {
        this.fromLat = fromLat;
    }

    public double getFromLng() {
        return fromLng;
    }

    public void setFromLng(double fromLng) {
        this.fromLng = fromLng;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public double getDistance_me_pick_up_location() {
        return distance_me_pick_up_location;
    }

    public void setDistance_me_pick_up_location(double distance_me_pick_up_location) {
        this.distance_me_pick_up_location = distance_me_pick_up_location;
    }

    public double getToLat() {
        return toLat;
    }

    public void setToLat(double toLat) {
        this.toLat = toLat;
    }

    public double getToLng() {
        return toLng;
    }

    public void setToLng(double toLng) {
        this.toLng = toLng;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public double getDistance_me_drop_off_location() {
        return distance_me_drop_off_location;
    }

    public void setDistance_me_drop_off_location(double distance_me_drop_off_location) {
        this.distance_me_drop_off_location = distance_me_drop_off_location;
    }

    public double getUserLat() {
        return userLat;
    }

    public void setUserLat(double userLat) {
        this.userLat = userLat;
    }

    public double getUserLng() {
        return userLng;
    }

    public void setUserLng(double userLng) {
        this.userLng = userLng;
    }

    public double getFrom_location_to_location_distance() {
        return from_location_to_location_distance;
    }
}
