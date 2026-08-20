package com.sharedbasket.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Pre-integrated categories seeded into every group. Emoji and color are server-owned product data. */
public enum DefaultCategory {
    DAIRY("Dairy", "#3498db", "\uD83E\uDD5B"),
    MEAT("Meat & Fish", "#e74c3c", "\uD83E\uDD69"),
    PRODUCE("Fruits & Vegetables", "#27ae60", "\uD83E\uDD6C"),
    BAKERY("Bakery", "#f39c12", "\uD83E\uDD56"),
    FROZEN("Frozen", "#16a085", "\uD83E\uDDCA"),
    PANTRY("Pantry", "#8e44ad", "\uD83E\uDD6B"),
    BEVERAGES("Beverages", "#3498db", "\uD83E\uDDC3"),
    SNACKS("Snacks", "#f39c12", "\uD83C\uDF6A"),
    HOUSEHOLD("Household", "#7f8c8d", "\uD83E\uDDFB"),
    PERSONAL_CARE("Personal Care", "#8e44ad", "\uD83E\uDDF4"),
    OTHER("Other", "#7f8c8d", "\uD83D\uDED2");

    /** Name of the guaranteed fallback category used when no category is selected. */
    public static final String FALLBACK_NAME = "Other";

    private final String displayName;
    private final String color;
    private final String emoji;

    DefaultCategory(String displayName, String color, String emoji) {
        this.displayName = displayName;
        this.color = color;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getEmoji() {
        return emoji;
    }

    public static List<DefaultCategory> all() {
        return Arrays.stream(values()).collect(Collectors.toList());
    }
}
