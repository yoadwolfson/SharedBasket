package com.sharedbasket.dto;

import java.io.Serializable;

public class CreateCategoryRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String color;
    private Integer sortOrder;

    public CreateCategoryRequestDTO() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
