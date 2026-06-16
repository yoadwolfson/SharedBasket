package com.sharedbasket.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Error Response Data Transfer Object (ErrorResponseDTO)
 * 
 * Represents error responses from REST endpoints. This DTO standardizes how
 * validation errors, business logic errors, and system errors are communicated
 * from the backend to the JSF frontend.
 * 
 * DEV-A Connection Point:
 * @DevAConnection Provider=DeveloperA, Service=Various, Type=ErrorResponseContract
 *                Context: Error responses from Developer A's REST endpoints (auth, categories, items, reporting)
 *                         must conform to this structure for consistent frontend error handling.
 *                Integration: Developers must ensure backend error responses match this shape.
 *                Expected Shape:
 *                  {
 *                    "timestamp": "2026-06-16T10:30:00Z",
 *                    "status": 400,
 *                    "error": "Bad Request",
 *                    "message": "Validation failed",
 *                    "fieldErrors": [
 *                      { "fieldName": "email", "message": "Email is invalid", "errorCode": "INVALID_EMAIL" },
 *                      { "fieldName": "password", "message": "Password must be at least 8 characters", "errorCode": "INVALID_PASSWORD" }
 *                    ]
 *                  }
 */
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
