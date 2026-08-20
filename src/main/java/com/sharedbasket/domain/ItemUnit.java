package com.sharedbasket.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum ItemUnit {
    PIECE("Per item", "pcs", 0, BigDecimal.ONE, new BigDecimal("1")),
    KILOGRAM("Weight (kg)", "kg", 3, new BigDecimal("0.001"), new BigDecimal("0.1"));

    private final String label;
    private final String shortLabel;
    private final int scale;
    private final BigDecimal minQuantity;
    private final BigDecimal step;

    ItemUnit(String label, String shortLabel, int scale, BigDecimal minQuantity, BigDecimal step) {
        this.label = label;
        this.shortLabel = shortLabel;
        this.scale = scale;
        this.minQuantity = minQuantity;
        this.step = step;
    }

    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public int getScale() {
        return scale;
    }

    public BigDecimal getMinQuantity() {
        return minQuantity;
    }

    public BigDecimal getStep() {
        return step;
    }

    /** Accepts the enum name plus the legacy free-text values that existed before the unit dropdown. */
    public static ItemUnit fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PIECE;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "kilogram":
            case "kilograms":
            case "kilo":
            case "kg":
                return KILOGRAM;
            default:
                return PIECE;
        }
    }

    public static List<ItemUnit> options() {
        return Arrays.stream(values()).collect(Collectors.toList());
    }
}
