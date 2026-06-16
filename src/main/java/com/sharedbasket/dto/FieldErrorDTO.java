package com.sharedbasket.dto;

import java.io.Serializable;

/**
 * Field Error Data Transfer Object (FieldErrorDTO)
 * 
 * Represents a validation or constraint error for a specific form field.
 * Provides detailed error information for frontend rendering and user feedback.
 * 
 * DEV-A Connection Point:
 * @DevAConnection Provider=DeveloperA, Service=Various, Type=FieldErrorContract
 *                Context: Field-level errors from Developer A's REST endpoints must conform
 *                         to this structure for consistent validation feedback in the JSF layer.
 */
public class FieldErrorDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String fieldName;
    private String message;
    private String errorCode;
    private Object rejectedValue;
    
    // Constructors
    public FieldErrorDTO() {}
    
    public FieldErrorDTO(String fieldName, String message, String errorCode) {
        this.fieldName = fieldName;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public FieldErrorDTO(String fieldName, String message, String errorCode, Object rejectedValue) {
        this.fieldName = fieldName;
        this.message = message;
        this.errorCode = errorCode;
        this.rejectedValue = rejectedValue;
    }
    
    // Getters & Setters
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public Object getRejectedValue() {
        return rejectedValue;
    }
    
    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }
    
    @Override
    public String toString() {
        return "FieldErrorDTO{" +
                "fieldName='" + fieldName + '\'' +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", rejectedValue=" + rejectedValue +
                '}';
    }
}
