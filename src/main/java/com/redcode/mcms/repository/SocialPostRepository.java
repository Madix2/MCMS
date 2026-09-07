package com.redcode.mcms.repository;

import com.redcode.mcms.entity.SocialPost;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link SocialPost} (simulated social-media integration).
 */
@Stateless
public class SocialPostRepository extends GenericRepository<SocialPost, Long> {

    public List<SocialPost> findAllOrderedDesc() {
        return em.createQuery("SELECT p FROM SocialPost p ORDER BY p.scheduledAt DESC", SocialPost.class)
                .getResultList();
    }

    public List<SocialPost> findByCampaign(Long campaignId) {
        return em.createQuery("SELECT p FROM SocialPost p WHERE p.campaign.id = :id ORDER BY p.scheduledAt DESC",
                        SocialPost.class)
                .setParameter("id", campaignId)
                .getResultList();
    }
}
