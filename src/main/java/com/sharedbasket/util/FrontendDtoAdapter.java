package com.sharedbasket.util;

import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;
// this class is used to create placeholder DTOs for the frontend, especially for the dashboard page. It provides static methods to generate sample data for shopping items and user information.
public final class FrontendDtoAdapter {
    private FrontendDtoAdapter() {}

    public static List<ShoppingItemDTO> createPlaceholderShoppingItems() {
        List<ShoppingItemDTO> items = new ArrayList<>();
        items.add(new ShoppingItemDTO("Organic Apples",4,8.99));
        items.add(new ShoppingItemDTO("Whole Wheat Bread",2,5.50));
        items.add(new ShoppingItemDTO("Greek Yogurt",6,12.00));
        return items;
    }

    public static UserDTO createGuestUser() {
        return new UserDTO(null, "guest@sharedbasket.local", "Guest user", "MEMBER");
    }

    public static UserDTO normalizeUser(UserDTO u) {
        if (u==null) return createGuestUser();
        return u;
    }
}
