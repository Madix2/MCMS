package com.redcode.mcms.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TokenRevocationService {
    private final ConcurrentHashMap<String, Long> revoked = new ConcurrentHashMap<>();

    public void revoke(String token, Map<String, String> claims) {
        long expiry = claims == null ? Instant.now().plusSeconds(28800).getEpochSecond()
                : Long.parseLong(claims.getOrDefault("exp", Long.toString(Instant.now().plusSeconds(28800).getEpochSecond())));
        revoked.put(fingerprint(token), expiry);
    }

    public boolean isRevoked(String token) {
        long now = Instant.now().getEpochSecond();
        String key = fingerprint(token);
        revoked.entrySet().removeIf(entry -> entry.getValue() <= now);
        return revoked.containsKey(key);
    }

    private String fingerprint(String token) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Token fingerprinting failed.", e);
        }
    }
}
