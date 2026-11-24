package com.sos.util;

import com.sos.exception.InvalidInputException;

/** All input checks in one place. Only String and Character methods (no regex). */
public final class Validator {
    private Validator() { }

    public static String requireText(String value, String field, int maxLength) throws InvalidInputException {
        if (value == null || value.trim().isEmpty())
            throw new InvalidInputException(field + " is required");
        value = value.trim();
        if (value.length() > maxLength)
            throw new InvalidInputException(field + " must be at most " + maxLength + " characters");
        return value;
    }

    public static String validateEmail(String email) throws InvalidInputException {
        email = requireText(email, "Email", 100).toLowerCase();
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        boolean bad = email.contains(" ")
                || at < 1                              // nothing before @
                || email.indexOf('@', at + 1) != -1    // second @
                || dot < at + 2                        // no dot after the domain name
                || dot == email.length() - 1;          // ends with a dot
        if (bad) throw new InvalidInputException("Invalid email format");
        return email;
    }

    public static String validatePhone(String phone) throws InvalidInputException {
        phone = requireText(phone, "Phone", 15);
        if (phone.length() != 10) throw new InvalidInputException("Phone must be exactly 10 digits");
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i)))
                throw new InvalidInputException("Phone must contain digits only");
        }
        return phone;
    }

    public static void validatePassword(String password) throws InvalidInputException {
        if (password == null || password.length() < 6)
            throw new InvalidInputException("Password must be at least 6 characters");
    }

    /** "1" (highest) to "5" (lowest). Uses the Integer wrapper class. */
    public static int parsePriority(String text) throws InvalidInputException {
        try {
            int p = Integer.parseInt(text == null ? "" : text.trim());
            if (p < 1 || p > 5) throw new NumberFormatException();
            return p;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Priority must be a number from 1 (highest) to 5");
        }
    }

    /** "ketan sharma" -> "Ketan Sharma" using StringBuilder. */
    public static String toTitleCase(String text) {
        StringBuilder result = new StringBuilder();
        String[] words = text.trim().split(" ");
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0)));
            result.append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }
}
