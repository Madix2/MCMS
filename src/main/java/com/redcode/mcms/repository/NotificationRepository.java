package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Notification;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link Notification}.
 */
@Stateless
public class NotificationRepository extends GenericRepository<Notification, Long> {

    public List<Notification> findRecent(int limit) {
        return em.createQuery("SELECT n FROM Notification n ORDER BY n.createdAt DESC", Notification.class)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Notifications relevant to a given role. A notification with a null/empty
     * targetRole is treated as general and shown to everyone.
     */
    public List<Notification> findByRole(int limit, String role) {
        return em.createQuery(
                        "SELECT n FROM Notification n WHERE n.targetRole IS NULL OR n.targetRole = '' "
                                + "OR n.targetRole = :role ORDER BY n.createdAt DESC", Notification.class)
                .setParameter("role", role)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countUnread() {
        return em.createQuery("SELECT COUNT(n) FROM Notification n WHERE n.read = false", Long.class)
                .getSingleResult();
    }
}
