package com.redcode.mcms.resource;

import com.redcode.mcms.dto.AttendanceDto;
import com.redcode.mcms.dto.EmployeeDto;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.EmployeeService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Human Resources REST API: employees and attendance.
 * Sensitive employee data is restricted to HR, managers and administrators
 * (enforced in the service layer).
 */
@Path("/hr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class HrResource {

    @Inject
    private EmployeeService employeeService;

    @GET
    @Path("/employees")
    public List<EmployeeDto> employees(@QueryParam("q") String q) {
        return employeeService.list(q);
    }

    @GET
    @Path("/employees/{id}")
    public EmployeeDto employee(@PathParam("id") Long id) {
        return employeeService.find(id);
    }

    @POST
    @Path("/employees")
    public Response createEmployee(@Valid EmployeeDto dto) {
        return Response.status(Response.Status.CREATED).entity(employeeService.create(dto)).build();
    }

    @PUT
    @Path("/employees/{id}")
    public EmployeeDto updateEmployee(@PathParam("id") Long id, @Valid EmployeeDto dto) {
        return employeeService.update(id, dto);
    }

    @GET
    @Path("/attendance")
    public List<AttendanceDto> attendance(@QueryParam("employeeId") Long employeeId,
                                          @QueryParam("date") String date) {
        LocalDate d = date == null ? null : LocalDate.parse(date);
        return employeeService.listAttendance(employeeId, d);
    }

    @POST
    @Path("/attendance")
    public AttendanceDto recordAttendance(@Valid AttendanceDto dto) {
        return employeeService.recordAttendance(dto);
    }

    @GET
    @Path("/meta")
    public Map<String, Object> meta() {
        return Map.of("roles", List.of("ADMIN", "MANAGER", "SALES", "INVENTORY", "PROCUREMENT", "FINANCE", "HR", "MARKETING"));
    }
}
