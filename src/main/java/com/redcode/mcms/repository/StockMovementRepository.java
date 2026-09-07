package com.redcode.mcms.repository;

import com.redcode.mcms.entity.StockMovement;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link StockMovement} history.
 */
@Stateless
public class StockMovementRepository extends GenericRepository<StockMovement, Long> {

    public List<StockMovement> findByProduct(Long productId) {
        return em.createQuery(
                        "SELECT m FROM StockMovement m WHERE m.product.id = :id ORDER BY m.createdAt DESC",
                        StockMovement.class)
                .setParameter("id", productId)
                .getResultList();
    }

    public List<StockMovement> findRecent(int limit) {
        return em.createQuery("SELECT m FROM StockMovement m ORDER BY m.createdAt DESC", StockMovement.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
