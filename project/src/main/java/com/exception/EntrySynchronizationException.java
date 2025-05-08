package com.exception;

public class EntrySynchronizationException extends RuntimeException {
    public EntrySynchronizationException() {
        super("Local resource is not synchronized with remote resource!");
    }

    public EntrySynchronizationException(String message) {
        // warn log here
        super(message);
    }

    public EntrySynchronizationException(String query, String message) {
        // error log here
        super(message);
    }
}
