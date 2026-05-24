package com.property.system.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    String[] value();

    RequirePermission.Logical logical() default RequirePermission.Logical.OR;
}
