package com.sharedbasket.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CategoryColorOption {
    BLUE("Blue", "#3498db"),
    GREEN("Green", "#27ae60"),
    ORANGE("Orange", "#f39c12"),
    RED("Red", "#e74c3c"),
    PURPLE("Purple", "#8e44ad"),
    TEAL("Teal", "#16a085"),
    GRAY("Gray", "#7f8c8d");

    private final String label;
    private final String value;

    CategoryColorOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public static boolean isAllowed(String value) {
        return value == null || Arrays.stream(values()).anyMatch(option -> option.value.equalsIgnoreCase(value.trim()));
    }

    public static String defaultValue() {
        return BLUE.value;
    }

    public static List<CategoryColorOption> options() {
        return Arrays.stream(values()).collect(Collectors.toList());
    }
}
