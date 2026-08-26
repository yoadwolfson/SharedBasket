package com.sharedbasket.controller.rest;

import com.sharedbasket.domain.User;
import com.sharedbasket.dto.LoginRequestDTO;
import com.sharedbasket.dto.RegisterRequestDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.exception.ConflictOperationException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.impl.CookieAuthenticationService;
import com.sharedbasket.util.UserViewMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieAuthenticationService authenticationService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, CookieAuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    /**
     * Creates an enabled user after checking that the email and username are available.
     *
     * @param request validated registration details
     * @return the public user view without password data
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new ConflictOperationException("A user with that email already exists");
        }
        if (userRepository.findByUsername(request.getUsername().trim()).isPresent()) {
            throw new ConflictOperationException("That username is unavailable");
        }

        User user = new User();
        user.setEmail(request.getEmail().trim());
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName() == null ? null : request.getFirstName().trim());
        user.setLastName(request.getLastName() == null ? null : request.getLastName().trim());
        user.setEnabled(Boolean.TRUE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(UserViewMapper.toUserDto(user, "MEMBER"));
    }

    /**
     * Verifies credentials and binds the authenticated user to the current HTTP session.
     *
     * @param request validated login credentials
     * @param servletRequest current request whose session receives the authenticated identity
     * @return the public user view
     */
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest servletRequest) {
        User user = authenticationService.authenticate(request.getEmail().trim(), request.getPassword());
        if (user == null) {
            throw new ForbiddenOperationException("Invalid email or password");
        }

        authenticationService.attachSession(servletRequest, user);
        return ResponseEntity.ok(UserViewMapper.toUserDto(user, "MEMBER"));
    }

    /**
     * Returns the current session user.
     *
     * @return the authenticated public user view
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> currentUser() {
        UserDTO currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenOperationException("Authentication required");
        }
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Ends the current session and returns no body on success.
     *
     * @param servletRequest current request containing the session to invalidate
     * @return an empty no-content response
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        authenticationService.logout(servletRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}