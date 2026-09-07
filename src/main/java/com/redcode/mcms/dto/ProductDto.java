package com.redcode.mcms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Carrier for creating/updating a {@code Product} and for displaying one.
 * Product price cannot be negative and stock quantity cannot be negative.
 */
public class ProductDto {

    private Long id;

    private String barcode;

    @NotBlank(message = "Product name is required.")
    private String name;

    private Long categoryId;
    private String categoryName;

    private String description;

    @NotNull(message = "Selling price is required.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative.")
    private BigDecimal sellingPrice;

    @NotNull(message = "Cost price is required.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cost price cannot be negative.")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative.")
    private int quantity = 0;

    @Min(value = 0, message = "Minimum stock level cannot be negative.")
    private int minStockLevel = 0;

    @Min(value = 0, message = "Maximum stock level cannot be negative.")
    private int maxStockLevel = 1000;

    private Long supplierId;
    private String supplierName;

    private String status;

    private boolean lowStock;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }
    public int getMaxStockLevel() { return maxStockLevel; }
    public void setMaxStockLevel(int maxStockLevel) { this.maxStockLevel = maxStockLevel; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isLowStock() { return lowStock; }
    public void setLowStock(boolean lowStock) { this.lowStock = lowStock; }
}
