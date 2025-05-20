package com.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntryDuplicateFoundException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(EntryDuplicateFoundException.class);

    public EntryDuplicateFoundException() {
        super("Unique resource duplicate found!");
    }

    public EntryDuplicateFoundException(String message) {
        super(message);
        log.warn(message);
    }

    public EntryDuplicateFoundException(String query, String message) {
        super(message);
        log.error("Query {} returned multiple entries, when identifier should be unique.", query);
        log.warn(message);
    }
}
