package com.redcode.mcms.service;

import com.redcode.mcms.entity.AuditLog;
import com.redcode.mcms.repository.AuditLogRepository;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Records an immutable audit trail of significant user actions.
 * E.g. "Manager approved Purchase Order #PO-105".
 *
 * The write runs in its own transaction (REQUIRES_NEW) so that the audit record
 * is persisted even if the surrounding business transaction later rolls back.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AuditService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private AuthContext authContext;

    public void log(String action, String entity, Long entityId, String description) {
        AuditLog log = new AuditLog();
        log.setUsername(username());
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDescription(description);
        em.persist(log);
    }

    public void log(String action, String entity, String description) {
        log(action, entity, null, description);
    }

    private String username() {
        if (authContext != null && authContext.isAuthenticated()) {
            return authContext.getUser().getUsername();
        }
        return "SYSTEM";
    }
}
