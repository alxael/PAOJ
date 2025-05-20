package com.exception;

public class ValidationException extends RuntimeException {
    public ValidationException() {
        super("Invalid input!");
    }
}
