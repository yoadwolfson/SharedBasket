package com.sharedbasket.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharedbasket.domain.ItemUnit;
import com.sharedbasket.domain.ShoppingItemRequestStatus;
import com.sharedbasket.domain.ShoppingItemRequestType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class ShoppingItemRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long groupId;
    private Long requestedByUserId;
    private String requestedByUserName;
    private Long itemId;
    private ShoppingItemRequestType type;
    private ShoppingItemRequestStatus status;
    private Long itemVersion;
    private String name;
    private BigDecimal quantity;
    private ItemUnit unit;
    private BigDecimal estimatedPrice;
    private Long categoryId;
    private String categoryName;
    private String categoryEmoji;
    private String categoryColor;
    private String notes;
    private Instant createdAt;
    private Instant decidedAt;
    private Long decidedByUserId;
    private String decisionNote;

    public ShoppingItemRequestDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(Long requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public String getRequestedByUserName() { return requestedByUserName; }
    public void setRequestedByUserName(String requestedByUserName) { this.requestedByUserName = requestedByUserName; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public ShoppingItemRequestType getType() { return type; }
    public void setType(ShoppingItemRequestType type) { this.type = type; }
    public ShoppingItemRequestStatus getStatus() { return status; }
    public void setStatus(ShoppingItemRequestStatus status) { this.status = status; }
    public Long getItemVersion() { return itemVersion; }
    public void setItemVersion(Long itemVersion) { this.itemVersion = itemVersion; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public ItemUnit getUnit() { return unit; }
    public void setUnit(ItemUnit unit) { this.unit = unit; }
    @JsonIgnore
    public String getUnitLabel() { return unit == null ? "" : unit.getShortLabel(); }
    public String getCategoryEmoji() { return categoryEmoji; }
    public void setCategoryEmoji(String categoryEmoji) { this.categoryEmoji = categoryEmoji; }
    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
    public Long getDecidedByUserId() { return decidedByUserId; }
    public void setDecidedByUserId(Long decidedByUserId) { this.decidedByUserId = decidedByUserId; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
