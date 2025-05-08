package com.exception;

public class IdentifierNotFoundException extends RuntimeException {
    public IdentifierNotFoundException(String identifier) {
        super("Could not find identifier " + identifier + " in query string!");
    }

    public IdentifierNotFoundException() {
        super("Could not find identifier in query string!");
    }
}
