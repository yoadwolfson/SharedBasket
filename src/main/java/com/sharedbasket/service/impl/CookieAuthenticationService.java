package com.sharedbasket.service.impl;

import com.sharedbasket.domain.User;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Objects;

@Service
@Primary
public class CookieAuthenticationService implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CookieAuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getCurrentUser() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("authenticatedUserId");
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(Long.valueOf(userId.toString())).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            return null;
        }
        return mapToDto(user);
    }

    @Override
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    @Override
    public void logout() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public void invalidateUserSessions(Long userId) {
        if (userId == null) {
            return;
        }
        // TODO: Placeholder for future admin/session revocation logic.
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    public void authenticateUser(UserDTO user) {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("authenticatedUserId", user.getId());
            session.setAttribute("authenticatedUserEmail", user.getEmail());
        }
    }

    public void attachSession(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute("authenticatedUserId", user.getId());
        session.setAttribute("authenticatedUserEmail", user.getEmail());
    }

    private HttpServletRequest getCurrentRequest() {
        // JSF request context
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            try {
                return (HttpServletRequest) fc.getExternalContext().getRequest();
            } catch (Exception e) {
                // Fall through to Spring context
            }
        }
        // Spring/REST request context
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes == null ? null : attributes.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private UserDTO mapToDto(User user) {
        return new UserDTO(
            user.getId(),
            user.getEmail(),
            buildDisplayName(user),
            "MEMBER"
        );
    }

    private String buildDisplayName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
}
