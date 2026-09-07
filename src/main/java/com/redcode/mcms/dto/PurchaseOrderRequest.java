package com.redcode.mcms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for creating a purchase order (list of product quantities).
 */
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier is required.")
    private Long supplierId;

    @NotEmpty(message = "A purchase order must contain at least one product.")
    private List<PoItem> items = new ArrayList<>();

    private String notes;

    public static class PoItem {
        @NotNull(message = "Product is required.")
        private Long productId;

        @Min(value = 1, message = "Quantity must be greater than zero.")
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public List<PoItem> getItems() { return items; }
    public void setItems(List<PoItem> items) { this.items = items; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
