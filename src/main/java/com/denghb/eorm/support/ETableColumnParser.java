package com.denghb.eorm.support;


import com.denghb.eorm.EormException;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.EReflectUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public class ETableColumnParser {

    // <tableName,Table>
    private static final Map<String, Table> CACHE_TABLE = new ConcurrentHashMap<String, Table>();

    private static final Map<String, String> WHERE_PRIMARY_KEY_CACHE = new ConcurrentHashMap<String, String>();

    private static final Map<String, String> ALL_COLUMN_CACHE = new ConcurrentHashMap<String, String>();

    public static Table getTable(String tableName) {
        return CACHE_TABLE.get(tableName);
    }

    public static Table load(Class<?> clazz) {
        Etable etable = clazz.getAnnotation(Etable.class);
        if (null == etable) {
            throw new EormException("not find @Etable");
        }
        String tableName = etable.name();
        Table table = getTable(tableName);
        if (null != table) {
            return table;
        }
        table = new Table();
        table.setName(tableName);

        //
        Set<Field> fields = EReflectUtils.getFields(clazz);
        for (Field field : fields) {

            Ecolumn e = field.getAnnotation(Ecolumn.class);
            if (null == e) {
                continue;
            }
            Column column = buildColumn(e, field);
            table.getAllColumns().add(column);

            boolean primaryKey = e.primaryKey();
            if (primaryKey) {
                table.getPkColumns().add(column);
            } else {
                table.getOtherColumns().add(column);
            }

        }
        if (table.getAllColumns().isEmpty()) {
            throw new EormException("not find @Ecolumn");
        }
        if (table.getPkColumns().isEmpty()) {
            throw new EormException("not find @Ecolumn primaryKey = true");
        }
        CACHE_TABLE.put(tableName, table);
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

    public static void validate(Column column, Object value) {
        if (!column.isAllowNull() && null == value) {
            throw new EormException("column [" + column.getName() + "] not null");
        }
        if (0 < column.getCharMaxLength() && String.valueOf(value).length() > column.getCharMaxLength()) {
            throw new EormException("column [" + column.getName() + "] length <= " + column.getCharMaxLength());
        }
    }


    /**
     * 解析主键
     *
     * @param table 表对象
     * @return 主键查询拼接
     */
    public static String loadWherePrimaryKey(Table table) {
        String key = table.getName();
        String where = WHERE_PRIMARY_KEY_CACHE.get(key);
        if (null == where) {
            StringBuilder sb = new StringBuilder(" where ");
            boolean append = false;
            List<Column> columns = table.getPkColumns();
            for (Column column : columns) {
                if (append) {
                    sb.append(" and ");
                }
                sb.append("`");
                sb.append(column.getName());
                sb.append("` = ?");
                append = true;
            }
            where = sb.toString();
            WHERE_PRIMARY_KEY_CACHE.put(key, where);
        }
        return where;
    }

    /**
     * 加载所有的列
     *
     * @param table 表对象
     * @return 列拼接
     */
    public static String loadAllColumnName(Table table) {
        String key = table.getName();
        String columnNames = ALL_COLUMN_CACHE.get(key);
        if (null == columnNames) {
            StringBuilder sb = new StringBuilder();
            boolean append = false;
            List<Column> columns = table.getAllColumns();
            for (Column column : columns) {
                if (append) {
                    sb.append(", ");
                }
                sb.append("`");
                sb.append(column.getName());
                sb.append("`");
                append = true;
            }
            columnNames = sb.toString();
            ALL_COLUMN_CACHE.put(key, columnNames);
        }
        return columnNames;
    }
}
