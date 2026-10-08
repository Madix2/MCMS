package com.redcode.mcms.resource;

import com.redcode.mcms.dto.SaleDto;
import com.redcode.mcms.dto.SaleRequest;
import com.redcode.mcms.security.AuthContext;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.exception.ForbiddenException;
import com.redcode.mcms.exception.UnauthorizedException;
import com.redcode.mcms.service.SalesService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/v1/cashier")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
@RolesAllowed("CASHIER")
public class CashierResource {
    @Inject private SalesService salesService;
    @Inject private AuthContext authContext;
    @Context private SecurityContext securityContext;

    @GET
    @Path("/sales")
    public List<SaleDto> history() {
        requireCashier();
        return salesService.listTodayByCashier(cashierId());
    }

    @POST
    @Path("/sales")
    public Response create(@Valid SaleRequest request) {
        requireCashier();
        SaleDto sale = salesService.createSale(request, cashierId());
        return Response.status(Response.Status.CREATED).entity(sale).build();
    }

    private Long cashierId() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new UnauthorizedException("Authenticated cashier identity is required.");
        }
        try { return Long.valueOf(securityContext.getUserPrincipal().getName()); }
        catch (NumberFormatException e) { throw new UnauthorizedException("Authenticated cashier identity is invalid."); }
    }

    private void requireCashier() {
        authContext.requireRole(AuthContext.RolePermission.CASHIER);
        if (securityContext == null || !securityContext.isUserInRole("CASHIER")) {
            throw new ForbiddenException("CASHIER role is required.");
        }
    }
}
