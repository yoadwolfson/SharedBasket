package com.sharedbasket.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PurchaseHistoryEntryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")
        .withZone(ZoneId.systemDefault());
    private Long id;
    private String itemName;
    private String categoryName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal pricePaid;
    private Instant purchasedAt;
    private String purchasedByUserName;

    public PurchaseHistoryEntryDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPricePaid() { return pricePaid; }
    public void setPricePaid(BigDecimal pricePaid) { this.pricePaid = pricePaid; }
    /** UI-friendly rendering of purchasedAt, e.g. "25/08/26 14:30". */
    public String getPurchasedAtDisplay() {
        return purchasedAt == null ? "" : DISPLAY_FORMAT.format(purchasedAt);
    }
    public Instant getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(Instant purchasedAt) { this.purchasedAt = purchasedAt; }
    public String getPurchasedByUserName() { return purchasedByUserName; }
    public void setPurchasedByUserName(String purchasedByUserName) { this.purchasedByUserName = purchasedByUserName; }
}
