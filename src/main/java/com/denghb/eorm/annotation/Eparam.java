package com.denghb.eorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注解
 *
 * @author denghb
 * @since 2019-07-13 22:38
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Eparam {
    /**
     * 参数名称
     *
     * @return value
     */
    String name();
}
