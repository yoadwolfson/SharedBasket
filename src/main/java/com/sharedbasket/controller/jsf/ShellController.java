package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.AlertDTO;
import com.sharedbasket.dto.GroupDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.util.JsfRestClientSupport;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellController.class);
    
    private transient RestTemplate restTemplate;
    
    private List<AlertDTO> applicationAlerts;
    private UserDTO currentUser;
    private Long currentGroupId;
    private String currentGroupName;
    
    @PostConstruct
    public void init() {
        this.applicationAlerts = new ArrayList<>();
        this.restTemplate = JsfRestClientSupport.resolveBean(RestTemplate.class);
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
            restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/auth") + "/logout",
                HttpMethod.POST,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                Void.class
            );
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

    // ==================== Getters & Setters ====================
    
    public List<AlertDTO> getApplicationAlerts() {
        return applicationAlerts;
    }
    
    public void setApplicationAlerts(List<AlertDTO> applicationAlerts) {
        this.applicationAlerts = applicationAlerts;
    }

    public void syncCurrentUserFromAuthService() {
        if (restTemplate == null) {
            restTemplate = JsfRestClientSupport.resolveBean(RestTemplate.class);
        }

        try {
            currentUser = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/auth") + "/me",
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                UserDTO.class
            ).getBody();
        } catch (HttpStatusCodeException e) {
            currentUser = null;
        } catch (Exception e) {
            LOGGER.warn("syncCurrentUser failed: {}", e.getMessage(), e);
            currentUser = null;
        }
    }

    public UserDTO getCurrentUser() {
        syncCurrentUserFromAuthService();
        return currentUser;
    }

    /**
     * Resolves the group used for dashboard/reports/recommendations scoping.
     * Only the first accepted group is used - there is no multi-group switcher yet.
     */
    public Long getCurrentGroupId() {
        if (currentGroupId == null) {
            resolveCurrentGroup();
        }
        return currentGroupId;
    }

    public String getCurrentGroupName() {
        if (currentGroupId == null) {
            resolveCurrentGroup();
        }
        return currentGroupName;
    }

    private void resolveCurrentGroup() {
        if (restTemplate == null) {
            restTemplate = JsfRestClientSupport.resolveBean(RestTemplate.class);
        }
        try {
            GroupDTO[] groups = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/groups"),
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                GroupDTO[].class
            ).getBody();
            if (groups != null && groups.length > 0) {
                currentGroupId = groups[0].getId();
                currentGroupName = groups[0].getName();
            }
        } catch (Exception e) {
            LOGGER.warn("resolveCurrentGroup failed: {}", e.getMessage(), e);
        }
    }

    public boolean isAuthenticated() {
        syncCurrentUserFromAuthService();
        return currentUser != null;
    }
    
    public void setCurrentUser(UserDTO currentUser) {
        this.currentUser = currentUser;
    }
}