package com.redcode.mcms.entity;

/**
 * Lifecycle of a purchase order through its approval and receiving workflow.
 */
public enum PurchaseOrderStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    COMMUNICATED_TO_SUPPLIER,
    ACKNOWLEDGED,
    REJECTED,
    ORDERED,
    PARTIALLY_RECEIVED,
    COMPLETED,
    SHIPPED,
    DELAYED,
    RECEIVED,
    CANCELLED
}
