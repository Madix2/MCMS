package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Promotion;
import com.redcode.mcms.entity.PromotionStatus;
import jakarta.ejb.Stateless;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Data-access for {@link Promotion}.
 */
@Stateless
public class PromotionRepository extends GenericRepository<Promotion, Long> {

    public List<Promotion> findAllOrderedDesc() {
        return em.createQuery("SELECT p FROM Promotion p ORDER BY p.createdAt DESC", Promotion.class)
                .getResultList();
    }

    public List<Promotion> findByStatus(PromotionStatus status) {
        return em.createQuery("SELECT p FROM Promotion p WHERE p.status = :s ORDER BY p.endDate DESC",
                        Promotion.class)
                .setParameter("s", status)
                .getResultList();
    }

    public List<Promotion> findActive() {
        return findByStatus(PromotionStatus.ACTIVE);
    }

    public Optional<Promotion> findBestActiveForProduct(Long productId, Long categoryId) {
        List<Promotion> matches = em.createQuery(
                        "SELECT p FROM Promotion p WHERE p.status = :status "
                                + "AND (p.product.id = :productId OR p.category.id = :categoryId "
                                + "OR (p.product IS NULL AND p.category IS NULL)) "
                                + "AND (p.startDate IS NULL OR p.startDate <= :today) "
                                + "AND (p.endDate IS NULL OR p.endDate >= :today) "
                                + "ORDER BY CASE WHEN p.product.id = :productId THEN 0 "
                                + "WHEN p.category.id = :categoryId THEN 1 ELSE 2 END, p.discount DESC",
                        Promotion.class)
                .setParameter("status", PromotionStatus.ACTIVE)
                .setParameter("productId", productId)
                .setParameter("categoryId", categoryId)
                .setParameter("today", LocalDate.now())
                .setMaxResults(1)
                .getResultList();
        return matches.stream().findFirst();
    }
}
