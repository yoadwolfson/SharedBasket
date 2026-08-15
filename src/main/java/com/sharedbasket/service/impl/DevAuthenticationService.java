package com.sharedbasket.service.impl;

import com.sharedbasket.domain.User;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.util.UserViewMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;

import java.time.Instant;

/**
 * Development-only authentication fallback.
 *
 * This allows frontend and feature work to run before the real auth module
 * is integrated by Developer A.
 */
//@Service TODO: Uncomment this when Developer A's auth module is integrated
@Primary
@Profile("dev")
public class DevAuthenticationService implements AuthenticationService {

    private static final String DEV_USERNAME = "devuser";
    private static final String DEV_EMAIL = "dev@sharedbasket.local";

    private final UserRepository userRepository;

    public DevAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO getCurrentUser() {
        User user = userRepository.findByEmail(DEV_EMAIL).orElseGet(this::createDevUser);
        return UserViewMapper.toUserDto(user, "ADMIN");
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void logout() {
        // no-op for development fallback
    }

    @Override
    public void invalidateUserSessions(Long userId) {
        // no-op for development fallback
    }

    private User createDevUser() {
        User user = new User();
        user.setUsername(DEV_USERNAME);
        user.setEmail(DEV_EMAIL);
        user.setPasswordHash("dev-only-not-for-production");
        user.setFirstName("Dev");
        user.setLastName("User");
        user.setEnabled(Boolean.TRUE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

}