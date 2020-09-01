package com.denghb.eorm.support.domain;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * 列信息
 */
@Data
public class Column {

    /**
     * 列名
     * user_name
     */
    private String name;

    /**
     * 字段信息
     */
    private Field field;

    /**
     * 允许空值
     */
    private boolean allowNull;

    /**
     * 字符串长度
     */
    private int charMaxLength;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 描述
     */
    private String comment;

}