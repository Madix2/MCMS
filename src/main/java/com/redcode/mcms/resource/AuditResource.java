package com.redcode.mcms.resource;

import com.redcode.mcms.entity.AuditLog;
import com.redcode.mcms.dto.PageResponse;
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
    public PageResponse<Map<String, Object>> list(@DefaultValue("0") @QueryParam("page") int page,
                                                  @DefaultValue("25") @QueryParam("size") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new com.redcode.mcms.exception.BusinessException("page must be >= 0 and size must be between 1 and 100.");
        }
        List<Map<String, Object>> items = auditLogRepository.findPage(page, size).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return new PageResponse<>(items, page, size, auditLogRepository.countAll());
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
