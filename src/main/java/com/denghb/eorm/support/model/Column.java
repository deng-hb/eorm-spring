package com.denghb.eorm.support.model;

import java.lang.reflect.Field;

/**
 * 列信息
 */
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

    public Column() {
    }

    public Column(String name, Field field) {
        this.name = name;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                ", field=" + field +
                '}';
    }
}