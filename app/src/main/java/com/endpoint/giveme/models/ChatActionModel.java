package com.endpoint.giveme.models;

import java.io.Serializable;

public class ChatActionModel implements Serializable {
    private String action;
    private boolean selected = false;

    public ChatActionModel(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
