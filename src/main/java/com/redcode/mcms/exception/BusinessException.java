package com.redcode.mcms.exception;

/**
 * Thrown when a business rule is violated (e.g. insufficient stock).
 * Mapped to HTTP 400 by {@link GlobalExceptionMapper}.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
