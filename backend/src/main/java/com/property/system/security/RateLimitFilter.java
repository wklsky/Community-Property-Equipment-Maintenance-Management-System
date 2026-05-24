package com.property.system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.system.dto.Result;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RateLimitFilter implements Filter {

    private static final int LOGIN_MAX = 10;
    private static final int API_MAX = 120;
    private static final long WINDOW_MS = 60_000;
    private static final long CLEANUP_INTERVAL_MS = 300_000;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile long lastCleanup = System.currentTimeMillis();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        int maxAttempts;
        if (path.startsWith("/api/v1/auth/login")) {
            maxAttempts = LOGIN_MAX;
        } else if (path.startsWith("/api/v1/")) {
            maxAttempts = API_MAX;
        } else {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(httpRequest);
        String key = ip + ":" + path;

        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());

        synchronized (counter) {
            long now = System.currentTimeMillis();
            if (now - counter.windowStart > WINDOW_MS) {
                counter.windowStart = now;
                counter.count = 0;
            }

            if (counter.count >= maxAttempts) {
                log.warn("Rate limit exceeded - IP: {}, path: {}, count: {}", ip, path, counter.count);
                httpResponse.setStatus(429);
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write(
                        objectMapper.writeValueAsString(Result.error(429, "请求过于频繁，请稍后再试")));
                return;
            }
            counter.count++;
        }

        if (now() - lastCleanup > CLEANUP_INTERVAL_MS) {
            cleanupStaleEntries();
            lastCleanup = now();
        }

        chain.doFilter(request, response);
    }

    private void cleanupStaleEntries() {
        long threshold = System.currentTimeMillis() - WINDOW_MS;
        Iterator<Map.Entry<String, WindowCounter>> it = counters.entrySet().iterator();
        int removed = 0;
        while (it.hasNext()) {
            Map.Entry<String, WindowCounter> entry = it.next();
            synchronized (entry.getValue()) {
                if (entry.getValue().windowStart < threshold) {
                    it.remove();
                    removed++;
                }
            }
        }
        if (removed > 0) {
            log.debug("Cleaned up {} stale rate-limit entries", removed);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private static class WindowCounter {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }
}
