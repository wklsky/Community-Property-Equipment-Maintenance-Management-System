package com.property.system.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    String[] value();

    Logical logical() default Logical.OR;

    enum Logical {
        AND, OR
    }
}
