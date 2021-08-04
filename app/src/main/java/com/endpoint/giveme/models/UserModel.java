package com.endpoint.giveme.models;

import java.io.Serializable;

public class UserModel implements Serializable {

    private User user;

    public User getUser() {
        return user;
    }

    public static class User implements Serializable {
        private int id;
        private String name;
        private String email;
        private String phone_code;
        private String phone;
        private String logo;
        private String token;
        private String latitude;
        private String longitude;
        private String rate;
        private String address;
        private String user_type;
        private String gender;
        private String register_link;
        private String receive_notifications;
        private String date_of_birth;
        private String fireBaseToken;
        private CountryModel country;


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

        public String getToken() {
            return token;
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

        public String getUser_type() {
            return user_type;
        }

        public void setUser_type(String user_type) {
            this.user_type = user_type;
        }

        public String getGender() {
            return gender;
        }

        public String getDate_of_birth() {
            return date_of_birth;
        }

        public String getFireBaseToken() {
            return fireBaseToken;
        }

        public void setFireBaseToken(String fireBaseToken) {
            this.fireBaseToken = fireBaseToken;
        }

        public String getReceive_notifications() {
            return receive_notifications;
        }

        public String getRate() {
            return rate;
        }

        public String getRegister_link() {
            return register_link;
        }

        public CountryModel getCountry() {
            return country;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
    }
}
