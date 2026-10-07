package com.redcode.mcms.repository;

import com.redcode.mcms.entity.SupplierProductMap;
import com.redcode.mcms.entity.SupplierProductMapId;
import jakarta.ejb.Stateless;
import java.util.Optional;

@Stateless
public class SupplierProductMapRepository extends GenericRepository<SupplierProductMap, SupplierProductMapId> {
    public Optional<SupplierProductMap> findByProductIdAndSupplierId(Long productId, Long supplierId) {
        return em.createQuery("SELECT m FROM SupplierProductMap m WHERE m.product.id = :productId AND m.supplier.id = :supplierId", SupplierProductMap.class)
                .setParameter("productId", productId).setParameter("supplierId", supplierId)
                .getResultStream().findFirst();
    }
    public Optional<SupplierProductMap> findPrimaryForProduct(Long productId) {
        return em.createQuery("SELECT m FROM SupplierProductMap m WHERE m.product.id = :productId ORDER BY m.unitCost", SupplierProductMap.class)
                .setParameter("productId", productId).setMaxResults(1).getResultStream().findFirst();
    }
}
