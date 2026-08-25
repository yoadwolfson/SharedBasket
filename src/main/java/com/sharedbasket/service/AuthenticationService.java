package com.sharedbasket.service;

import com.sharedbasket.dto.UserDTO;

/**
 * Authentication Service Interface
 * 
 * This interface defines the contract for authentication and session management.
 */
public interface AuthenticationService {
    
    /**
     * Get the currently authenticated user from the session.
     * 
     * @return UserDTO containing user information (id, email, fullName, role)
     *         or null if no user is authenticated
     */
    UserDTO getCurrentUser();
    
    /**
     * Check if a user is currently authenticated.
     * 
     * @return true if user has a valid session, false otherwise
     */
    boolean isAuthenticated();
    
    /**
     * Logout the current user and clear the session.
     */
    void logout();
    
    /**
     * Invalidate all sessions for a user (used for account termination).
     * 
     * @param userId the user ID to invalidate sessions for
     */
    void invalidateUserSessions(Long userId);
}
