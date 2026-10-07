package com.redcode.mcms.service;

import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import com.redcode.mcms.entity.User;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import com.redcode.mcms.repository.UserRepository;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Stateless
public class PurchaseOrderApprovalService {
    @Inject private PurchaseOrderRepository purchaseOrderRepository;
    @Inject private UserRepository userRepository;
    @Inject private AuthContext authContext;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrder process(Long id, String administrativeOverrideToken) {
        if (id == null) throw new IllegalArgumentException("Purchase order id is required.");
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Purchase order not found."));
        BigDecimal total = po.getTotal() == null ? BigDecimal.ZERO : po.getTotal();
        if (total.compareTo(new BigDecimal("200000")) > 0) {
            if (!authContext.isAuthenticated() || !authContext.getUser().getRole().equalsIgnoreCase("ADMIN")
                    || administrativeOverrideToken == null || administrativeOverrideToken.isBlank()) {
                throw new BusinessException("Orders above R200,000 require an ADMINISTRATOR security authorization override token.");
            }
        } else if (total.compareTo(new BigDecimal("50000")) >= 0) {
            authContext.requireRole(AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        }
        User approver = authContext.isAuthenticated() ? userRepository.findById(authContext.getUser().getId()).orElse(null) : null;
        po.setApprovedBy(approver);
        po.setApprovalTimestamp(OffsetDateTime.now());
        po.setStatus(PurchaseOrderStatus.APPROVED);
        return purchaseOrderRepository.update(po);
    }
}
