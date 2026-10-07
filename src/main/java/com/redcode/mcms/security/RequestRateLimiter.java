package com.redcode.mcms.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RequestRateLimiter {
    private static final long WINDOW_SECONDS = 60;
    private static final int MAX_REQUESTS = 120;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        long window = Instant.now().getEpochSecond() / WINDOW_SECONDS;
        Window current = windows.compute(key, (ignored, previous) -> {
            if (previous == null || previous.window != window) return new Window(window, 1);
            return new Window(window, previous.count + 1);
        });
        if (windows.size() > 10000) windows.entrySet().removeIf(entry -> entry.getValue().window < window - 2);
        return current.count <= MAX_REQUESTS;
    }

    private record Window(long window, int count) { }
}
