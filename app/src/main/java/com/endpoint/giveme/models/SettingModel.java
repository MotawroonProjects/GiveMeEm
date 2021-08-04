package com.endpoint.giveme.models;

import java.io.Serializable;

public class SettingModel implements Serializable {
    private Setting settings;

    public Setting getSettings() {
        return settings;
    }

    public static class Setting implements Serializable{
        private String facebook;
        private String twitter;
        private String instagram;
        private String telegram;
        private String whatsapp;
        private String complaint_list;
        private String submit_the_complaint;
        private String terms_and_conditions;
        private String privacy_policy;


        public String getFacebook() {
            return facebook;
        }

        public String getTwitter() {
            return twitter;
        }

        public String getInstagram() {
            return instagram;
        }

        public String getTelegram() {
            return telegram;
        }

        public String getWhatsapp() {
            return whatsapp;
        }

        public String getComplaint_list() {
            return complaint_list;
        }

        public String getSubmit_the_complaint() {
            return submit_the_complaint;
        }

        public String getTerms_and_conditions() {
            return terms_and_conditions;
        }

        public String getPrivacy_policy() {
            return privacy_policy;
        }
    }
}
