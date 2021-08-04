package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class NotificationDataModel implements Serializable {
    private int current_page;
    private List<NotificationModel> data;

    public int getCurrent_page() {
        return current_page;
    }

    public List<NotificationModel> getData() {
        return data;
    }

    public static class NotificationModel implements Serializable
    {
        private int id;
        private String from_user_id;
        private String to_user_id;
        private String order_id;
        private String offer_id;
        private String title;
        private String title_en;
        private String message;
        private String message_en;
        private String action;
        private UserModel.User from_user;
        private UserModel.User to_user;
        private String notification_date;


        public int getId() {
            return id;
        }

        public String getFrom_user_id() {
            return from_user_id;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public String getOrder_id() {
            return order_id;
        }

        public String getOffer_id() {
            return offer_id;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getAction() {
            return action;
        }

        public UserModel.User getFrom_user() {
            return from_user;
        }

        public UserModel.User getTo_user() {
            return to_user;
        }

        public String getNotification_date() {
            return notification_date;
        }

        public String getTitle_en() {
            return title_en;
        }

        public String getMessage_en() {
            return message_en;
        }
    }
}
