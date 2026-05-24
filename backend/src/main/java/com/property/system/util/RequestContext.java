package com.property.system.util;

import com.property.system.tenant.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestContext {

    public static Long getUserId() {

        Long userId = TenantContextHolder.getUserId();
        if (userId != null) {
            return userId;
        }

        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userIdAttr = request.getAttribute("userId");
            return userIdAttr != null ? (Long) userIdAttr : null;
        }
        return null;
    }

    public static Long getTenantId() {

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }

        HttpServletRequest request = getRequest();
        if (request != null) {
            Object tenantIdAttr = request.getAttribute("tenantId");
            return tenantIdAttr != null ? (Long) tenantIdAttr : null;
        }
        return null;
    }

    public static String getRole() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object roleAttr = request.getAttribute("role");
            return roleAttr != null ? roleAttr.toString() : null;
        }
        return null;
    }

    public static boolean isSuperAdmin() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object attr = request.getAttribute("isSuperAdmin");
            return attr != null && (Boolean) attr;
        }
        return false;
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
