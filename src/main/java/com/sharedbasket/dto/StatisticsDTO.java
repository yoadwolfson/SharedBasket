package com.sharedbasket.dto;

import java.io.Serializable;
import java.time.Instant;

public class StatisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int totalItems;
    private double totalCost;
    private int itemsPurchased;
    private Instant periodStart;
    private Instant periodEnd;
    private String currency = "NIS";

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
    public Instant getPeriodStart() { return periodStart; }
    public void setPeriodStart(Instant periodStart) { this.periodStart = periodStart; }
    public Instant getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Instant periodEnd) { this.periodEnd = periodEnd; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
