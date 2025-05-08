package com.validation;

import java.util.regex.Pattern;

import static java.lang.Character.isDigit;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.isLowerCase;

public class Validator {
    private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_.]+@[a-zA-Z0-9_]+.[a-zA-Z0-9_.]+$");
    private static final String specialCharacters = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    public static boolean isPasswordStrong(String password) {
        boolean hasLower = false, hasUpper = false, hasSpecial = false, hasNumber = false;
        for (char c : password.toCharArray()) {
            if (isUpperCase(c)) {
                hasUpper = true;
            }
            if (isLowerCase(c)) {
                hasLower = true;
            }
            if (isDigit(c)) {
                hasNumber = true;
            }
            if (specialCharacters.indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }
        return hasLower && hasUpper && hasSpecial && hasNumber;
    }

    public static boolean isEmail(String email) {
        return emailPattern.matcher(email).matches();
    }
}
