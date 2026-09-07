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

    public List<AuditLog> findAllOrderedDesc() {
        return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class)
                .getResultList();
    }
}
