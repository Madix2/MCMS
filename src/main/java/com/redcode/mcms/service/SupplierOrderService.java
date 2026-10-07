package com.redcode.mcms.service;

import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class SupplierOrderService {
    @Inject private PurchaseOrderRepository repository;

    public List<PurchaseOrder> findVisible(Long supplierId) {
        if (supplierId == null) throw new IllegalArgumentException("Supplier identity is required.");
        return repository.findSupplierVisible(supplierId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrder updateStatus(Long id, Long supplierId, String status) {
        if (id == null || supplierId == null || status == null) throw new IllegalArgumentException("Order, supplier and status are required.");
        PurchaseOrder po = repository.findById(id).orElseThrow(() -> new BusinessException("Purchase order not found."));
        if (!supplierId.equals(po.getSupplier().getId())) throw new BusinessException("Order does not belong to this supplier.");
        if (po.getStatus() != PurchaseOrderStatus.APPROVED && po.getStatus() != PurchaseOrderStatus.COMMUNICATED_TO_SUPPLIER
                && po.getStatus() != PurchaseOrderStatus.SHIPPED && po.getStatus() != PurchaseOrderStatus.DELAYED) {
            throw new BusinessException("Only communicated orders may change delivery status.");
        }
        po.setStatus(PurchaseOrderStatus.valueOf(status));
        return repository.update(po);
    }
}
