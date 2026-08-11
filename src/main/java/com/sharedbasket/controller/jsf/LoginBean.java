package com.sharedbasket.controller.jsf;

import com.sharedbasket.controller.ShellController;
import com.sharedbasket.domain.User;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.service.impl.CookieAuthenticationService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.Serializable;

@ManagedBean(name = "loginBean")
@RequestScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient CookieAuthenticationService cookieAuthService;

    private String email;
    private String password;

    @PostConstruct
    public void init() {
        this.cookieAuthService = resolveSpringBean(CookieAuthenticationService.class);
    }

    public String login() {
        if (isBlank(email) || isBlank(password)) {
            addError("Email and password are required.");
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = cookieAuthService.authenticate(normalizedEmail, password);

        if (user == null) {
            addError("Invalid email or password.");
            return null;
        }
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName  = user.getLastName()  == null ? "" : user.getLastName().trim();
        String fullName  = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) fullName = user.getUsername();

        cookieAuthService.authenticateUser(new UserDTO(user.getId(), user.getEmail(), fullName, "MEMBER"));
        syncShellUser();

        return "/dashboard.xhtml?faces-redirect=true";
    }

    private void syncShellUser() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return;
        }

        ShellController shellController = facesContext.getApplication().evaluateExpressionGet(
            facesContext,
            "#{shellController}",
            ShellController.class
        );

        if (shellController != null) {
            shellController.syncCurrentUserFromAuthService();
        }
    }

    private void addError(String message) {
        FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed", message)
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
