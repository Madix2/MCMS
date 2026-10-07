package com.redcode.mcms.resource;

import com.redcode.mcms.dto.SupplierOrderStatusRequest;
import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.dto.PurchaseOrderDto;
import com.redcode.mcms.service.PurchaseOrderService;
import com.redcode.mcms.security.VendorSecured;
import com.redcode.mcms.service.SupplierOrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/supplier/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@VendorSecured
public class SupplierOrderResource {
    @Inject private SupplierOrderService service;
    @Inject private PurchaseOrderService purchaseOrderService;
    @Context private ContainerRequestContext request;

    @GET public List<PurchaseOrderDto> list() {
        return service.findVisible(supplierId()).stream().map(purchaseOrderService::toDto).toList();
    }

    @PUT @Path("/{id}/status")
    public PurchaseOrderDto update(@PathParam("id") Long id, @Valid SupplierOrderStatusRequest payload) {
        return purchaseOrderService.toDto(service.updateStatus(id, supplierId(), payload.getStatus()));
    }
    private Long supplierId() { return (Long) request.getProperty("supplierId"); }
}
