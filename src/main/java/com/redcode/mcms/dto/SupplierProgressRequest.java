package com.redcode.mcms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SupplierProgressRequest {
    @NotBlank private String token;
    @NotBlank @Pattern(regexp = "SHIPPED|DELAYED") private String status;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
