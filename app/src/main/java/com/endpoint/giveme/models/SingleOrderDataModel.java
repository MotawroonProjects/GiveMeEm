package com.endpoint.giveme.models;

import java.io.Serializable;

public class SingleOrderDataModel implements Serializable {
    private OrderModel order;

    public OrderModel getOrder() {
        return order;
    }
}
