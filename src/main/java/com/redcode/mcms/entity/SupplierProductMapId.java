package com.redcode.mcms.entity;

import java.io.Serializable;
import java.util.Objects;

public class SupplierProductMapId implements Serializable {
    private Long productId;
    private Long supplierId;

    public SupplierProductMapId() { }
    public SupplierProductMapId(Long productId, Long supplierId) {
        this.productId = productId;
        this.supplierId = supplierId;
    }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierProductMapId other)) return false;
        return Objects.equals(productId, other.productId) && Objects.equals(supplierId, other.supplierId);
    }
    @Override public int hashCode() { return Objects.hash(productId, supplierId); }
}
