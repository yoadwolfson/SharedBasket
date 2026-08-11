package com.sharedbasket.controller;

import com.sharedbasket.domain.User;
import com.sharedbasket.dto.ProfileUpdateRequestDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.exception.BadRequestException;
import com.sharedbasket.exception.ForbiddenOperationException;
import com.sharedbasket.repo.UserRepository;
import com.sharedbasket.service.impl.CookieAuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Instant;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final CookieAuthenticationService authenticationService;

    public ProfileController(UserRepository userRepository, CookieAuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getProfile() {
        UserDTO currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenOperationException("Authentication required");
        }
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ForbiddenOperationException("Authentication required"));
        return ResponseEntity.ok(mapToDto(user));
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody ProfileUpdateRequestDTO request) {
        UserDTO currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new ForbiddenOperationException("Authentication required");
        }

        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ForbiddenOperationException("Authentication required"));

        if (userRepository.findByUsername(request.getUsername().trim()).filter(existing -> !existing.getId().equals(user.getId())).isPresent()) {
            throw new BadRequestException("A user with that username already exists");
        }

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setUsername(request.getUsername().trim());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(mapToDto(user));
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
