package com.property.system.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.property.system.tenant.TenantContextHolder;
import com.property.system.tenant.TenantHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final TenantHandler tenantHandler;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantHandler));

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {

                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());

                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

                Long tenantId = TenantContextHolder.getTenantId();
                if (tenantId != null) {
                    this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {

                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
