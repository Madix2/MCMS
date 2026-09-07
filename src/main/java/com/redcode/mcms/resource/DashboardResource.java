package com.redcode.mcms.resource;

import com.redcode.mcms.dto.ChartPointDto;
import com.redcode.mcms.dto.DashboardSummaryDto;
import com.redcode.mcms.dto.NotificationDto;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.DashboardService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Central dashboard REST API: role-aware summary cards, charts and activity.
 */
@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class DashboardResource {

    @Inject
    private DashboardService dashboardService;

    @GET
    @Path("/summary")
    public DashboardSummaryDto summary() {
        return dashboardService.summary();
    }

    @GET
    @Path("/top-products")
    public List<ChartPointDto> topProducts() {
        return dashboardService.topSellingProducts(5);
    }

    @GET
    @Path("/inventory-by-category")
    public List<ChartPointDto> inventoryByCategory() {
        return dashboardService.inventoryByCategory();
    }

    @GET
    @Path("/revenue-trend")
    public List<ChartPointDto> revenueTrend(@DefaultValue("14") @QueryParam("days") int days) {
        return dashboardService.revenueTrend(days);
    }

    @GET
    @Path("/activity")
    public List<String> activity(@DefaultValue("10") @QueryParam("limit") int limit) {
        return dashboardService.recentActivity(limit);
    }

    @GET
    @Path("/notifications")
    public List<NotificationDto> notifications(@DefaultValue("10") @QueryParam("limit") int limit) {
        return dashboardService.notifications(limit);
    }
}
