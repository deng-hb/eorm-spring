package com.denghb.eorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EColumn {

    /**
     * 列名
     */
    String name();

    /**
     * 是否作为主键
     */
    boolean primaryKey() default false;

    /**
     * 是否自增
     */
    boolean autoIncrement() default false;

    /**
     * 允许空值
     */
    boolean allowNull() default true;

    /**
     * 字符串长度
     */
    int length() default 0;

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * 描述
     */
    String comment() default "";
}
