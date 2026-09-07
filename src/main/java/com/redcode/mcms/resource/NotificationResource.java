package com.redcode.mcms.resource;

import com.redcode.mcms.dto.NotificationDto;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.DashboardService;
import com.redcode.mcms.service.NotificationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Central notification REST API.
 */
@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class NotificationResource {

    @Inject
    private NotificationService notificationService;

    @Inject
    private DashboardService dashboardService;

    @GET
    public List<NotificationDto> list(@DefaultValue("20") @QueryParam("limit") int limit) {
        return dashboardService.notifications(limit);
    }

    @GET
    @Path("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("unread", notificationService.unreadCount());
    }

    @PUT
    @Path("/{id}/read")
    public Response markRead(@PathParam("id") Long id) {
        notificationService.markRead(id);
        return Response.ok(Map.of("message", "Notification marked as read.")).build();
    }
}
