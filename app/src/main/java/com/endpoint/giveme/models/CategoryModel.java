package com.endpoint.giveme.models;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private String id;
    private String title_ar;
    private String title_en;
    private String keyword_google;
    private String image;
    private String type;

    public String getId() {
        return id;
    }

    public String getTitle_ar() {
        return title_ar;
    }

    public String getTitle_en() {
        return title_en;
    }

    public String getKeyword_google() {
        return keyword_google;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }
}
