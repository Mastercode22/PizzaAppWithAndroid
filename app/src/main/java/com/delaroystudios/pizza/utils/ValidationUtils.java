package com.delaroystudios.pizza.utils;



import android.util.Patterns;
import android.text.TextUtils;

public class ValidationUtils {

    /**
     * Validates email address format
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates phone number (minimum 10 digits)
     */
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        // Remove any non-digit characters for validation
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10;
    }

    /**
     * Validates password strength (minimum 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    /**
     * Validates password strength with detailed requirements
     */
    public static boolean isStrongPassword(String password) {
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase, one lowercase, and one digit
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Validates username (3-20 characters, alphanumeric and underscore only)
     */
    public static boolean isValidUsername(String username) {
        if (TextUtils.isEmpty(username)) return false;
        return username.length() >= 3 && username.length() <= 20 &&
                username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Validates name (not empty, reasonable length)
     */
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) return false;
        String trimmedName = name.trim();
        return trimmedName.length() >= 2 && trimmedName.length() <= 50;
    }

    /**
     * Validates pizza size selection
     */
    public static boolean isValidPizzaSize(String size) {
        if (TextUtils.isEmpty(size)) return false;
        return size.equals("Small") || size.equals("Medium") ||
                size.equals("Large") || size.equals("Party");
    }

    /**
     * Validates pizza crust selection
     */
    public static boolean isValidCrust(String crust) {
        if (TextUtils.isEmpty(crust)) return false;
        return crust.equals("Thin") || crust.equals("Thick") ||
                crust.equals("Deep Dish") || crust.equals("Stuffed");
    }

    /**
     * Validates price value
     */
    public static boolean isValidPrice(double price) {
        return price >= 0.0 && price <= 999.99;
    }

    /**
     * Validates quantity
     */
    public static boolean isValidQuantity(int quantity) {
        return quantity > 0 && quantity <= 50;
    }

    /**
     * Get password strength message
     */
    public static String getPasswordStrengthMessage(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }

        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }

        if (password.length() < 8) {
            return "Password is weak. Consider using at least 8 characters.";
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int strengthScore = 0;
        if (hasUpper) strengthScore++;
        if (hasLower) strengthScore++;
        if (hasDigit) strengthScore++;
        if (hasSpecial) strengthScore++;
        if (password.length() >= 12) strengthScore++;

        switch (strengthScore) {
            case 0:
            case 1:
                return "Password is very weak";
            case 2:
                return "Password is weak";
            case 3:
                return "Password is fair";
            case 4:
                return "Password is strong";
            case 5:
                return "Password is very strong";
            default:
                return "Password accepted";
        }
    }
}