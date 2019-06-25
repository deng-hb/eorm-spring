package com.denghb.eorm.support.domain;

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

    /**
     * 是否自增
     */
    private boolean autoIncrement;

    /**
     * 允许空值
     */
    private boolean allowNull;

    /**
     * 字符串长度
     */
    private int length;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 描述
     */
    private String comment;

    public Column() {
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

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isAllowNull() {
        return allowNull;
    }

    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", field=" + field +
                ", autoIncrement=" + autoIncrement +
                ", allowNull=" + allowNull +
                ", length=" + length +
                ", defaultValue='" + defaultValue + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}