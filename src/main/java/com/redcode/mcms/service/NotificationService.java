package com.redcode.mcms.service;

import com.redcode.mcms.entity.Notification;
import com.redcode.mcms.repository.NotificationRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Central notification system. Notifications are created automatically from
 * business events (low stock, purchase-order approvals, new employees, etc.)
 * and displayed on the dashboard.
 */
@Stateless
public class NotificationService {

    @Inject
    private NotificationRepository notificationRepository;

    @Inject
    private AuditService auditService;

    public Notification notify(String type, String title, String message, String targetRole) {
        Notification n = new Notification();
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setTargetRole(targetRole);
        return notificationRepository.save(n);
    }

    public List<Notification> recent(int limit) {
        return notificationRepository.findRecent(limit);
    }

    /** Notifications relevant to the caller's role. */
    public List<Notification> forRole(int limit, String role) {
        return notificationRepository.findByRole(limit, role);
    }

    public long unreadCount() {
        return notificationRepository.countUnread();
    }

    public Notification markRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new com.redcode.mcms.exception.NotFoundException("Notification not found."));
        n.setRead(true);
        return notificationRepository.update(n);
    }

    public void clearSystemEvent(String title) {
        // placeholder hook for future cleanup
    }
}
