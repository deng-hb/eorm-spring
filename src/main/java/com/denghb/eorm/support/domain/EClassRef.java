package com.denghb.eorm.support.domain;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Java Bean 映射 数据库返回信息
 */
@Data
public class EClassRef {

    /**
     * 属性对应表及字段，理论只有两层
     * tableName,
     */
    private Map<String, ESubClassRef> tableMap = new HashMap<>();

    /**
     * 列名对应字段
     * columnName, Field
     */
    private Map<String, Field> columnMap = new HashMap<>();

    @Data
    public static class ESubClassRef {

        private Field field;

        private Map<String, Field> columnMap = new HashMap<>();
    }
}
