package com.redcode.mcms.service;

import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import com.redcode.mcms.security.SupplierConfirmationTokenService;
import com.redcode.mcms.websocket.ApprovalWebSocket;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Stateless
public class SupplierConfirmationService {
    @Inject private PurchaseOrderRepository purchaseOrderRepository;
    @Inject private SupplierConfirmationTokenService tokenService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void acknowledge(String token) {
        SupplierConfirmationTokenService.Claims claims = tokenService.verify(token);
        if (claims == null) throw new BusinessException("The supplier confirmation link is invalid or expired.");
        PurchaseOrder po = purchaseOrderRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new BusinessException("The supplier confirmation link has already been used."));
        if (!MessageDigest.isEqual(po.getConfirmationToken().getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))
                || !po.getId().equals(claims.orderId()) || !po.getSupplier().getId().equals(claims.supplierId())
                || po.getTokenExpiryTimestamp() == null
                || LocalDateTime.now().isAfter(po.getTokenExpiryTimestamp())) {
            throw new BusinessException("The supplier confirmation link is invalid or expired.");
        }
        if (po.getStatus() != PurchaseOrderStatus.APPROVED
                && po.getStatus() != PurchaseOrderStatus.COMMUNICATED_TO_SUPPLIER) {
            throw new BusinessException("This purchase order is not available for supplier acknowledgement.");
        }
        po.setStatus(PurchaseOrderStatus.ACKNOWLEDGED);
        po.setConfirmationToken(null);
        po.setTokenExpiryTimestamp(null);
        purchaseOrderRepository.update(po);
        ApprovalWebSocket.broadcastSupplierAcknowledged(po.getId(), po.getPoNumber(), po.getSupplier().getName());
    }
}
