package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class MessageDataModel implements Serializable {
    private int current_page;
    private List<MessageModel> data;

    public int getCurrent_page() {
        return current_page;
    }

    public List<MessageModel> getData() {
        return data;
    }
}
