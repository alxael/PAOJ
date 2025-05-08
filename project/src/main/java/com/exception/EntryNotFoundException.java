package com.exception;

public class EntryNotFoundException extends RuntimeException {
    public EntryNotFoundException() {
        super("Requested resource not found!");
    }

    public EntryNotFoundException(String message) {
        // warn log here
        super(message);
    }

    public EntryNotFoundException(String query, String message) {
        // error log here
        super(message);
    }
}
