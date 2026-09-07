package com.redcode.mcms.exception;

/**
 * Thrown when an unauthenticated request reaches a protected resource.
 * Mapped to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
