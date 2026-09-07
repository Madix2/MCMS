package com.redcode.mcms.resource;

import com.redcode.mcms.entity.Category;
import com.redcode.mcms.entity.Department;
import com.redcode.mcms.repository.CategoryRepository;
import com.redcode.mcms.repository.DepartmentRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lightweight lookup endpoints for categories and departments (used by forms
 * and filters throughout the UI). Read-only.
 */
@Path("/lookups")
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class LookupResource {

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    @GET
    @Path("/categories")
    public List<Map<String, Object>> categories() {
        return categoryRepository.findAll().stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/departments")
    public List<Map<String, Object>> departments() {
        return departmentRepository.findAll().stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "name", d.getName()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/payment-methods")
    public List<String> paymentMethods() {
        return List.of("CASH", "CARD", "EFT", "DIGITAL");
    }

    @GET
    @Path("/roles")
    public List<String> roles() {
        return List.of("ADMIN", "MANAGER", "SALES", "INVENTORY", "PROCUREMENT", "FINANCE", "HR", "MARKETING");
    }
}
