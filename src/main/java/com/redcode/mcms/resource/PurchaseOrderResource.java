package com.redcode.mcms.resource;

import com.redcode.mcms.dto.PurchaseOrderDto;
import com.redcode.mcms.dto.PurchaseOrderRequest;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.PurchaseOrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Procurement REST API.
 *
 *  POST /api/purchase-orders                create a draft PO
 *  GET  /api/purchase-orders                list POs (filter by status)
 *  GET  /api/purchase-orders/{id}
 *  POST /api/purchase-orders/{id}/submit    submit for approval
 *  PUT  /api/purchase-orders/{id}/approve   manager approval
 *  PUT  /api/purchase-orders/{id}/reject    manager rejection
 *  POST /api/purchase-orders/{id}/order     mark as ordered
 *  POST /api/purchase-orders/{id}/receive   receive order -> inventory update
 */
@Path("/purchase-orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class PurchaseOrderResource {

    @Inject
    private PurchaseOrderService purchaseOrderService;

    @GET
    public List<PurchaseOrderDto> list(@QueryParam("status") String status) {
        return purchaseOrderService.list(status);
    }

    @GET
    @Path("/pending-approval")
    public List<PurchaseOrderDto> pendingApproval() {
        return purchaseOrderService.listPendingApproval();
    }

    @GET
    @Path("/{id}")
    public PurchaseOrderDto get(@PathParam("id") Long id) {
        return purchaseOrderService.find(id);
    }

    @POST
    public Response create(@Valid PurchaseOrderRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(purchaseOrderService.create(request)).build();
    }

    @POST
    @Path("/{id}/submit")
    public PurchaseOrderDto submit(@PathParam("id") Long id) {
        return purchaseOrderService.submitForApproval(id);
    }

    @PUT
    @Path("/{id}/approve")
    public PurchaseOrderDto approve(@PathParam("id") Long id) {
        return purchaseOrderService.approve(id);
    }

    @PUT
    @Path("/{id}/reject")
    public PurchaseOrderDto reject(@PathParam("id") Long id, Map<String, String> body) {
        return purchaseOrderService.reject(id, body == null ? null : body.get("reason"));
    }

    @POST
    @Path("/{id}/order")
    public PurchaseOrderDto order(@PathParam("id") Long id) {
        return purchaseOrderService.markOrdered(id);
    }

    @POST
    @Path("/{id}/receive")
    public PurchaseOrderDto receive(@PathParam("id") Long id) {
        return purchaseOrderService.receive(id);
    }
}
