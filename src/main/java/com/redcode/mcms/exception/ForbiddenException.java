package com.redcode.mcms.exception;

/**
 * Thrown when an authenticated user attempts an action outside their role.
 * Mapped to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
