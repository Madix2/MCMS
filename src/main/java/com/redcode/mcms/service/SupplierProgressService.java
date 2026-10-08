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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Stateless
public class SupplierProgressService {
    @Inject private PurchaseOrderRepository repository;
    @Inject private SupplierConfirmationTokenService tokenService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void update(String token, String requestedStatus) {
        SupplierConfirmationTokenService.Claims claims = tokenService.verify(token);
        if (claims == null || !"SUPPLIER_PO_PROGRESS".equals(claims.purpose())) {
            throw new BusinessException("The supplier progress link is invalid or expired.");
        }
        PurchaseOrder po = repository.findByProgressToken(token)
                .orElseThrow(() -> new BusinessException("The supplier progress link is invalid or expired."));
        if (!MessageDigest.isEqual(po.getProgressToken().getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))
                || !po.getId().equals(claims.orderId()) || !po.getSupplier().getId().equals(claims.supplierId())
                || po.getProgressTokenExpiryTimestamp() == null
                || LocalDateTime.now().isAfter(po.getProgressTokenExpiryTimestamp())) {
            throw new BusinessException("The supplier progress link is invalid or expired.");
        }
        if (po.getStatus() != PurchaseOrderStatus.APPROVED
                && po.getStatus() != PurchaseOrderStatus.COMMUNICATED_TO_SUPPLIER
                && po.getStatus() != PurchaseOrderStatus.ACKNOWLEDGED
                && po.getStatus() != PurchaseOrderStatus.SHIPPED
                && po.getStatus() != PurchaseOrderStatus.DELAYED) {
            throw new BusinessException("This purchase order is not available for delivery progress updates.");
        }
        PurchaseOrderStatus status = PurchaseOrderStatus.valueOf(requestedStatus);
        po.setStatus(status);
        repository.update(po);
        ApprovalWebSocket.broadcastSupplierProgress(po.getId(), po.getPoNumber(), po.getSupplier().getName(), status.name());
    }
}
