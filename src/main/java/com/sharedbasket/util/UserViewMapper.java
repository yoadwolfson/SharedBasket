package com.sharedbasket.util;

import com.sharedbasket.domain.User;
import com.sharedbasket.dto.UserDTO;

public final class UserViewMapper {

    private UserViewMapper() {
    }

    public static UserDTO toUserDto(User user, String role) {
        UserDTO dto = new UserDTO(
            user.getId(),
            user.getEmail(),
            buildDisplayName(user),
            role
        );
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }

    private static String buildDisplayName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
}