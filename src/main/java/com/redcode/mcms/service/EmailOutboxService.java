package com.redcode.mcms.service;

import com.redcode.mcms.entity.EmailOutbox;
import com.redcode.mcms.entity.EmailOutboxStatus;
import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.entity.PurchaseOrderItem;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import com.redcode.mcms.repository.EmailOutboxRepository;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import com.redcode.mcms.security.SupplierConfirmationTokenService;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Objects;

@Stateless
public class EmailOutboxService {
    @Inject private EmailOutboxRepository outboxRepository;
    @Inject private PurchaseOrderRepository purchaseOrderRepository;
    @Inject private SupplierConfirmationTokenService tokenService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void enqueue(PurchaseOrder po) {
        if (po == null || po.getId() == null || po.getSupplier() == null
                || po.getSupplier().getEmail() == null || po.getSupplier().getEmail().isBlank()) {
            throw new IllegalArgumentException("A purchase order with a supplier email is required.");
        }
        EmailOutbox message = new EmailOutbox();
        message.setPurchaseOrderId(po.getId());
        message.setRecipient(po.getSupplier().getEmail());
        message.setSubject("MegaMart purchase order " + po.getPoNumber());
        LocalDateTime expiry = LocalDateTime.now().plusHours(72);
        po.setConfirmationToken(tokenService.issue(po.getId(), po.getSupplier().getId(), expiry.toInstant(java.time.ZoneOffset.UTC)));
        po.setTokenExpiryTimestamp(expiry);
        po.setProgressToken(tokenService.issueProgress(po.getId(), po.getSupplier().getId(), expiry.toInstant(java.time.ZoneOffset.UTC)));
        po.setProgressTokenExpiryTimestamp(expiry);
        message.setHtmlBody(render(po));
        outboxRepository.save(message);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void dispatch(Long outboxId) {
        EmailOutbox outbox = outboxRepository.findById(outboxId).orElse(null);
        if (outbox == null || outbox.getStatus() == EmailOutboxStatus.SENT) return;
        try {
            MimeMessage message = new MimeMessage(mailSession());
            message.setFrom(new InternetAddress(resolveFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(outbox.getRecipient(), true));
            message.setSubject(outbox.getSubject(), "UTF-8");
            message.setContent(outbox.getHtmlBody(), "text/html; charset=UTF-8");
            Transport.send(message);
            outbox.setStatus(EmailOutboxStatus.SENT);
            outbox.setLastError(null);
            PurchaseOrder po = purchaseOrderRepository.findById(outbox.getPurchaseOrderId()).orElse(null);
            if (po != null) po.setStatus(PurchaseOrderStatus.COMMUNICATED_TO_SUPPLIER);
        } catch (Exception e) {
            int attempts = outbox.getAttempts() + 1;
            outbox.setAttempts(attempts);
            outbox.setLastError(Objects.toString(e.getMessage(), e.getClass().getSimpleName()).substring(0,
                    Math.min(1000, Objects.toString(e.getMessage(), e.getClass().getSimpleName()).length())));
            outbox.setStatus(attempts >= 10 ? EmailOutboxStatus.FAILED : EmailOutboxStatus.PENDING);
            outbox.setNextAttemptAt(LocalDateTime.now().plusMinutes(Math.min(60, 1L << Math.min(attempts, 6))));
        }
    }

    private String render(PurchaseOrder po) {
        StringBuilder html = new StringBuilder("<html><body><h2>Purchase Order ")
                .append(escape(po.getPoNumber())).append("</h2><table border='1'><tr><th>Product</th><th>Quantity</th><th>Unit cost</th></tr>");
        for (PurchaseOrderItem item : po.getItems()) {
            html.append("<tr><td>").append(escape(item.getProduct().getName())).append("</td><td>")
                    .append(item.getQuantity()).append("</td><td>").append(item.getUnitCost()).append("</td></tr>");
        }
        String link = portalBaseUrl() + "/supplier-confirm.xhtml?token=" + po.getConfirmationToken();
        String progressLink = portalBaseUrl() + "/supplier-progress.xhtml?token=" + po.getProgressToken();
        return html.append("</table><p>Total: ").append(po.getTotal())
                .append("</p><p><a href='").append(escape(link))
                .append("' style='display:inline-block;padding:12px 18px;background:#146c94;color:#fff;text-decoration:none'>Review and acknowledge order</a></p>")
                .append("<p><a href='").append(escape(progressLink))
                .append("' style='display:inline-block;padding:12px 18px;background:#2f855a;color:#fff;text-decoration:none'>Update delivery progress</a></p>")
                .append("<p>This link expires in 72 hours. No action is taken by opening the link.</p></body></html>").toString();
    }

    private String portalBaseUrl() {
        String value = System.getenv("MCMS_SUPPLIER_PORTAL_BASE_URL");
        if (value == null || value.isBlank()) value = System.getProperty("MCMS_SUPPLIER_PORTAL_BASE_URL");
        if (value == null || value.isBlank()) value = "http://duckdns.org";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private Session mailSession() throws NamingException {
        InitialContext context = new InitialContext();
        try { return (Session) context.lookup("java:jboss/mail/Default"); }
        catch (NamingException e) { return (Session) context.lookup("mail/mcms"); }
    }

    private String resolveFromAddress() {
        String value = System.getenv("MCMS_MAIL_FROM");
        if (value == null || value.isBlank()) value = System.getProperty("MCMS_MAIL_FROM");
        if (value == null || value.isBlank()) throw new IllegalStateException("MCMS_MAIL_FROM is required.");
        return value;
    }

    private String escape(String value) {
        return Objects.toString(value, "").replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
