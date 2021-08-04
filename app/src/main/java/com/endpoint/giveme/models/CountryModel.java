package com.endpoint.giveme.models;

import java.io.Serializable;

public class CountryModel implements Serializable {
    private String id_country;
    private String phone_code;
    private String iso_two;
    private String big_flag;
    private Word word;


    public String getId_country() {
        return id_country;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public String getIso_two() {
        return iso_two;
    }

    public String getBig_flag() {
        return big_flag;
    }

    public Word getWord() {
        return word;
    }

    public static class Word implements Serializable{
        private String name;
        private String currency;



        public String getName() {
            return name;
        }

        public String getCurrency() {
            return currency;
        }


    }



}
