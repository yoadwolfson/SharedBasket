package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.ProfileUpdateRequestDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.util.JsfRestClientSupport;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ManagedBean(name = "profileBean")
@ViewScoped
public class ProfileBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String errorMessage;

    private transient RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = JsfRestClientSupport.resolveBean(RestTemplate.class);
        loadProfile();
    }

    public String save() {
        if (isBlank(username) || isBlank(firstName) || isBlank(lastName)) {
            errorMessage = "First name, last name, and username are required";
            return null;
        }

        ProfileUpdateRequestDTO request = new ProfileUpdateRequestDTO();
        request.setUsername(username.trim());
        request.setFirstName(firstName.trim());
        request.setLastName(lastName.trim());

        try {
            UserDTO response = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/profile"),
                HttpMethod.PUT,
                new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()),
                UserDTO.class
            ).getBody();

            if (response == null) {
                errorMessage = "Unable to update profile";
                return null;
            }

            this.email = response.getEmail();
            this.username = response.getUsername();
            this.firstName = response.getFirstName();
            this.lastName = response.getLastName();
            errorMessage = null;
            return null;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                errorMessage = "Authentication required";
                return "/login.xhtml?faces-redirect=true";
            }
            errorMessage = JsfRestClientSupport.extractErrorMessage(ex);
            return null;
        }
    }

    private void loadProfile() {
        try {
            UserDTO response = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/profile"),
                HttpMethod.GET,
                new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()),
                UserDTO.class
            ).getBody();

            if (response == null) {
                this.errorMessage = "Unable to load profile";
                return;
            }

            this.email = response.getEmail();
            this.username = response.getUsername();
            this.firstName = response.getFirstName();
            this.lastName = response.getLastName();
            this.errorMessage = null;
        } catch (Exception ex) {
            this.errorMessage = "Unable to load profile";
        }
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}