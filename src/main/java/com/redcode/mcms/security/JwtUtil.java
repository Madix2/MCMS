package com.redcode.mcms.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

/**
 * Minimal dependency-free JWT (JSON Web Token) implementation.
 *
 * The token carries the user id, username and role, and is signed with
 * HMAC-SHA256 using a secret configured via the environment variable
 * {@code MCMS_JWT_SECRET} (a random fallback is used if not set).
 *
 * Keeping this dependency-free makes the application portable across
 * application servers without extra libraries.
 */
@ApplicationScoped
public class JwtUtil {

    public static final String SECRET_ENV = "MCMS_JWT_SECRET";
    private static final String FALLBACK_SECRET = "mcms-default-dev-secret-change-in-production-2026";
    private static final long EXPIRY_MILLIS = 1000L * 60L * 60L * 8L; // 8 hours

    private String secret;

    private String resolveSecret() {
        if (secret == null) {
            String fromEnv = System.getenv(SECRET_ENV);
            secret = (fromEnv == null || fromEnv.isBlank()) ? FALLBACK_SECRET : fromEnv;
        }
        return secret;
    }

    public String createToken(Long userId, String username, String role) {
        long now = System.currentTimeMillis();
        String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = b64("{\"sub\":" + userId
                + ",\"user\":\"" + username
                + "\",\"role\":\"" + role
                + "\",\"iat\":" + now
                + ",\"exp\":" + (now + EXPIRY_MILLIS) + "}");
        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    /**
     * Validates the token signature and expiry. Returns the decoded claims,
     * or null if the token is invalid/expired.
     */
    public Map<String, String> validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                return null;
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long exp = extractNumber(payloadJson, "exp");
            if (System.currentTimeMillis() >= exp) {
                return null;
            }
            return Map.of(
                    "sub", Long.toString(extractNumber(payloadJson, "sub")),
                    "user", extractString(payloadJson, "user"),
                    "role", extractString(payloadJson, "role")
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String input) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(resolveSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return b64(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("JWT signing failed", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private String b64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String b64(String value) {
        return b64(value.getBytes(StandardCharsets.UTF_8));
    }

    private long extractNumber(String json, String key) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx < 0) {
            return 0;
        }
        int start = idx + token.length();
        int end = json.indexOf(',', start);
        if (end < 0) {
            end = json.indexOf('}', start);
        }
        try {
            return Long.parseLong(json.substring(start, end).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String extractString(String json, String key) {
        String token = "\"" + key + "\":\"";
        int idx = json.indexOf(token);
        if (idx < 0) {
            return "";
        }
        int start = idx + token.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end < 0 ? json.length() : end);
    }
}
