package com.redcode.mcms.entity;

/**
 * Lifecycle of a purchase order through its approval and receiving workflow.
 */
public enum PurchaseOrderStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    ORDERED,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CANCELLED
}
