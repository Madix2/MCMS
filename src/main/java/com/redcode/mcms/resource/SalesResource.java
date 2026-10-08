package com.redcode.mcms.resource;

import com.redcode.mcms.dto.SaleDto;
import com.redcode.mcms.dto.SaleRequest;
import com.redcode.mcms.dto.PageResponse;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.SalesService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.time.LocalDate;

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
    public PageResponse<SaleDto> list(@QueryParam("q") String q, @QueryParam("from") String from,
                                      @QueryParam("to") String to,
                                      @DefaultValue("0") @QueryParam("page") int page,
                                      @DefaultValue("25") @QueryParam("size") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new com.redcode.mcms.exception.BusinessException("page must be >= 0 and size must be between 1 and 100.");
        }
        try {
            return salesService.page(q, parseDate(from), parseDate(to), page, size);
        } catch (java.time.format.DateTimeParseException e) {
            throw new com.redcode.mcms.exception.BusinessException("Dates must use YYYY-MM-DD format.");
        }
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
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
