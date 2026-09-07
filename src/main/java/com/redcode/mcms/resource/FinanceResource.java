package com.redcode.mcms.resource;

import com.redcode.mcms.dto.FinanceSummaryDto;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.FinanceService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Finance REST API: revenue, expenses, payment breakdown and net revenue.
 */
@Path("/finance")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class FinanceResource {

    @Inject
    private FinanceService financeService;

    @GET
    @Path("/summary")
    public FinanceSummaryDto summary() {
        return financeService.summary();
    }
}
