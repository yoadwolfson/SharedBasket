package com.sharedbasket.controller.jsf;

import com.sharedbasket.controller.jsf.ShellController;
import com.sharedbasket.dto.LoginRequestDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.util.JsfRestClientSupport;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "loginBean")
@RequestScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient RestTemplate restTemplate;

    private String email;
    private String password;

    @PostConstruct
    public void init() {
        this.restTemplate = JsfRestClientSupport.resolveBean(RestTemplate.class);
    }

    public String login() {
        if (isBlank(email) || isBlank(password)) {
            addError("Email and password are required.");
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(normalizedEmail);
        loginRequest.setPassword(password);

        try {
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                JsfRestClientSupport.apiBaseUrl("/api/auth") + "/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest, JsfRestClientSupport.buildJsonHeaders()),
                UserDTO.class
            );

            if (response.getBody() == null) {
                addError("Invalid email or password.");
                return null;
            }

            forwardSessionCookie(response.getHeaders());
        } catch (HttpStatusCodeException ex) {
            addError("Invalid email or password.");
            return null;
        } catch (Exception ex) {
            addError("Login is currently unavailable.");
            return null;
        }

        syncShellUser();

        return "/dashboard.xhtml?faces-redirect=true";
    }

    // Forwards the login response's session cookie so the browser stays authenticated after this self HTTP call.
    private void forwardSessionCookie(HttpHeaders responseHeaders) {
        List<String> setCookies = responseHeaders.get(HttpHeaders.SET_COOKIE);
        if (setCookies == null) {
            return;
        }

        Object servletResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse();
        if (!(servletResponse instanceof HttpServletResponse)) {
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        for (String setCookie : setCookies) {
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, setCookie);
        }
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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
