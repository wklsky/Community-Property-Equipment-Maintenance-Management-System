package com.property.system.security;

/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 11:05
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 11:05
 * @FilePath: backend/src/main/java/com/property/system/security/SecurityWhitelist.java
 * @Description: 免认证路径白名单，供 Spring Security 与 JWT 过滤器共用
 */

/**
 * 此前 SecurityConfig 的 permitAll 与 JwtAuthenticationFilter.WHITE_LIST 各维护一份相同列表。
 * 两处一旦漂移会出现「Security 已放行但 JWT 过滤器仍拦截」的 401，或反之导致鉴权被绕过，
 * 故收敛为单一来源。
 */
public final class SecurityWhitelist {

    /**
     * 无需携带 Token 即可访问的业务路径。
     * 新增公开接口时必须在此登记，并同步确认不会泄漏租户/用户数据。
     */
    public static final String[] PERMIT_ALL_PATTERNS = {
            "/api/v1/auth/**",
            "/api/v1/public/**",
            "/api/v1/files/static/**",
            "/api/v1/qrcode/**"
    };

    /**
     * 框架级端点，仅 JWT 过滤器需要跳过（Spring Security 对它们有默认处理，无需在此声明）
     */
    public static final String[] FRAMEWORK_PATTERNS = {
            "/error",
            "/favicon.ico"
    };

    private SecurityWhitelist() {
    }
}
