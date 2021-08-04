package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class MarketModel implements Serializable {
    private int id;
    private String name;
    private String email;
    private String phone_code;
    private String phone;
    private String admin_type;
    private String register_status;
    private String register_type;
    private String parent;
    private String logo;
    private String rating;
    private String google_place_id;
    private String latitude;
    private String longitude;
    private String address;
    private String city_id;
    private String neighbor_id;
    private List<CustomPlaceModel.Days> days;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public String getPhone() {
        return phone;
    }

    public String getAdmin_type() {
        return admin_type;
    }

    public String getRegister_status() {
        return register_status;
    }

    public String getRegister_type() {
        return register_type;
    }

    public String getParent() {
        return parent;
    }

    public String getLogo() {
        return logo;
    }

    public String getRating() {
        return rating;
    }

    public String getGoogle_place_id() {
        return google_place_id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getCity_id() {
        return city_id;
    }

    public String getNeighbor_id() {
        return neighbor_id;
    }

    public List<CustomPlaceModel.Days> getDays() {
        return days;
    }
}
