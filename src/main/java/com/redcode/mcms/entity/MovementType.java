package com.redcode.mcms.entity;

/**
 * Reason a stock adjustment was made (sale deduction, receiving stock,
 * manual adjustment, return, etc.).
 */
public enum MovementType {
    SALE,
    PURCHASE,
    ADJUSTMENT,
    RETURN_IN,
    RETURN_OUT,
    INITIAL
}
