package com.sharedbasket.controller;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.service.AuthenticationService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Shell Controller for Shared Basket Application
 * 
 * Manages global application state, navigation, and alert display.
 * This controller is session-scoped and responsible for:
 * - Coordinating authentication state with Developer A's authentication service
 * - Managing global alerts and notifications
 * - Providing common navigation actions
 * - Maintaining shell-level state
 * 
 * DEV-A Connection Points:
 * @DevAConnection Provider=DeveloperA, Service=AuthenticationService, Type=Dependency
 *                Context: Shell depends on Developer A's AuthenticationService for session management.
 *                Integration: Injected AuthenticationService from Developer A's module.
 */
@ManagedBean(name = "shellController")
@SessionScoped
public class ShellController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private AuthenticationService authenticationService;
    
    private List<AlertDTO> applicationAlerts;
    private UserDTO currentUser;
    
    @PostConstruct
    public void init() {
        this.applicationAlerts = new ArrayList<>();
        this.authenticationService = resolveSpringBean(AuthenticationService.class);
        initializeCurrentUser();
    }
    
    /**
     * Initialize current user from Developer A's session.
     * 
     * DEV-A Connection Point:
     * @DevAConnection Provider=DeveloperA, Service=AuthenticationService, Type=UserRetrieval
     *                Expected: getCurrentUser() returns UserDTO with id, email, fullName, role properties.
     */
    private void initializeCurrentUser() {
        try {
            syncCurrentUserFromAuthService();
            if (currentUser == null) {
                redirectToLogin();
            }
        } catch (Exception e) {
            addErrorAlert("Failed to load user information. Please refresh the page.");
        }
    }
    
    /**
     * Add an application-level alert message.
     * 
     * @param message Alert message to display
     */
    public void addSuccessAlert(String message) {
        applicationAlerts.add(new AlertDTO("success", message));
    }
    
    public void addErrorAlert(String message) {
        applicationAlerts.add(new AlertDTO("error", message));
    }
    
    public void addWarningAlert(String message) {
        applicationAlerts.add(new AlertDTO("warning", message));
    }
    
    public void addInfoAlert(String message) {
        applicationAlerts.add(new AlertDTO("info", message));
    }
    
    /**
     * Clear all application alerts.
     */
    public void clearAlerts() {
        applicationAlerts.clear();
    }
    
    /**
     * Navigate to user profile.
     */
    public String navigateToProfile() {
        return "/profile.xhtml?faces-redirect=true";
    }
    
    /**
     * Logout user.
     * 
     * DEV-A Connection Point:
     * @DevAConnection Provider=DeveloperA, Service=AuthenticationService, Type=LogoutOperation
     *                Context: Delegates logout to Developer A's authentication service.
     *                Expected: logout() clears session and returns authentication status.
     */
    public String logout() {
        try {
            authenticationService.logout();
            currentUser = null;
            clearAlerts();
            return "/login.xhtml?faces-redirect=true";
        } catch (Exception e) {
            addErrorAlert("Error during logout: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Redirect to login page when user is not authenticated.
     */
    private void redirectToLogin() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return;
        }

        String viewId = fc.getViewRoot() != null ? fc.getViewRoot().getViewId() : "";
        if ("/login.xhtml".equals(viewId) || "/register.xhtml".equals(viewId)) {
            return;
        }

        try {
            fc.getExternalContext().redirect(
                fc.getExternalContext().getRequestContextPath() + "/login.xhtml"
            );
        } catch (Exception e) {
            // Log error but continue
        }
    }

    private <T> T resolveSpringBean(Class<T> beanType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            throw new IllegalStateException("FacesContext is not available");
        }

        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        WebApplicationContext applicationContext =
            WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

        return applicationContext.getBean(beanType);
    }
    
    // ==================== Getters & Setters ====================
    
    public List<AlertDTO> getApplicationAlerts() {
        return applicationAlerts;
    }
    
    public void setApplicationAlerts(List<AlertDTO> applicationAlerts) {
        this.applicationAlerts = applicationAlerts;
    }

    public void syncCurrentUserFromAuthService() {
        if (authenticationService == null) {
            authenticationService = resolveSpringBean(AuthenticationService.class);
        }

        try {
            currentUser = authenticationService.getCurrentUser();
        } catch (Exception e) {
            currentUser = null;
        }
    }

    public UserDTO getCurrentUser() {
        syncCurrentUserFromAuthService();
        return currentUser;
    }

    public boolean isAuthenticated() {
        syncCurrentUserFromAuthService();
        return currentUser != null;
    }
    
    public void setCurrentUser(UserDTO currentUser) {
        this.currentUser = currentUser;
    }
}
