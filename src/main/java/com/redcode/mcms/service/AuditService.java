package com.redcode.mcms.service;

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
 * Audit writes participate in the caller's container-managed transaction.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AuditService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private AuthContext authContext;

    public void log(String action, String entity, Long entityId, String description) {
        em.createNativeQuery("INSERT INTO audit_log "
                        + "(username, action, entity, entity_id, timestamp, description, delta_diff, ip_address, client_token_id) "
                        + "VALUES (?, ?, ?, CAST(? AS bigint), CURRENT_TIMESTAMP, ?, CAST(? AS jsonb), ?, ?)")
                .setParameter(1, username())
                .setParameter(2, action)
                .setParameter(3, entity)
                .setParameter(4, entityId)
                .setParameter(5, description)
                .setParameter(6, null)
                .setParameter(7, null)
                .setParameter(8, null)
                .executeUpdate();
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
