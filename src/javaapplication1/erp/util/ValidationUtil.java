package javaapplication1.erp.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Centralized validation helper.
 */
public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^.+@.+\\..+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-()\\s]{6,}$");

    private ValidationUtil() {}

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) throw new ValidationException(fieldName + " is required");
    }

    public static void requireEmail(String email, String fieldName) {
        if (email == null || !EMAIL.matcher(email).matches()) throw new ValidationException(fieldName + " is invalid");
    }

    public static void requirePhone(String phone, String fieldName) {
        if (phone != null && !phone.trim().isEmpty() && !PHONE.matcher(phone).matches()) throw new ValidationException(fieldName + " is invalid");
    }

    public static void requirePositive(BigDecimal val, String fieldName) {
        if (val == null || val.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException(fieldName + " must be positive");
    }
}
