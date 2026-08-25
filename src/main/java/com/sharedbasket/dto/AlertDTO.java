package com.sharedbasket.dto;

import java.io.Serializable;


public class AlertDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String type;
    private String message;
    
    public AlertDTO() {}
    
    public AlertDTO(String type, String message) {
        this.type = type;
        this.message = message;
    }
    
    // Getters & Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "AlertDTO{" +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
