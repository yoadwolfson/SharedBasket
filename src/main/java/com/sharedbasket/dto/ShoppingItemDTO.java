package com.sharedbasket.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class ShoppingItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long groupId;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal estimatedPrice;
    private Boolean purchased;
    private Instant purchasedAt;
    private Long purchasedByUserId;
    private String purchasedByUserName;
    private Long categoryId;
    private String categoryName;
    private String notes;
    private Long createdByUserId;
    private String createdByUserName;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public ShoppingItemDTO() {}

    public ShoppingItemDTO(String name, int quantity, double estimatedPrice) {
        this.name = name;
        this.quantity = BigDecimal.valueOf(quantity);
        this.estimatedPrice = BigDecimal.valueOf(estimatedPrice);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public void setQuantity(int quantity) { this.quantity = BigDecimal.valueOf(quantity); }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = BigDecimal.valueOf(estimatedPrice); }
    public Boolean getPurchased() { return purchased; }
    public void setPurchased(Boolean purchased) { this.purchased = purchased; }
    public Instant getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(Instant purchasedAt) { this.purchasedAt = purchasedAt; }
    public Long getPurchasedByUserId() { return purchasedByUserId; }
    public void setPurchasedByUserId(Long purchasedByUserId) { this.purchasedByUserId = purchasedByUserId; }
    public String getPurchasedByUserName() { return purchasedByUserName; }
    public void setPurchasedByUserName(String purchasedByUserName) { this.purchasedByUserName = purchasedByUserName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public String getCreatedByUserName() { return createdByUserName; }
    public void setCreatedByUserName(String createdByUserName) { this.createdByUserName = createdByUserName; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
