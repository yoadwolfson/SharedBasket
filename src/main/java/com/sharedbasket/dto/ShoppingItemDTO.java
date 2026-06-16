package com.sharedbasket.dto;

import java.io.Serializable;

public class ShoppingItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int quantity;
    private double estimatedPrice;

    public ShoppingItemDTO() {}

    public ShoppingItemDTO(String name, int quantity, double estimatedPrice) {
        this.name = name;
        this.quantity = quantity;
        this.estimatedPrice = estimatedPrice;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = estimatedPrice; }
}
