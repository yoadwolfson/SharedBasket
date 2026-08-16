package com.sharedbasket.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SpendingByCategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String categoryName;
    private BigDecimal amount;
    private BigDecimal quantity;
    private double percentageOfTotal;

    public SpendingByCategoryDTO() {}

    public SpendingByCategoryDTO(String categoryName, BigDecimal amount, BigDecimal quantity, double percentageOfTotal) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.quantity = quantity;
        this.percentageOfTotal = percentageOfTotal;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public double getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
}
