package com.endpoint.giveme.models;

import java.io.Serializable;
import java.util.List;

public class CategoryDataModel implements Serializable {
    private List<CategoryModel> data;

    public List<CategoryModel> getData() {
        return data;
    }
}
