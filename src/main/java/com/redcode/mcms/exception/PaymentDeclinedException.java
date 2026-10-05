package com.redcode.mcms.exception;

import jakarta.ejb.ApplicationException;

/** Returned when the demonstration payment gateway declines authorization. */
@ApplicationException(rollback = true)
public class PaymentDeclinedException extends RuntimeException {
    public PaymentDeclinedException(String message) {
        super(message);
    }
}
