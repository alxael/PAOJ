package com.exception;

public class UserQuitException extends RuntimeException {
    public UserQuitException() {
        super("User quit!");
    }
}
