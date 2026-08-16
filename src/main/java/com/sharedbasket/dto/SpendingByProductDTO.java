package com.sharedbasket.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SpendingByProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productName;
    private BigDecimal amount;
    private BigDecimal quantity;
    private double percentageOfTotal;

    public SpendingByProductDTO() {}

    public SpendingByProductDTO(String productName, BigDecimal amount, BigDecimal quantity, double percentageOfTotal) {
        this.productName = productName;
        this.amount = amount;
        this.quantity = quantity;
        this.percentageOfTotal = percentageOfTotal;
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public double getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
}
