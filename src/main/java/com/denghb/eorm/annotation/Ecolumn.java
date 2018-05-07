package com.denghb.eorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Ecolumn {

	/**
	 * 列名
	 */
	String name();

	/**
	 * 是否作为主键
	 */
	boolean primaryKey() default false;
}
