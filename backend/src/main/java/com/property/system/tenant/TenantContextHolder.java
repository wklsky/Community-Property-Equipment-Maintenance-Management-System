package com.property.system.tenant;

public class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IGNORE_TENANT = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setIgnoreTenant(boolean ignore) {
        IGNORE_TENANT.set(ignore);
    }

    public static boolean isIgnoreTenant() {
        Boolean ignore = IGNORE_TENANT.get();
        return ignore != null && ignore;
    }

    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        IGNORE_TENANT.remove();
    }

    public static void runWithoutTenant(Runnable runnable) {
        Boolean previousIgnore = IGNORE_TENANT.get();
        try {
            IGNORE_TENANT.set(true);
            runnable.run();
        } finally {
            if (previousIgnore == null) {
                IGNORE_TENANT.remove();
            } else {
                IGNORE_TENANT.set(previousIgnore);
            }
        }
    }

    public static void runWithTenant(Long tenantId, Runnable runnable) {
        Long previousTenantId = TENANT_ID.get();
        try {
            TENANT_ID.set(tenantId);
            runnable.run();
        } finally {
            if (previousTenantId == null) {
                TENANT_ID.remove();
            } else {
                TENANT_ID.set(previousTenantId);
            }
        }
    }
}
