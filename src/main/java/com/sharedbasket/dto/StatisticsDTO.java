package com.sharedbasket.dto;

import java.io.Serializable;

public class StatisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int totalItems;
    private double totalCost;
    private int itemsPurchased;

    public StatisticsDTO() {}
    public StatisticsDTO(int totalItems, double totalCost, int itemsPurchased) {
        this.totalItems = totalItems;
        this.totalCost = totalCost;
        this.itemsPurchased = itemsPurchased;
    }
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    public int getItemsPurchased() { return itemsPurchased; }
    public void setItemsPurchased(int itemsPurchased) { this.itemsPurchased = itemsPurchased; }
}
