package com.denghb.eorm.support.domain;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * 列信息
 */
@Data
public class EColumnRef {

    /**
     * 列名
     * user_name
     */
    private String name;

    /**
     * 字段信息
     */
    private Field field;

}