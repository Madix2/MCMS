package com.redcode.mcms.repository;

import com.redcode.mcms.entity.AuditLog;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link AuditLog}.
 */
@Stateless
public class AuditLogRepository extends GenericRepository<AuditLog, Long> {

    public List<AuditLog> findRecent(int limit) {
        return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> findPage(int page, int size) {
        return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class)
                .setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countAll() {
        return em.createQuery("SELECT COUNT(a) FROM AuditLog a", Long.class).getSingleResult();
    }

    public List<AuditLog> findAllOrderedDesc() {
        return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class)
                .getResultList();
    }
}
