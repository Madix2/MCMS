package com.redcode.mcms.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@ApplicationScoped
public class SupplierConfirmationTokenService {
    private static final String PURPOSE = "SUPPLIER_PO_CONFIRMATION";

    public String issue(Long orderId, Long supplierId, Instant expiresAt) {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"purpose\":\"" + PURPOSE + "\",\"orderId\":" + orderId
                + ",\"supplierId\":" + supplierId + ",\"exp\":" + expiresAt.getEpochSecond() + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public Claims verify(String token) {
        try {
            String[] parts = token == null ? new String[0] : token.split("\\.");
            if (parts.length != 3 || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1])
                    .getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) return null;
            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            if (!PURPOSE.equals(stringClaim(json, "purpose"))) return null;
            long expiry = numberClaim(json, "exp");
            if (Instant.now().getEpochSecond() >= expiry) return null;
            return new Claims(numberClaim(json, "orderId"), numberClaim(json, "supplierId"), expiry);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public record Claims(Long orderId, Long supplierId, long expiryEpochSeconds) { }

    private String sign(String input) {
        try {
            String secret = System.getenv("MCMS_JWT_SECRET");
            if (secret == null || secret.isBlank()) secret = System.getProperty("MCMS_JWT_SECRET");
            if (secret == null || secret.length() < 32) throw new IllegalStateException("MCMS_JWT_SECRET is not configured.");
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return encode(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException("Confirmation token signing failed.", e); }
    }

    private long numberClaim(String json, String name) {
        String value = stringClaim(json, name);
        if (value == null) throw new IllegalArgumentException("Missing claim");
        return Long.parseLong(value);
    }

    private String stringClaim(String json, String name) {
        String marker = "\"" + name + "\":";
        int start = json.indexOf(marker);
        if (start < 0) return null;
        start += marker.length();
        if (start < json.length() && json.charAt(start) == '\"') {
            int end = json.indexOf('\"', start + 1);
            return end < 0 ? null : json.substring(start + 1, end);
        }
        int end = json.indexOf(',', start);
        if (end < 0) end = json.indexOf('}', start);
        return end < 0 ? null : json.substring(start, end).trim();
    }

    private String encode(String value) { return encode(value.getBytes(StandardCharsets.UTF_8)); }
    private String encode(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
}
