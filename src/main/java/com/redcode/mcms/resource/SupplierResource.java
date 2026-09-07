package com.redcode.mcms.resource;

import com.redcode.mcms.dto.SupplierDto;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.SupplierService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Supplier management REST API.
 */
@Path("/suppliers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class SupplierResource {

    @Inject
    private SupplierService supplierService;

    @GET
    public List<SupplierDto> list(@QueryParam("q") String q) {
        return supplierService.list(q);
    }

    @GET
    @Path("/{id}")
    public SupplierDto get(@PathParam("id") Long id) {
        return supplierService.find(id);
    }

    @POST
    public Response create(@Valid SupplierDto dto) {
        return Response.status(Response.Status.CREATED).entity(supplierService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public SupplierDto update(@PathParam("id") Long id, @Valid SupplierDto dto) {
        return supplierService.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response deactivate(@PathParam("id") Long id) {
        supplierService.deactivate(id);
        return Response.ok(Map.of("message", "Supplier deactivated.")).build();
    }
}
