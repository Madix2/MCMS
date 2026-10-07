package com.redcode.mcms.service;

import com.redcode.mcms.entity.PurchaseOrder;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

@Stateless
public class EmailNotificationService {
    @Inject private EmailOutboxService outboxService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendPurchaseOrderToSupplier(PurchaseOrder po) {
        outboxService.enqueue(po);
    }
}
