package com.exception;

public class InvalidBusinessLogicException extends RuntimeException {
    public InvalidBusinessLogicException(String s) {
        super("Invalid business logic!");
    }
}
