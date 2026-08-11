package com.sharedbasket.controller.jsf;

import com.sharedbasket.domain.User;
import com.sharedbasket.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.time.Instant;

@ManagedBean(name = "registerBean")
@RequestScoped
public class RegisterBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient UserRepository userRepository;
    private transient PasswordEncoder passwordEncoder;

    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    @PostConstruct
    public void init() {
        this.userRepository = resolveSpringBean(UserRepository.class);
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String register() {
        if (isBlank(email) || isBlank(username) || isBlank(password)) {
            addGlobalError("Email, username, and password are required.");
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedUsername = username.trim();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            addGlobalError("This email is already registered.");
            return null;
        }

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            addGlobalError("This username is already taken.");
            return null;
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(isBlank(firstName) ? null : firstName.trim());
        user.setLastName(isBlank(lastName) ? null : lastName.trim());
        user.setEnabled(Boolean.TRUE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

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