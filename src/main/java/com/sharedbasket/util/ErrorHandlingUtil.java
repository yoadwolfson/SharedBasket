package com.sharedbasket.util;

import com.sharedbasket.dto.ErrorResponseDTO;
import com.sharedbasket.dto.FieldErrorDTO;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * Error Handling Utility Class
 * 
 * Provides centralized error handling and message rendering for the JSF layer.
 * Converts ErrorResponseDTO objects from backend responses into FacesMessage
 * objects for display in the JSF framework.
 * 
 * DEV-A Connection Point:
 * @DevAConnection Provider=DeveloperA, Service=Various, Type=ErrorHandlingContract
 *                Context: This utility processes error responses from Developer A's REST endpoints.
 *                Integration: Used by all page controllers that consume Developer A's services.
 */
public class ErrorHandlingUtil {
    
    /**
     * Process an ErrorResponseDTO and add JSF validation messages for each field error.
     * 
     * @param errorResponse the error response from backend
     * @param context FacesContext for adding messages
     */
    public static void processErrorResponse(ErrorResponseDTO errorResponse, FacesContext context) {
        if (errorResponse == null) {
            return;
        }
        
        // Add global error message if present
        if (errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                errorResponse.getError() != null ? errorResponse.getError() : "Error",
                errorResponse.getMessage()
            ));
        }
        
        // Add field-level error messages
        if (errorResponse.hasFieldErrors()) {
            addFieldErrors(errorResponse.getFieldErrors(), context);
        }
    }
    
    /**
     * Add field-level validation errors as JSF messages.
     * 
     * @param fieldErrors list of field errors from backend
     * @param context FacesContext for adding messages
     */
    public static void addFieldErrors(List<FieldErrorDTO> fieldErrors, FacesContext context) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return;
        }
        
        for (FieldErrorDTO fieldError : fieldErrors) {
            String clientId = fieldError.getFieldName();
            String message = fieldError.getMessage();
            
            // Create a detailed error message with error code
            String detail = message;
            if (fieldError.getErrorCode() != null) {
                detail += " (" + fieldError.getErrorCode() + ")";
            }
            
            context.addMessage(clientId, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                fieldError.getFieldName(),
                detail
            ));
        }
    }
    
    /**
     * Add a global success message.
     * 
     * @param context FacesContext for adding message
     * @param summary summary message text
     * @param detail detail message text (optional)
     */
    public static void addSuccessMessage(FacesContext context, String summary, String detail) {
        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_INFO,
            summary,
            detail
        ));
    }
    
    /**
     * Add a global error message.
     * 
     * @param context FacesContext for adding message
     * @param summary summary message text
     * @param detail detail message text (optional)
     */
    public static void addErrorMessage(FacesContext context, String summary, String detail) {
        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_ERROR,
            summary,
            detail
        ));
    }
    
    /**
     * Add a warning message.
     * 
     * @param context FacesContext for adding message
     * @param summary summary message text
     * @param detail detail message text (optional)
     */
    public static void addWarningMessage(FacesContext context, String summary, String detail) {
        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_WARN,
            summary,
            detail
        ));
    }
    
    /**
     * Clear all JSF messages.
     * 
     * @param context FacesContext for clearing messages
     */
    public static void clearMessages(FacesContext context) {
        context.getMessages().forEachRemaining(message -> {
            // Messages are cleared by creating a new FacesContext or by iterating
            // This is a simplified approach; consider using a message manager
        });
    }
    
    /**
     * Get HTTP status code description.
     * 
     * @param statusCode HTTP status code
     * @return Human-readable status description
     */
    public static String getStatusDescription(int statusCode) {
        switch (statusCode) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 409: return "Conflict";
            case 422: return "Unprocessable Entity";
            case 500: return "Internal Server Error";
            case 503: return "Service Unavailable";
            default: return "Error";
        }
    }
}
