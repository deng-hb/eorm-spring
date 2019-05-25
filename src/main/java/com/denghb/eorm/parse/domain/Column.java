package com.denghb.eorm.parse.domain;

import java.lang.reflect.Field;

/**
 * 列信息
 */
public class Column {

    /**
     * 列名
     */
    private String name;

    /**
     * 是否主键
     */
    private Boolean primaryKey;

    /**
     * 字段信息
     */
    private Field field;

    public Column(String name, Boolean primaryKey, Field field) {
        this.name = name;
        this.primaryKey = primaryKey;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", primaryKey=" + primaryKey +
                ", field=" + field +
                '}';
    }
}