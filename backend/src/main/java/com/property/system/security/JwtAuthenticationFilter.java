package com.property.system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.system.common.ResultCode;
import com.property.system.dto.Result;
import com.property.system.tenant.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/v1/auth/**",
            "/api/v1/public/**",
            "/api/v1/files/static/**",
            "/api/v1/qrcode/**",
            "/error",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (isWhiteListed(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = resolveToken(request);

            if (StringUtils.hasText(token)) {
                if (jwtTokenProvider.validateToken(token)) {

                    if (jwtTokenProvider.isRefreshToken(token)) {
                        log.warn("尝试使用刷新Token访问API - URI: {}", requestUri);
                        sendErrorResponse(response, ResultCode.TOKEN_INVALID, "无效的访问Token");
                        return;
                    }

                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    Long userId = jwtTokenProvider.getUserId(token);
                    Long tenantId = jwtTokenProvider.getTenantId(token);
                    boolean isSuperAdmin = jwtTokenProvider.getIsSuperAdmin(token);

                    request.setAttribute("userId", userId);
                    request.setAttribute("tenantId", tenantId);
                    request.setAttribute("role", jwtTokenProvider.getRole(token));
                    request.setAttribute("isSuperAdmin", isSuperAdmin);

                    TenantContextHolder.setUserId(userId);
                    TenantContextHolder.setTenantId(tenantId);
                    if (isSuperAdmin) {
                        TenantContextHolder.setIgnoreTenant(true);
                    }

                    log.debug("用户认证成功 - userId: {}, tenantId: {}, isSuperAdmin: {}, URI: {}", userId, tenantId, isSuperAdmin, requestUri);
                } else {
                    log.debug("Token验证失败 - URI: {}", requestUri);

                }
            } else {
                log.debug("请求未携带Token - URI: {}", requestUri);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT认证过程发生异常 - URI: {}", requestUri, e);
            sendErrorResponse(response, ResultCode.INTERNAL_ERROR, "认证过程发生异常");
        } finally {

            SecurityContextHolder.clearContext();
            TenantContextHolder.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private boolean isWhiteListed(String requestUri) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    private void sendErrorResponse(HttpServletResponse response, ResultCode resultCode, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Result<?> result = Result.error(resultCode.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
