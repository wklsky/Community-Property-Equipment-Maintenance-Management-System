package com.property.system.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class TenantHandler implements TenantLineHandler {

    private static final Set<String> IGNORE_TABLES = new HashSet<>(Arrays.asList(
            "sys_tenant",
            "sys_permission",
            "sys_role_permission",
            "sys_user_role",
            "repair_order_flow",
            "sys_dict",
            "sys_dict_item"
    ));

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            log.warn("租户上下文为空，使用默认租户ID(-1)防止数据泄露");

            return new LongValue(-1L);
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {

        String lowerTableName = tableName.toLowerCase();

        if (IGNORE_TABLES.contains(lowerTableName)) {
            return true;
        }

        if (TenantContextHolder.isIgnoreTenant()) {
            log.debug("当前线程忽略租户过滤，表: {}", tableName);
            return true;
        }

        return false;
    }

}
