package com.redcode.mcms.service;

import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.entity.PurchaseOrderItem;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import jakarta.ejb.Asynchronous;
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
import java.util.Objects;

@Stateless
public class EmailNotificationService {
    @Inject private PurchaseOrderRepository purchaseOrderRepository;

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendPurchaseOrderToSupplier(PurchaseOrder po) {
        if (po == null || po.getId() == null) throw new IllegalArgumentException("Purchase order is required.");
        PurchaseOrder managed = purchaseOrderRepository.findById(po.getId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found."));
        if (managed.getSupplier() == null || managed.getSupplier().getEmail() == null
                || managed.getSupplier().getEmail().isBlank()) throw new IllegalStateException("Supplier email is not configured.");
        try {
            MimeMessage message = new MimeMessage(mailSession());
            message.setFrom(new InternetAddress(resolveFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(managed.getSupplier().getEmail(), true));
            message.setSubject("MegaMart purchase order " + managed.getPoNumber(), "UTF-8");
            StringBuilder html = new StringBuilder("<html><body><h2>Purchase Order ")
                    .append(escape(managed.getPoNumber())).append("</h2><table border='1'><tr><th>Product</th><th>Quantity</th><th>Unit cost</th></tr>");
            for (PurchaseOrderItem item : managed.getItems()) {
                html.append("<tr><td>").append(escape(item.getProduct().getName())).append("</td><td>")
                        .append(item.getQuantity()).append("</td><td>").append(item.getUnitCost()).append("</td></tr>");
            }
            html.append("</table><p>Total: ").append(managed.getTotal()).append("</p></body></html>");
            message.setContent(html.toString(), "text/html; charset=UTF-8");
            Transport.send(message);
            managed.setStatus(PurchaseOrderStatus.COMMUNICATED_TO_SUPPLIER);
            purchaseOrderRepository.update(managed);
        } catch (Exception e) {
            throw new IllegalStateException("Purchase order email could not be sent.", e);
        }
    }

    private String escape(String value) {
        return Objects.toString(value, "").replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String resolveFromAddress() {
        String configured = System.getenv("MCMS_MAIL_FROM");
        if (configured == null || configured.isBlank()) configured = System.getProperty("MCMS_MAIL_FROM");
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("MCMS_MAIL_FROM must identify a verified Brevo sender.");
        }
        return configured;
    }

    private Session mailSession() throws NamingException {
        InitialContext context = new InitialContext();
        try {
            return (Session) context.lookup("java:jboss/mail/Default");
        } catch (NamingException wildFlyNameNotFound) {
            return (Session) context.lookup("mail/mcms");
        }
    }
}
