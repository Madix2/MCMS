package com.redcode.mcms.exception;

/**
 * Thrown when an entity is not found. Mapped to HTTP 404.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
