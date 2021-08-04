package com.endpoint.giveme.models;

public class NotFireModel {
    private boolean status;
    private String type;

    public NotFireModel(boolean status, String type) {
        this.status = status;
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public String getType() {
        return type;
    }
}
