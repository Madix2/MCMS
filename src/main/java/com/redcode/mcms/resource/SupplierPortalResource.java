package com.redcode.mcms.resource;

import com.redcode.mcms.dto.SupplierAcknowledgementRequest;
import com.redcode.mcms.service.SupplierConfirmationService;
import com.redcode.mcms.service.SupplierProgressService;
import com.redcode.mcms.dto.SupplierProgressRequest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/supplier-portal")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SupplierPortalResource {
    @Inject private SupplierConfirmationService confirmationService;
    @Inject private SupplierProgressService progressService;

    @POST
    @Path("/acknowledge-order")
    public Response acknowledge(@Valid SupplierAcknowledgementRequest request) {
        confirmationService.acknowledge(request.getToken());
        return Response.ok(java.util.Map.of("message", "Purchase order acknowledged.")).build();
    }

    @POST
    @Path("/update-progress")
    public Response updateProgress(@Valid SupplierProgressRequest request) {
        progressService.update(request.getToken(), request.getStatus());
        return Response.ok(java.util.Map.of("message", "Purchase order progress updated.")).build();
    }
}
