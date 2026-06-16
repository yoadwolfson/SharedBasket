package com.sharedbasket.dto;

import java.io.Serializable;

/**
 * Alert Data Transfer Object (AlertDTO)
 * 
 * Represents an alert or notification message for display to the user.
 * Used by the shell's alert rendering system to show messages of various types.
 * 
 * Alert Types:
 *  - "success": Operation completed successfully
 *  - "error": An error occurred
 *  - "warning": Warning message
 *  - "info": Informational message
 */
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
