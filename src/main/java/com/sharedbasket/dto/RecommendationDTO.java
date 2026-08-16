package com.sharedbasket.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class RecommendationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String recommendationId;
    private String itemName;
    private String category;
    private String categoryName;
    private String rationale;
    private int frequency;
    private BigDecimal estimatedUnitPrice;
    private BigDecimal estimatedTotalContribution;
    private Instant sourceWindowStart;
    private Instant sourceWindowEnd;

    public RecommendationDTO() {}
    public RecommendationDTO(String itemName, String category, int frequency) {
        this.itemName = itemName;
        this.category = category;
        this.categoryName = category;
        this.frequency = frequency; 
    }
    public String getRecommendationId() { return recommendationId; }
    public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    public BigDecimal getEstimatedUnitPrice() { return estimatedUnitPrice; }
    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) { this.estimatedUnitPrice = estimatedUnitPrice; }
    public BigDecimal getEstimatedTotalContribution() { return estimatedTotalContribution; }
    public void setEstimatedTotalContribution(BigDecimal estimatedTotalContribution) { this.estimatedTotalContribution = estimatedTotalContribution; }
    public Instant getSourceWindowStart() { return sourceWindowStart; }
    public void setSourceWindowStart(Instant sourceWindowStart) { this.sourceWindowStart = sourceWindowStart; }
    public Instant getSourceWindowEnd() { return sourceWindowEnd; }
    public void setSourceWindowEnd(Instant sourceWindowEnd) { this.sourceWindowEnd = sourceWindowEnd; }
}
