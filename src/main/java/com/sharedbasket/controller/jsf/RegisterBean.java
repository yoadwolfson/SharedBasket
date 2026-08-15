package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.RegisterRequestDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.util.JsfRestClientSupport;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "registerBean")
@RequestScoped
public class RegisterBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient RestTemplate restTemplate;

    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    @PostConstruct
    public void init() {
        this.restTemplate = JsfRestClientSupport.resolveBean(RestTemplate.class);
    }

    public String register() {
        if (isBlank(email) || isBlank(username) || isBlank(password)) {
            addGlobalError("Email, username, and password are required.");
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedUsername = username.trim();

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail(normalizedEmail);
        request.setUsername(normalizedUsername);
        request.setPassword(password);
        request.setFirstName(isBlank(firstName) ? null : firstName.trim());
        request.setLastName(isBlank(lastName) ? null : lastName.trim());

        try {
            UserDTO response = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/auth") + "/register",
                HttpMethod.POST,
                new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()),
                UserDTO.class
            ).getBody();

            if (response == null) {
                addGlobalError("Registration failed.");
                return null;
            }
        } catch (HttpStatusCodeException ex) {
            addGlobalError(JsfRestClientSupport.extractErrorMessage(ex));
            return null;
        } catch (Exception ex) {
            addGlobalError("Registration is currently unavailable.");
            return null;
        }

        FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration successful", "You can now sign in.")
        );

        return "/login.xhtml?faces-redirect=true";
    }

    private void addGlobalError(String message) {
        FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", message)
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
