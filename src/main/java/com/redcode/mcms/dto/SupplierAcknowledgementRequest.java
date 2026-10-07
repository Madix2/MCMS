package com.redcode.mcms.dto;

import jakarta.validation.constraints.NotBlank;

public class SupplierAcknowledgementRequest {
    @NotBlank
    private String token;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
