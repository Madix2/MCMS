package com.redcode.mcms.resource;

import com.redcode.mcms.exception.UnauthorizedException;
import com.redcode.mcms.security.JwtUtil;
import com.redcode.mcms.security.VendorSecured;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Map;

@Provider
@VendorSecured
@Priority(Priorities.AUTHENTICATION)
public class VendorSecurityFilter implements ContainerRequestFilter {
    @Inject private JwtUtil jwtUtil;
    @Override public void filter(ContainerRequestContext context) throws IOException {
        String header = context.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) throw new UnauthorizedException("Vendor authentication is required.");
        Map<String, String> claims = jwtUtil.validateToken(header.substring(7));
        if (claims == null || claims.get("supplierId") == null
                || !"SUPPLIER".equalsIgnoreCase(claims.get("role"))) {
            throw new UnauthorizedException("A supplier identity claim and SUPPLIER role are required.");
        }
        try { context.setProperty("supplierId", Long.valueOf(claims.get("supplierId"))); }
        catch (NumberFormatException e) { throw new UnauthorizedException("Supplier identity claim is invalid."); }
    }
}
