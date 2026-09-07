package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Promotion;
import com.redcode.mcms.entity.PromotionStatus;
import jakarta.ejb.Stateless;

import java.util.List;

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
}
