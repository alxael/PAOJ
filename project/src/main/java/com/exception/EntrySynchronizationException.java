package com.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntrySynchronizationException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(EntrySynchronizationException.class);

    public EntrySynchronizationException() {
        super("Local resource is not synchronized with remote resource!");
    }

    public EntrySynchronizationException(String message) {
        super(message);
        log.warn(message);
    }

    public EntrySynchronizationException(String query, String message) {
        super(message);
        log.error("Query {} result is not synchronized with local data.", query);
        log.warn(message);
    }
}
