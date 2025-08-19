package org.example.utils;

import java.util.regex.Pattern;

public class NonNegativeIntegerCheck {
    private static final Pattern NON_NEGATIVE_INTEGER_PATTERN = Pattern.compile("0|[1-9]\\d*");

    public static boolean isNonNegativeInteger(String str) {
        return str != null && NON_NEGATIVE_INTEGER_PATTERN.matcher(str).matches();
    }
}