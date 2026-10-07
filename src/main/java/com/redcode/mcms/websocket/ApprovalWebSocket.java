package com.redcode.mcms.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/ws/approvals", configurator = ApprovalWebSocketConfigurator.class)
public class ApprovalWebSocket {
    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void open(Session session) throws IOException {
        String role = (String) session.getUserProperties().get("approvalRole");
        if (session.getUserPrincipal() == null || (!"MANAGER".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role) && !"ADMINISTRATOR".equalsIgnoreCase(role))) {
            session.close();
            return;
        }
        SESSIONS.add(session);
    }

    @OnClose
    public void close(Session session) { SESSIONS.remove(session); }

    public static void broadcastPendingApproval(Long id, String poNumber, String supplier, String total) {
        String json = "{\"event\":\"PENDING_APPROVAL\",\"id\":" + id + ",\"poNumber\":\""
                + jsonEscape(poNumber) + "\",\"supplier\":\"" + jsonEscape(supplier)
                + "\",\"total\":\"" + jsonEscape(total) + "\"}";
        for (Session session : SESSIONS) {
            if (session.isOpen()) session.getAsyncRemote().sendText(json);
        }
    }

    public static void broadcastSupplierAcknowledged(Long id, String poNumber, String supplier) {
        String json = "{\"event\":\"SUPPLIER_ACKNOWLEDGED\",\"id\":" + id
                + ",\"poNumber\":\"" + jsonEscape(poNumber) + "\",\"supplier\":\""
                + jsonEscape(supplier) + "\"}";
        for (Session session : SESSIONS) {
            if (session.isOpen()) session.getAsyncRemote().sendText(json);
        }
    }

    private static String jsonEscape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
