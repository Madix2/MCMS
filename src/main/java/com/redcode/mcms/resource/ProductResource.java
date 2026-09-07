package com.redcode.mcms.resource;

import com.redcode.mcms.dto.ProductDto;
import com.redcode.mcms.dto.StockMovementDto;
import com.redcode.mcms.entity.StockMovement;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.ProductService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory REST API:
 *
 *  GET    /api/products                    list/search products
 *  POST   /api/products                    create product
 *  GET    /api/products/{id}               product detail
 *  PUT    /api/products/{id}               update product
 *  DELETE /api/products/{id}               deactivate product
 *  GET    /api/products/low-stock          low stock products
 *  POST   /api/products/{id}/adjust        manual stock adjustment
 *  GET    /api/products/{id}/movements     stock movement history
 */
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class ProductResource {

    @Inject
    private ProductService productService;

    @GET
    public List<ProductDto> list(@QueryParam("q") String q, @QueryParam("category") Long categoryId,
                                 @QueryParam("supplierId") Long supplierId) {
        return productService.list(q, categoryId, supplierId);
    }

    @GET
    @Path("/low-stock")
    public List<ProductDto> lowStock() {
        return productService.lowStock();
    }

    @GET
    @Path("/{id}")
    public ProductDto get(@PathParam("id") Long id) {
        return productService.find(id);
    }

    @POST
    public Response create(@Valid ProductDto dto) {
        ProductDto created = productService.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public ProductDto update(@PathParam("id") Long id, @Valid ProductDto dto) {
        return productService.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        productService.delete(id);
        return Response.ok(Map.of("message", "Product deactivated.")).build();
    }

    @POST
    @Path("/{id}/adjust")
    public ProductDto adjust(@PathParam("id") Long id, Map<String, Integer> body) {
        Integer delta = body.get("delta");
        String reason = body.get("reason") != null ? body.get("reason").toString() : "Manual adjustment";
        return productService.adjustStock(id, delta == null ? 0 : delta, reason);
    }

    @GET
    @Path("/{id}/movements")
    public List<StockMovementDto> movements(@PathParam("id") Long id) {
        return productService.movements(id).stream().map(this::toDto).collect(Collectors.toList());
    }

    private StockMovementDto toDto(StockMovement m) {
        StockMovementDto dto = new StockMovementDto();
        dto.setId(m.getId());
        dto.setProductId(m.getProduct().getId());
        dto.setProductName(m.getProduct().getName());
        dto.setType(m.getType().name());
        dto.setQuantityChanged(m.getQuantityChanged());
        dto.setBalanceAfter(m.getBalanceAfter());
        dto.setReference(m.getReference());
        dto.setNotes(m.getNotes());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
