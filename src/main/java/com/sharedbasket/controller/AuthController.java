package com.sharedbasket.controller;

import com.sharedbasket.domain.User;
import com.sharedbasket.dto.LoginRequestDTO;
import com.sharedbasket.dto.RegisterRequestDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ConflictOperationException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.AuthenticationService;
import com.sharedbasket.service.impl.CookieAuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Instant;
import java.util.Objects;

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

        return ResponseEntity.ok(mapToDto(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest servletRequest) {
        User user = authenticationService.authenticate(request.getEmail().trim(), request.getPassword());
        if (user == null) {
            throw new ForbiddenOperationException("Invalid email or password");
        }

        authenticationService.attachSession(servletRequest, user);
        return ResponseEntity.ok(mapToDto(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> currentUser() {
        UserDTO currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenOperationException("Authentication required");
        }
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        authenticationService.logout();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
