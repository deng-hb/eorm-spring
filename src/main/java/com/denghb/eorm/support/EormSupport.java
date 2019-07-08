package com.denghb.eorm.support;


import com.denghb.eorm.EormException;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;
import com.denghb.eorm.support.model.Column;
import com.denghb.eorm.support.model.Table;
import com.denghb.eorm.utils.ReflectUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public abstract class EormSupport {
    // 性能？
    private static final ExpressionParser SpEL = new SpelExpressionParser();

    private static final Map<String, Table> DOMAIN_TABLE_CACHE = new ConcurrentHashMap<String, Table>();

    private static final Map<String, String> WHERE_PRIMARY_KEY_CACHE = new ConcurrentHashMap<String, String>();

    private static final Map<String, String> ALL_COLUMN_CACHE = new ConcurrentHashMap<String, String>();

    /**
     * 加载表注解
     *
     * @param clazz Eorm 实体类
     * @return 表对象
     */
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

            Ecolumn e = field.getAnnotation(Ecolumn.class);
            if (null == e) {
                continue;
            }
            Column column = new Column(e.name(), field);
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
        DOMAIN_TABLE_CACHE.put(key, table);
        return table;
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

    /**
     * 获取表名
     *
     * @param clazz Eorm 实体类
     * @return 表名
     */
    private static String getTableName(Class clazz) {

        // 获取表名
        Etable table = (Etable) clazz.getAnnotation(Etable.class);
        if (null == table) {
            throw new EormException("not found @Etable");
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

    /**
     * 转换 ${} #{} #end
     *
     * @param sql    模版
     * @param params 参数
     * @return 分析后的SQL
     */
    public static String parse(String sql, Map<String, Object> params) {

        if (!sql.contains("#") && !sql.contains("$")) {
            return sql;
        }
        StandardEvaluationContext ctx = null;
        if (sql.contains("#{")) {
            ctx = new StandardEvaluationContext();
            ctx.setVariables(params);
        }
        StringBuilder ss = new StringBuilder();
        boolean append = true;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            // a${b}c > abc
            if ('$' == c && '{' == sql.charAt(i + 1)) {
                i = i + 2;
                StringBuilder name = new StringBuilder();
                for (; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if (' ' == c) {
                        continue;
                    }
                    if ('}' == c) {
                        break;
                    }
                    name.append(c);
                }
                Object object = params.get(name.toString());
                ss.append(object);// 只当成字符串拼接
            } else if ('#' == c && '{' == sql.charAt(i + 1)) {
                // #{ SpEL }
                i = i + 2;
                StringBuilder el = new StringBuilder();
                for (; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if ('}' == c) {
                        break;
                    }
                    el.append(c);
                }
                append = SpEL.parseExpression(el.toString()).getValue(ctx, Boolean.class);

            } else if ('#' == c && 'e' == sql.charAt(i + 1) && 'n' == sql.charAt(i + 2)
                    && 'd' == sql.charAt(i + 3)) {
                // #end
                i = i + 4;
                append = true;
            } else if (append) {
                ss.append(c);
            }
        }
        return ss.toString();

    }

}
