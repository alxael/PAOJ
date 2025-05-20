package com.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntryNotFoundException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(EntryNotFoundException.class);

    public EntryNotFoundException() {
        super("Requested resource not found!");
    }

    public EntryNotFoundException(String message) {
        super(message);
        log.warn(message);
    }

    public EntryNotFoundException(String query, String message) {
        super(message);
        log.error("Query {} returned an empty result!", query);
        log.warn(message);
    }
}
