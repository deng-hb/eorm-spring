/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.template;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析book.*的所有对应的列表
 *
 * @author denghongbing
 * @date 2020/07/19 02:05
 */
public class EAsteriskColumn {

    private String tsql;

    private Map<String, Field> fields = new HashMap<>();

    public String getTsql() {
        return tsql;
    }

    public void setTsql(String tsql) {
        this.tsql = tsql;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public void setFields(Map<String, Field> fields) {
        this.fields = fields;
    }
}
