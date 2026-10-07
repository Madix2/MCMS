package com.redcode.mcms.repository;

import com.redcode.mcms.entity.EmailOutbox;
import com.redcode.mcms.entity.EmailOutboxStatus;
import jakarta.ejb.Stateless;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class EmailOutboxRepository extends GenericRepository<EmailOutbox, Long> {
    public List<EmailOutbox> findDue(int limit) {
        return em.createQuery("SELECT e FROM EmailOutbox e WHERE e.status IN :statuses "
                        + "AND e.nextAttemptAt <= :now ORDER BY e.createdAt", EmailOutbox.class)
                .setParameter("statuses", List.of(EmailOutboxStatus.PENDING, EmailOutboxStatus.FAILED))
                .setParameter("now", LocalDateTime.now())
                .setMaxResults(limit)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }
}
