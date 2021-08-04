package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class FeedbackDataModel implements Serializable {
    private int current_page;
    private List<FeedbackModel> data;

    public int getCurrent_page() {
        return current_page;
    }

    public List<FeedbackModel> getData() {
        return data;
    }
}
