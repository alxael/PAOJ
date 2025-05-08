package com.exception;

public class EntryDuplicateFoundException extends RuntimeException {
    public EntryDuplicateFoundException() {
        super("Unique resource duplicate found!");
    }

    public EntryDuplicateFoundException(String message) {
        // warn log here
        super(message);
    }

    public EntryDuplicateFoundException(String query, String message) {
        // error log here
        super(message);
    }
}
