package com.sharedbasket.util;

import com.sharedbasket.dto.RecommendationDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.StatisticsDTO;
import com.sharedbasket.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;
// this class is used to create placeholder DTOs for the frontend, especially for the dashboard and recommendations pages. It provides static methods to generate sample data for shopping items, statistics, recommendations, and user information.
public final class FrontendDtoAdapter {
    private FrontendDtoAdapter() {}

    public static List<ShoppingItemDTO> createPlaceholderShoppingItems() {
        List<ShoppingItemDTO> items = new ArrayList<>();
        items.add(new ShoppingItemDTO("Organic Apples",4,8.99));
        items.add(new ShoppingItemDTO("Whole Wheat Bread",2,5.50));
        items.add(new ShoppingItemDTO("Greek Yogurt",6,12.00));
        return items;
    }

    public static StatisticsDTO createPlaceholderStatistics() {
        return new StatisticsDTO(12,98.74,7);
    }

    public static List<RecommendationDTO> createPlaceholderRecommendations() {
        List<RecommendationDTO> recs = new ArrayList<>();
        recs.add(new RecommendationDTO("Milk","Dairy",4));
        recs.add(new RecommendationDTO("Bananas","Fruit",5));
        recs.add(new RecommendationDTO("Rice","Pantry",3));
        return recs;
    }

    public static UserDTO createGuestUser() {
        return new UserDTO(null, "guest@sharedbasket.local", "Guest user", "MEMBER");
    }

    public static UserDTO normalizeUser(UserDTO u) {
        if (u==null) return createGuestUser();
        return u;
    }
}
