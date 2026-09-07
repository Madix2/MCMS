package com.redcode.mcms.resource;

import com.redcode.mcms.entity.AuditLog;
import com.redcode.mcms.repository.AuditLogRepository;
import com.redcode.mcms.security.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Audit log REST API (read-only), restricted to managers and administrators.
 */
@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class AuditResource {

    @Inject
    private AuditLogRepository auditLogRepository;

    @GET
    public List<Map<String, Object>> list(@DefaultValue("100") @QueryParam("limit") int limit) {
        return auditLogRepository.findRecent(limit).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(AuditLog log) {
        return Map.of(
                "id", log.getId(),
                "username", log.getUsername(),
                "action", log.getAction(),
                "entity", log.getEntity(),
                "entityId", log.getEntityId() == null ? "" : log.getEntityId(),
                "timestamp", log.getTimestamp().toString(),
                "description", log.getDescription());
    }
}
