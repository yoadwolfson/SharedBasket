package com.sharedbasket.dto;

import java.io.Serializable;

public class RecommendationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String itemName;
    private String category;
    private int frequency;
    public RecommendationDTO() {}
    public RecommendationDTO(String itemName, String category, int frequency) {
        this.itemName = itemName;
        this.category = category;
        this.frequency = frequency; 
    }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
}
