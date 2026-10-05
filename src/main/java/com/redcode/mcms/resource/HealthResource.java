package com.redcode.mcms.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;
import java.util.Map;

/** Lightweight liveness endpoint for VM, reverse proxy, and uptime checks. */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @PersistenceContext
    private EntityManager entityManager;

    @GET
    public Response health() {
        return Response.ok(Map.of("status", "UP", "service", "mcms", "timestamp", Instant.now().toString()))
                .build();
    }

    @GET
    @Path("/ready")
    public Response ready() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return Response.ok(Map.of("status", "READY", "service", "mcms")).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("status", "NOT_READY", "service", "mcms"))
                    .build();
        }
    }
}
