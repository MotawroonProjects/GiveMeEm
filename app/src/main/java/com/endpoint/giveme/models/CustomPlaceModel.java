package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class CustomPlaceModel implements Serializable {
    private int id;
    private String name;
    private String email;
    private String phone_code;
    private String phone;
    private String logo;
    private String rating;
    private String google_place_id;
    private String latitude;
    private String longitude;
    private String address;
    private String details;
    private boolean isOpen;
    private double distance;
    private String comments_count;
    private String products_count;
    private List<Gallery> gallary;
    private List<MenuImage> menu;
    private DeliveryOffer delivery_offer;
    private List<Days>days;

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

    public String getLogo() {
        return logo;
    }

    public String getRating() {
        return rating;
    }

    public String getGoogle_place_id() {
        return google_place_id;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<Days> getDays() {
        return days;
    }


    public String getDetails() {
        return details;
    }

    public List<Gallery> getGallary() {
        return gallary;
    }

    public List<MenuImage> getMenu() {
        return menu;
    }

    public DeliveryOffer getDelivery_offer() {
        return delivery_offer;
    }

    public String getComments_count() {
        return comments_count;
    }

    public String getProducts_count() {
        return products_count;
    }

    public static class  Gallery implements Serializable{
        private String image;

        public String getImage() {
            return image;
        }
    }



    public static class MenuImage implements Serializable{
        private String image;

        public String getImage() {
            return image;
        }
    }



    public static class DeliveryOffer implements Serializable{
        private int id;
        private String market_id;
        private String from_date;
        private String to_date;
        private String offer_type;
        private String offer_value;
        private String less_value;

        public int getId() {
            return id;
        }

        public String getMarket_id() {
            return market_id;
        }

        public String getFrom_date() {
            return from_date;
        }

        public String getTo_date() {
            return to_date;
        }

        public String getOffer_type() {
            return offer_type;
        }

        public String getOffer_value() {
            return offer_value;
        }

        public String getLess_value() {
            return less_value;
        }
    }

    public static class Days implements Serializable{
        private int day;
        private String status;
        private String from_time;
        private String to_time;


        public int getDay() {
            return day;
        }

        public String getStatus() {
            return status;
        }

        public String getFrom_time() {
            return from_time;
        }

        public String getTo_time() {
            return to_time;
        }
    }


}
