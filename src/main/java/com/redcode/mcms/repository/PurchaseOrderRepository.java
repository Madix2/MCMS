package com.redcode.mcms.repository;

import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link PurchaseOrder}.
 */
@Stateless
public class PurchaseOrderRepository extends GenericRepository<PurchaseOrder, Long> {

    public List<PurchaseOrder> findAllOrderedDesc() {
        return em.createQuery("SELECT po FROM PurchaseOrder po ORDER BY po.orderDate DESC",
                        PurchaseOrder.class)
                .getResultList();
    }

    public List<PurchaseOrder> findByStatus(PurchaseOrderStatus status) {
        return em.createQuery("SELECT po FROM PurchaseOrder po WHERE po.status = :s ORDER BY po.orderDate DESC",
                        PurchaseOrder.class)
                .setParameter("s", status)
                .getResultList();
    }

    public List<PurchaseOrder> findPendingApproval() {
        return findByStatus(PurchaseOrderStatus.PENDING_APPROVAL);
    }

    public long countByStatus(PurchaseOrderStatus status) {
        return em.createQuery("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = :s", Long.class)
                .setParameter("s", status)
                .getSingleResult();
    }

    public List<PurchaseOrder> findSupplierVisible(Long supplierId) {
        return em.createQuery("SELECT DISTINCT po FROM PurchaseOrder po JOIN FETCH po.items i "
                        + "WHERE po.supplier.id = :supplierId AND po.status IN :statuses ORDER BY po.orderDate DESC", PurchaseOrder.class)
                .setParameter("supplierId", supplierId)
                .setParameter("statuses", List.of(PurchaseOrderStatus.APPROVED, PurchaseOrderStatus.COMMUNICATED_TO_SUPPLIER))
                .getResultList();
    }
}
