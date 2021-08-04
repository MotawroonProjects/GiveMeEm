package com.endpoint.giveme.models;

import java.io.Serializable;

public class RateReason implements Serializable {
    private int id;
    private String title;
    private boolean selected = false;

    public RateReason(int id, String title, boolean selected) {
        this.id = id;
        this.title = title;
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
