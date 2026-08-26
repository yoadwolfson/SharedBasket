package com.sharedbasket.dto;

import java.io.Serializable;
import java.util.List;

/** Stable error payload used by REST responses and the JSF field-message renderer. */
public class ErrorResponseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private List<FieldErrorDTO> fieldErrors;
    
    // Constructors
    public ErrorResponseDTO() {}
    
    public ErrorResponseDTO(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
    
    // Getters & Setters
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<FieldErrorDTO> getFieldErrors() {
        return fieldErrors;
    }
    
    public void setFieldErrors(List<FieldErrorDTO> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
    
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }
    
    @Override
    public String toString() {
        return "ErrorResponseDTO{" +
                "timestamp='" + timestamp + '\'' +
                ", status=" + status +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", fieldErrors=" + fieldErrors +
                '}';
    }
}
