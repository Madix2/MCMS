package com.redcode.mcms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "supplier_product_map")
public class SupplierProductMap implements Serializable {
    @EmbeddedId
    private SupplierProductMapId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @MapsId("supplierId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotBlank
    @Column(name = "supplier_sku", nullable = false, length = 100)
    private String supplierSku;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Min(0)
    @Column(name = "delivery_lead_time_days", nullable = false)
    private int deliveryLeadTimeDays = 3;

    public SupplierProductMapId getId() { return id; }
    public void setId(SupplierProductMapId id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getSupplierSku() { return supplierSku; }
    public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public int getDeliveryLeadTimeDays() { return deliveryLeadTimeDays; }
    public void setDeliveryLeadTimeDays(int deliveryLeadTimeDays) { this.deliveryLeadTimeDays = deliveryLeadTimeDays; }
}
