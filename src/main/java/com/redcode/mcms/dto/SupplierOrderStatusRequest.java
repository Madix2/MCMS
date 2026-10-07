package com.redcode.mcms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SupplierOrderStatusRequest {
    @NotNull
    @Pattern(regexp = "SHIPPED|DELAYED")
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
