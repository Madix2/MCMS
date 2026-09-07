package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Campaign;
import com.redcode.mcms.entity.PromotionStatus;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link Campaign}.
 */
@Stateless
public class CampaignRepository extends GenericRepository<Campaign, Long> {

    public List<Campaign> findAllOrderedDesc() {
        return em.createQuery("SELECT c FROM Campaign c ORDER BY c.createdAt DESC", Campaign.class)
                .getResultList();
    }

    public List<Campaign> findByStatus(PromotionStatus status) {
        return em.createQuery("SELECT c FROM Campaign c WHERE c.status = :s ORDER BY c.createdAt DESC",
                        Campaign.class)
                .setParameter("s", status)
                .getResultList();
    }
}
