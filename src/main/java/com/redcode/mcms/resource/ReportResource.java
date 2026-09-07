package com.redcode.mcms.resource;

import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.ReportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.Map;

/**
 * Reports REST API. Each report can be returned as JSON or exported as CSV
 * (via the {@code format=csv} query parameter).
 */
@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class ReportResource {

    @Inject
    private ReportService reportService;

    private Response render(String name, LocalDate from, LocalDate to, String format) {
        ReportService.Report report = switch (name.toLowerCase()) {
            case "sales" -> reportService.salesReport(from, to);
            case "inventory" -> reportService.inventoryReport();
            case "low-stock" -> reportService.lowStockReport();
            case "procurement" -> reportService.procurementReport();
            case "supplier" -> reportService.supplierReport();
            case "customer" -> reportService.customerReport();
            case "employee" -> reportService.employeeReport();
            case "financial" -> reportService.financialReport(from, to);
            case "promotion" -> reportService.promotionReport();
            case "stock-movement" -> reportService.stockMovementReport();
            default -> throw new jakarta.ws.rs.BadRequestException("Unknown report: " + name);
        };

        if ("csv".equalsIgnoreCase(format)) {
            String csv = reportService.toCsv(report);
            return Response.ok(csv)
                    .header("Content-Disposition", "attachment; filename=\"" + name + "-report.csv\"")
                    .type("text/csv")
                    .build();
        }
        return Response.ok(Map.of("columns", report.columns, "rows", report.rows)).build();
    }

    @GET
    public Response reports(@QueryParam("name") String name,
                            @QueryParam("from") String from,
                            @QueryParam("to") String to,
                            @QueryParam("format") String format) {
        if (name == null || name.isBlank()) {
            return Response.ok(Map.of("available",
                    new String[]{"sales", "inventory", "low-stock", "procurement", "supplier",
                            "customer", "employee", "financial", "promotion", "stock-movement"})).build();
        }
        LocalDate fromDate = (from == null || from.isBlank()) ? null : LocalDate.parse(from);
        LocalDate toDate = (to == null || to.isBlank()) ? null : LocalDate.parse(to);
        return render(name, fromDate, toDate, format);
    }
}
