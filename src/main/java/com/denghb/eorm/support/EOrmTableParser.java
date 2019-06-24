package com.denghb.eorm.support;


import com.denghb.eorm.EOrmException;
import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public class EOrmTableParser {

    private static final Map<String, Table> DOMAIN_TABLE_CACHE = new ConcurrentHashMap<String, Table>();

    public static Table load(Class clazz) {
        String key = clazz.getName();
        Table table = DOMAIN_TABLE_CACHE.get(key);
        if (null != table) {
            return table;
        }
        table = new Table();
        table.setName(getTableName(clazz));

        //
        Set<Field> fields = ReflectUtils.getFields(clazz);
        for (Field field : fields) {

            EColumn a = field.getAnnotation(EColumn.class);
            if (null == a) {
                continue;
            }
            boolean primaryKey = a.primaryKey();
            if (primaryKey) {
                if (null != table.getPrimaryKeyColumn()) {
                    throw new EOrmException("exist primary key");
                }
                table.setPrimaryKeyColumn(new Column(a.name(), field, a.autoIncrement(), a.allowNull(), a.length(), a.comment()));
            } else {
                table.getColumns().add(new Column(a.name(), field, a.autoIncrement(), a.allowNull(), a.length(), a.comment()));
            }

        }
        if (null == table.getPrimaryKeyColumn()) {
            throw new EOrmException("not find @EColumn primaryKey = true");
        }
        if (table.getColumns().isEmpty()) {
            throw new EOrmException("not find @EColumn");
        }
        DOMAIN_TABLE_CACHE.put(key, table);
        return table;
    }

    private static <T> String getTableName(Class<T> clazz) {

        // 获取表名
        ETable table = clazz.getAnnotation(ETable.class);
        if (null == table) {
            throw new EOrmException("not found @ETable");
        }
        StringBuilder sb = new StringBuilder("`");
        // 获取数据库名称
        String database = table.database();

        if (database.trim().length() != 0) {
            sb.append(database);
            sb.append("`.`");
        }
        // 获取注解的表名
        sb.append(table.name());
        sb.append("`");

        return sb.toString();
    }

    public static void validate(Column column, Object value) {
        if (!column.isAllowNull() && null == value) {
            throw new EOrmException("column [" + column.getName() + "] not null");
        }
        if (0 < column.getLength() && String.valueOf(value).length() > column.getLength()) {
            throw new EOrmException("column [" + column.getName() + "] length <= " + column.getLength());
        }
    }
}