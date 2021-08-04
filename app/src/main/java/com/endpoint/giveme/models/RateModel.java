package com.endpoint.giveme.models;

import java.io.Serializable;

public class RateModel implements Serializable {
    private int rate;
    private int reason;
    private String comment;

    public RateModel() {
        rate = 0;
        reason = 0;
        comment = "";
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
