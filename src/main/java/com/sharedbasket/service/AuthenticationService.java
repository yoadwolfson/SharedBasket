package com.sharedbasket.service;

import com.sharedbasket.dto.UserDTO;

/**
 * Authentication Service Interface
 * 
 * This interface defines the contract for authentication and session management.
 * The implementation is provided by Developer A's authentication module.
 * 
 * DEV-A Connection Point:
 * @DevAConnection Provider=DeveloperA, Service=AuthenticationService, Type=ServiceContract
 *                Context: This interface must be implemented by Developer A's authentication service.
 *                         Developer B consumes this service for shell-level session management.
 *                Integration: Implementation must be injected into ShellController and other
 *                             components that need authentication.
 *                Expected Operations:
 *                  - getCurrentUser(): UserDTO - returns authenticated user from session
 *                  - logout(): void - clears session and invalidates authentication
 *                  - isAuthenticated(): boolean - checks if user has valid session
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
