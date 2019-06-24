package com.denghb.eorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ETable {

    /**
     * 表名
     */
    String name();

    /**
     * 数据库
     */
    String database() default "";

    /**
     * 描述
     */
    String comment() default "";

}
