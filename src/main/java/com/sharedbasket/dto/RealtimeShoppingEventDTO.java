package com.sharedbasket.dto;

import java.io.Serializable;

public class RealtimeShoppingEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private long version;
    private RealtimeEventType type;
    private String itemName;
    private int quantity;
    private double estimatedPrice;
    private String message;

    public RealtimeShoppingEventDTO() {
    }

    public RealtimeShoppingEventDTO(
        String eventId,
        long version,
        RealtimeEventType type,
        String itemName,
        int quantity,
        double estimatedPrice,
        String message
    ) {
        this.eventId = eventId;
        this.version = version;
        this.type = type;
        this.itemName = itemName;
        this.quantity = quantity;
        this.estimatedPrice = estimatedPrice;
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public RealtimeEventType getType() {
        return type;
    }

    public void setType(RealtimeEventType type) {
        this.type = type;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
