package com.redcode.mcms.websocket;

import com.redcode.mcms.security.JwtUtil;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

public class ApprovalWebSocketConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig configuration, HandshakeRequest request,
                                HandshakeResponse response) {
        List<String> authorization = request.getHeaders().get("Authorization");
        if (authorization == null || authorization.isEmpty()) return;
        String header = authorization.get(0);
        if (!header.startsWith("Bearer ")) return;
        Map<String, String> claims = new JwtUtil().validateToken(header.substring(7));
        if (claims != null) configuration.getUserProperties().put("approvalRole", claims.get("role"));
    }
}
