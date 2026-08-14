package com.sharedbasket.service.impl;

import com.sharedbasket.domain.User;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.AuthenticationService;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.sharedbasket.util.UserViewMapper;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;

@Service
@Primary
public class CookieAuthenticationService implements AuthenticationService {

    private static final String AUTHENTICATED_USER_ID = "authenticatedUserId";
    private static final String AUTHENTICATED_USER_EMAIL = "authenticatedUserEmail";
    private static final String MEMBER_ROLE = "MEMBER";
    private static final String MEMBER_AUTHORITY = "ROLE_MEMBER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CookieAuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getCurrentUser() {
        User user = null;
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            user = resolveFromSession(request.getSession(false));
        }
        if (user == null) {
            user = resolveFromSecurityContext();
        }

        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            return null;
        }
        return UserViewMapper.toUserDto(user, MEMBER_ROLE);
    }

    @Override
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();

        HttpServletRequest request = getCurrentRequest();
        logout(request);
    }

    public void logout(HttpServletRequest request) {
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
        if (user == null || user.getId() == null) {
            return;
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return;
        }

        User persistedUser = userRepository.findById(user.getId()).orElse(null);
        if (persistedUser != null) {
            attachSession(request, persistedUser);
        }
    }

    public void attachSession(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(AUTHENTICATED_USER_ID, user.getId());
        session.setAttribute(AUTHENTICATED_USER_EMAIL, user.getEmail());
        bindAuthentication(request, user);
    }

    public void attachCurrentSession(User user) {
        HttpServletRequest request = getCurrentRequest();
        if (request != null && user != null) {
            attachSession(request, user);
        }
    }

    private User resolveFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object details = authentication.getDetails();
        if (details != null) {
            try {
                return userRepository.findById(Long.valueOf(details.toString())).orElse(null);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            String principalValue = ((String) principal).trim();
            if (!principalValue.isEmpty() && !"anonymousUser".equalsIgnoreCase(principalValue)) {
                return userRepository.findByEmail(principalValue.toLowerCase()).orElse(null);
            }
        }

        return null;
    }

    private User resolveFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute(AUTHENTICATED_USER_ID);
        if (userId == null) {
            return null;
        }

        try {
            return userRepository.findById(Long.valueOf(userId.toString())).orElse(null);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void bindAuthentication(HttpServletRequest request, User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority(MEMBER_AUTHORITY))
        );
        ((UsernamePasswordAuthenticationToken) authentication).setDetails(user.getId());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
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

}
