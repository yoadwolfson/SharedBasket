package com.sharedbasket.controller.jsf;

import com.sharedbasket.dto.ProfileUpdateRequestDTO;
import com.sharedbasket.dto.UserDTO;
import org.springframework.web.client.RestTemplate;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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

    public ProfileBean() {
        loadProfile();
    }

    public String save() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        RestTemplate restTemplate = new RestTemplate();
        ProfileUpdateRequestDTO request = new ProfileUpdateRequestDTO();
        request.setUsername(username);
        request.setFirstName(firstName);
        request.setLastName(lastName);

        try {
            UserDTO response = restTemplate.postForObject(context.getRequestScheme() + "://" + context.getRequestServerName() + ":" + context.getRequestServerPort() + context.getRequestContextPath() + "/api/profile", request, UserDTO.class);
            if (response != null) {
                errorMessage = null;
                return null;
            }
        } catch (Exception ex) {
            errorMessage = "Unable to update profile";
            return null;
        }

        errorMessage = "Unable to update profile";
        return null;
    }

    private void loadProfile() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        RestTemplate restTemplate = new RestTemplate();
        try {
            UserDTO response = restTemplate.getForObject(context.getRequestScheme() + "://" + context.getRequestServerName() + ":" + context.getRequestServerPort() + context.getRequestContextPath() + "/api/profile", UserDTO.class);
            if (response != null) {
                this.email = response.getEmail();
                this.username = response.getEmail() != null ? response.getEmail().split("@")[0] : null;
                this.firstName = null;
                this.lastName = null;
            }
        } catch (Exception ex) {
            this.errorMessage = "Unable to load profile";
        }
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