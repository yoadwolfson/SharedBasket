package com.sharedbasket.dto;

import com.sharedbasket.domain.ItemUnit;

import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateShoppingItemRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private BigDecimal quantity;
    private ItemUnit unit;
    private BigDecimal estimatedPrice;
    private Long categoryId;
    private String notes;
    private Boolean purchased;
    private Long version;

    public UpdateShoppingItemRequestDTO() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public ItemUnit getUnit() { return unit; }
    public void setUnit(ItemUnit unit) { this.unit = unit; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Boolean getPurchased() { return purchased; }
    public void setPurchased(Boolean purchased) { this.purchased = purchased; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}