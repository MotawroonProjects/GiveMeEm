package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class PlaceDetailsModel implements Serializable {
    private String status;
    private PlaceDetails result;

    public PlaceDetails getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

    public class PlaceDetails implements Serializable
    {
        private String icon;
        private String name;
        private String place_id;
        private Opening_Hours opening_hours;
        private List<PhotosModel> photos;
        private List<Reviews> reviews;
        private List<String> types;
        private String vicinity;
        private double rating;
        private NearbyModel.Geometry geometry;

        public Opening_Hours getOpening_hours() {
            return opening_hours;
        }

        public List<PhotosModel> getPhotos() {
            return photos;
        }

        public List<Reviews> getReviews() {
            return reviews;
        }

        public String getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getPlace_id() {
            return place_id;
        }

        public List<String> getTypes() {
            return types;
        }

        public String getVicinity() {
            return vicinity;
        }

        public double getRating() {
            return rating;
        }

        public NearbyModel.Geometry getGeometry() {
            return geometry;
        }
    }

    public class Opening_Hours implements Serializable
    {

        private boolean open_now = false;
        private List<Periods> periods;
        private List<String> weekday_text;
        public List<Periods> getPeriods() {
            return periods;
        }

        public List<String> getWeekday_text() {
            return weekday_text;
        }

        public boolean isOpen_now() {
            return open_now;
        }
    }

    public class Periods implements Serializable
    {
        private Open open;

        public Open getOpen() {
            return open;
        }
    }

    public class Open implements Serializable
    {
        private String time;

        public String getTime() {
            return time;
        }
    }



    public class Reviews implements Serializable
    {
        private String author_name;
        private String profile_photo_url;
        private String relative_time_description;
        private double rating;
        private String text;

        public String getAuthor_name() {
            return author_name;
        }

        public String getProfile_photo_url() {
            return profile_photo_url;
        }

        public String getRelative_time_description() {
            return relative_time_description;
        }

        public double getRating() {
            return rating;
        }

        public String getText() {
            return text;
        }
    }
}
