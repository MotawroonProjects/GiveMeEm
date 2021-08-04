package com.endpoint.giveme.models;

import java.io.Serializable;

public class MainItemData implements Serializable {
    private int type = 0;

    public MainItemData(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
