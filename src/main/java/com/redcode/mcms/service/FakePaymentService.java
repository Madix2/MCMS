package com.redcode.mcms.service;

import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.PaymentDeclinedException;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;
import java.util.UUID;

/** Deterministic fake gateway for demos; it never contacts a bank or processor. */
@Stateless
public class FakePaymentService {

    public PaymentResult authorize(String method, BigDecimal amount, BigDecimal tendered, String scenario) {
        String selected = scenario == null || scenario.isBlank() ? "SUCCESS" : scenario.toUpperCase();
        switch (selected) {
            case "SUCCESS":
                break;
            case "DECLINED_INSUFFICIENT_FUNDS":
                throw new PaymentDeclinedException("Payment declined: insufficient funds.");
            case "DECLINED_EXPIRED_CARD":
                throw new PaymentDeclinedException("Payment declined: card expired.");
            case "DECLINED_NETWORK":
                throw new PaymentDeclinedException("Payment declined: simulated payment network error.");
            default:
                throw new BusinessException("Unknown payment simulation scenario.");
        }

        if ("CASH".equalsIgnoreCase(method) && tendered.compareTo(amount) < 0) {
            throw new PaymentDeclinedException("Payment declined: amount tendered is insufficient.");
        }
        return new PaymentResult("SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public record PaymentResult(String reference) { }
}
