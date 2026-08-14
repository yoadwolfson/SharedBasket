package com.sharedbasket.dto;

import java.io.Serializable;

/**
 * User Data Transfer Object (UserDTO)
 * 
 * Represents a user in the system with authentication and profile information.
 * This DTO is shared across the application and serves as the contract for
 * user data between the backend and frontend.
 * 
 * DEV-A Connection Point:
 * @DevAConnection Provider=DeveloperA, Service=AuthenticationService, Type=UserDataContract
 *                Context: This DTO must match the shape of user objects returned by Developer A's 
 *                         AuthenticationService.getCurrentUser() and user-related endpoints.
 *                Expected Fields from Developer A:
 *                  - id: Long (unique user identifier)
 *                  - email: String (user email address)
 *                  - fullName: String (user's full name)
 *                  - role: String ("ADMIN" | "MEMBER") - role within a group context
 */
public class UserDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String username;
    private String firstName;
    private String lastName;
    
    // Constructors
    public UserDTO() {}
    
    public UserDTO(Long id, String email, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
    
    // Getters & Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
            ", username='" + username + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
                '}';
    }
}
