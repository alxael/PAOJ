package com.bank;

import com.validation.Validator;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User implements Serializable {
    private Country country;
    private String email;
    private String firstName;
    private String lastName;
    private String password;

    private String hashPassword(String password) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-512");
            byte[] hashedPassword = messageDigest.digest(password.getBytes());

            var stringBuilder = new StringBuilder();
            for (byte b : hashedPassword) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return "";
        }
    }

    // constructor for new user
    public User(String password, String lastName, String firstName, String email, Country country) {
        if (!Validator.isEmail(email)) {
            throw new IllegalArgumentException("The provided email is not valid.");
        }

        if (!Validator.isPasswordStrong(password)) {
            throw new IllegalArgumentException("The provided password does not match the strength criteria. Please make sure your password contains at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character.");
        }

        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("The first name must not be empty.");
        }

        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("The last name must not be empty.");
        }

        this.password = hashPassword(password);
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.country = country;
    }

    // constructor for existing user
    public User(Country country, String password, String lastName, String firstName, String email) {
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User [country=" + country + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName + ", password=" + password + "]";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof User user)) return false;

        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    public boolean authenticate(String password) {
        var passwordHashed = hashPassword(password);
        return this.password.compareTo(passwordHashed) == 0;
    }
}
