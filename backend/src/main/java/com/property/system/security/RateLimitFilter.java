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
import java.util.regex.Pattern;

@Slf4j
public class RateLimitFilter implements Filter {

    private static final int LOGIN_MAX = 10;
    private static final int API_MAX = 120;
    private static final long WINDOW_MS = 60_000;
    private static final long CLEANUP_INTERVAL_MS = 300_000;

    /**
     * 是否信任反向代理透传的 X-Forwarded-For。
     * 直接置为 true 会让攻击者通过伪造该头任意切换限流计数桶，使登录接口的爆破防护完全失效；
     * 仅当服务确实部署在 Nginx / SLB 之后时才开启，并配合 TRUSTED_PROXY_PATTERN 收敛可信来源。
     */
    private static final boolean TRUST_PROXY = false;

    // 仅内网网段可作为可信代理；开启 TRUST_PROXY 后，非内网来源的请求一律忽略 XFF
    private static final Pattern TRUSTED_PROXY_PATTERN = Pattern.compile(
            "^(127\\.0\\.0\\.1|localhost|0:0:0:0:0:0:0:1|::1|10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"
                    + "|192\\.168\\.\\d{1,3}\\.\\d{1,3}|172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3})$");

    // RESTful 路径中的 UUID 段，归一化后避免遍历不同资源绕过限流
    private static final Pattern UUID_SEGMENT = Pattern.compile(
            "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?=/|$)");

    // RESTful 路径中的数字主键段，如 /api/v1/repair-orders/12345
    private static final Pattern NUMERIC_SEGMENT = Pattern.compile("/\\d+(?=/|$)");

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

        // 使用归一化后的路由模板作为计数维度：否则 /repair-orders/1、/repair-orders/2 会各占一个桶，
        // 攻击者遍历主键即可把 API_MAX 的限制抬高到任意倍数
        String key = ip + ":" + normalizePath(path);

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

    /**
     * 将 URI 中的资源标识替换为占位符，使同一路由模板共享一个限流桶。
     * /api/v1/repair-orders/12345 -> /api/v1/repair-orders/{id}
     */
    private String normalizePath(String uri) {
        String normalized = UUID_SEGMENT.matcher(uri).replaceAll("/{id}");
        return NUMERIC_SEGMENT.matcher(normalized).replaceAll("/{id}");
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // 只有来自可信代理（内网/本机）的连接才允许解析 XFF。
        // 否则攻击者每次请求伪造一个 X-Forwarded-For 就能获得全新的计数桶，
        // LOGIN_MAX=10 的防爆破限制等同于不存在。
        if (TRUST_PROXY && remoteAddr != null && TRUSTED_PROXY_PATTERN.matcher(remoteAddr).matches()) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isEmpty()) {
                return xff.split(",")[0].trim();
            }
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp.trim();
            }
        }
        return remoteAddr;
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private static class WindowCounter {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }
}
