package com.redcode.mcms.resource;

import com.redcode.mcms.dto.SaleDto;
import com.redcode.mcms.dto.SaleRequest;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.SalesService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Sales REST API.
 *
 *  POST /api/sales          complete a sale (single transaction)
 *  GET  /api/sales          list/search sales
 *  GET  /api/sales/{id}     sale detail (also used to render a receipt)
 */
@Path("/sales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class SalesResource {

    @Inject
    private SalesService salesService;

    @GET
    public List<SaleDto> list(@QueryParam("q") String q) {
        return salesService.list(q);
    }

    @GET
    @Path("/{id}")
    public SaleDto get(@PathParam("id") Long id) {
        return salesService.find(id);
    }

    @POST
    public Response create(@Valid SaleRequest request) {
        SaleDto created = salesService.createSale(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
