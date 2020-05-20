package com.denghb.eorm.support;


import com.denghb.eorm.EormException;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.EReflectUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public class ETableColumnParser {

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
        Set<Field> fields = EReflectUtils.getFields(clazz);
        for (Field field : fields) {

            Ecolumn column = field.getAnnotation(Ecolumn.class);
            if (null == column) {
                continue;
            }
            boolean primaryKey = column.primaryKey();
            if (primaryKey) {
                if (null != table.getPrimaryKeyColumn()) {
                    throw new EormException("exist primary key");
                }
                table.setPrimaryKeyColumn(buildColumn(column, field));
            } else {
                table.getColumns().add(buildColumn(column, field));
            }

        }
        if (null == table.getPrimaryKeyColumn()) {
            throw new EormException("not find @EColumn primaryKey = true");
        }
        if (table.getColumns().isEmpty()) {
            throw new EormException("not find @EColumn");
        }
        DOMAIN_TABLE_CACHE.put(key, table);
        return table;
    }

    private static Column buildColumn(Ecolumn e, Field field) {
        Column c = new Column();
        c.setAllowNull(e.allowNull());
        c.setComment(e.comment());

        c.setField(field);
        c.setName(e.name());
        c.setCharMaxLength(e.charMaxLength());

        c.setDefaultValue(e.defaultValue());
        return c;
    }

    private static <T> String getTableName(Class<T> clazz) {

        // 获取表名
        Etable table = clazz.getAnnotation(Etable.class);
        if (null == table) {
            throw new EormException("not found @ETable");
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
            throw new EormException("column [" + column.getName() + "] not null");
        }
        if (0 < column.getCharMaxLength() && String.valueOf(value).length() > column.getCharMaxLength()) {
            throw new EormException("column [" + column.getName() + "] length <= " + column.getCharMaxLength());
        }
    }
}
