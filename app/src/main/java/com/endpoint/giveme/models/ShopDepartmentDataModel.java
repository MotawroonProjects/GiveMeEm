package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class ShopDepartmentDataModel implements Serializable {
    private List<ShopDepartments> data;

    public List<ShopDepartments> getData() {
        return data;
    }
}
