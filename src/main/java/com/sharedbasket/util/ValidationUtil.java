package com.sharedbasket.util;

import javax.faces.context.FacesContext;

/**
 * Validation Utility Class
 * 
 * Provides common validation helper methods for JSF forms and input validation.
 * Supports field validation, cross-field validation, and error state management.
 */
public class ValidationUtil {
    
    /**
     * Check if a field has validation errors.
     * 
     * @param fieldName the JSF component/field name
     * @param context FacesContext
     * @return true if field has validation errors, false otherwise
     */
    public static boolean hasFieldError(String fieldName, FacesContext context) {
        return context.getMessageList(fieldName).stream()
            .anyMatch(msg -> msg.getSeverity().getOrdinal() >= 
                           javax.faces.application.FacesMessage.SEVERITY_ERROR.getOrdinal());
    }
    
    /**
     * Get validation error message for a field.
     * 
     * @param fieldName the JSF component/field name
     * @param context FacesContext
     * @return error message or empty string if no error
     */
    public static String getFieldError(String fieldName, FacesContext context) {
        return context.getMessageList(fieldName).stream()
            .filter(msg -> msg.getSeverity().getOrdinal() >= 
                          javax.faces.application.FacesMessage.SEVERITY_ERROR.getOrdinal())
            .map(msg -> msg.getDetail() != null ? msg.getDetail() : msg.getSummary())
            .findFirst()
            .orElse("");
    }
    
    /**
     * Check if any field has validation errors.
     * 
     * @param context FacesContext
     * @return true if any field has errors, false otherwise
     */
    public static boolean hasValidationErrors(FacesContext context) {
        return context.getMessageList().stream()
            .anyMatch(msg -> msg.getSeverity().getOrdinal() >= 
                           javax.faces.application.FacesMessage.SEVERITY_ERROR.getOrdinal());
    }
    
    /**
     * Check if email format is valid.
     * 
     * @param email email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    /**
     * Check if a string is not empty.
     * 
     * @param value string to check
     * @return true if string is not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Check if a string meets minimum length requirement.
     * 
     * @param value string to check
     * @param minLength minimum length
     * @return true if length is sufficient, false otherwise
     */
    public static boolean isMinLength(String value, int minLength) {
        return value != null && value.length() >= minLength;
    }
    
    /**
     * Check if a string meets maximum length requirement.
     * 
     * @param value string to check
     * @param maxLength maximum length
     * @return true if length is within limit, false otherwise
     */
    public static boolean isMaxLength(String value, int maxLength) {
        return value == null || value.length() <= maxLength;
    }
}
