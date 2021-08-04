package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class SliderModel implements Serializable {

    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public static class Data implements Serializable{

        private String id;
        private String image;
        private String google_place_id;



        public Data() {
        }

        public Data(String id, String image, String google_place_id) {
            this.id = id;
            this.image = image;
            this.google_place_id = google_place_id;
        }

        public String getId() {
            return id;
        }

        public String getImage() {
            return image;
        }

        public String getGoogle_place_id() {
            return google_place_id;
        }
    }
}
