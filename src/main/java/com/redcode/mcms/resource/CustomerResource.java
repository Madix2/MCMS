package com.redcode.mcms.resource;

import com.redcode.mcms.dto.CustomerDto;
import com.redcode.mcms.dto.SaleDto;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.CustomerService;
import com.redcode.mcms.service.SalesService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Customer management REST API, including purchase history and loyalty points.
 */
@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class CustomerResource {

    @Inject
    private CustomerService customerService;

    @Inject
    private SalesService salesService;

    @GET
    public List<CustomerDto> list(@QueryParam("q") String q) {
        return customerService.list(q);
    }

    @GET
    @Path("/{id}")
    public CustomerDto get(@PathParam("id") Long id) {
        return customerService.find(id);
    }

    @POST
    public Response create(@Valid CustomerDto dto) {
        return Response.status(Response.Status.CREATED).entity(customerService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public CustomerDto update(@PathParam("id") Long id, @Valid CustomerDto dto) {
        return customerService.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response deactivate(@PathParam("id") Long id) {
        customerService.deactivate(id);
        return Response.ok(Map.of("message", "Customer deactivated.")).build();
    }

    @GET
    @Path("/{id}/purchases")
    public List<SaleDto> purchases(@PathParam("id") Long id) {
        return salesService.listByCustomer(id);
    }
}
