package com.redcode.mcms.resource;

import com.redcode.mcms.dto.SupplierAcknowledgementRequest;
import com.redcode.mcms.service.SupplierConfirmationService;
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

    @POST
    @Path("/acknowledge-order")
    public Response acknowledge(@Valid SupplierAcknowledgementRequest request) {
        confirmationService.acknowledge(request.getToken());
        return Response.ok(java.util.Map.of("message", "Purchase order acknowledged.")).build();
    }
}
