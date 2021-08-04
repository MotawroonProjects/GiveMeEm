package com.endpoint.giveme.models;

import java.io.Serializable;

public class SingleMessageDataModel implements Serializable {
    private MessageModel data;

    public MessageModel getData() {
        return data;
    }
}
