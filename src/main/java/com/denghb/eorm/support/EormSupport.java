package com.denghb.eorm.support;


import com.denghb.eorm.EormException;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;
import com.denghb.eorm.support.model.Column;
import com.denghb.eorm.support.model.Expression;
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
     * 美化分析SQL
     *
     * @param sql  原SQL
     * @param args 参数
     * @return 美化后的SQL
     */
    public static String parse(String sql, Object... args) {

        return sql;
    }

    private static final char $ = '$';
    private static final char HASH = '#';
    private static final String HASH_IF = "#if";
    private static final String HASH_ELSE_IF = "#elseIf";
    private static final String HASH_ELSE = "#else";
    private static final String HASH_END = "#end";

    /**
     * 转换SQL模版
     * <pre>
     *     String month = "201905";
     *     tb_user_${month} > tb_user_201905
     *
     *
     *     #if()
     *
     *     #elseif()
     *
     *     #else
     *
     *     #end
     * </pre>
     *
     * @param sql    模版
     * @param params 参数
     * @return 分析后的SQL
     */
    public static String parse(String sql, Map<String, Object> params) {

        if (-1 == sql.indexOf(HASH) && -1 == sql.indexOf($)) {
            return sql;
        }
        StandardEvaluationContext seContext = null;
        if (sql.contains(HASH_IF)) {
            seContext = new StandardEvaluationContext();
            seContext.setVariables(params);
        }
        StringBuilder appendSQL = new StringBuilder();
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            // a${b}c > abc
            if ($ == c && '{' == sql.charAt(i + 1)) {
                i += 2;
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
                appendSQL.append(object);// 只当成字符串拼接
            } else {
                appendSQL.append(c);
            }
        }

        sql = appendSQL.toString();

        StringBuilder ss = new StringBuilder();
        int i = parseIfElseIfElseEnd(sql, 0, ss, seContext);
        return ss.toString();
    }

    // 判断有么有#if
    private static int parseIfElseIfElseEnd(String sql, int i, StringBuilder ss, StandardEvaluationContext seContext) {

        String nextSQL = sql.substring(i);
        for (int j = 0; j < nextSQL.length(); j++) {
            if (HASH != nextSQL.charAt(j)) {
                continue;
            }
            if (hasKeyword(nextSQL, HASH_IF, j)) {
                break;
            }
            if (hasKeyword(nextSQL, HASH_ELSE_IF, j)) {
                return i;
            }
            if (hasKeyword(nextSQL, HASH_ELSE, j)) {
                return i;
            }
            if (hasKeyword(nextSQL, HASH_END, j)) {
                return i;
            }

        }
        boolean append = true;

        // 协助流程判断
        boolean _if = false, _elseIf = false, _else = false;

        for (; i < sql.length(); i++) {
            char c = sql.charAt(i);
            // #if
            if (HASH == c && hasKeyword(sql, HASH_IF, i)) {
                i += 3;
                Expression e = getExpression(sql, i);
                i = e.getEndIndex();
                append = SpEL.parseExpression(e.getContent()).getValue(seContext, Boolean.class);
                _if = append;
                if (!append) {
                    i = ignoreInternalIfEnd(sql, i);
                } else {
                    i = parseIfElseIfElseEnd(sql, i, ss, seContext);
                }
            }
            // #elseIf
            else if (HASH == c && !_if && hasKeyword(sql, HASH_ELSE_IF, i)) {
                i += 7;
                if (_elseIf) {
                    append = false;
                    continue;
                }
                Expression e = getExpression(sql, i);
                i = e.getEndIndex();
                append = SpEL.parseExpression(e.getContent()).getValue(seContext, Boolean.class);
                _elseIf = append;

                if (!append) {
                    i = ignoreInternalIfEnd(sql, i);
                } else {
                    i = parseIfElseIfElseEnd(sql, i, ss, seContext);
                }
            }
            // #else
            else if (HASH == c && !_if && !_elseIf && hasKeyword(sql, HASH_ELSE, i)) {
                i += 5;
                i = parseIfElseIfElseEnd(sql, i, ss, seContext);
                append = true;
            }
            // #end
            else if (HASH == c && hasKeyword(sql, HASH_END, i)) {
                i += 4;
                append = true;
            } else if (append) {
                ss.append(c);
            }

        }
        return i;
    }

    /**
     * 忽略#if、#elseIf 内部 #if ... #end
     *
     * @param sql 模版
     * @param i   索引
     * @return 接下来的索引
     */
    private static int ignoreInternalIfEnd(String sql, int i) {
        int countIf = 0;
        int index = i;
        for (; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (HASH == c) {
                if (hasKeyword(sql, HASH_IF, i)) {
                    countIf++;
                } else if (0 < countIf && hasKeyword(sql, HASH_END, i)) {
                    countIf--;
                    index = i;
                } else if (0 == countIf && (hasKeyword(sql, HASH_ELSE_IF, i)
                        || hasKeyword(sql, HASH_ELSE, i)
                        || hasKeyword(sql, HASH_END, i))) {
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 判断接下来的字符串是否为keyword
     *
     * @param sql     模版
     * @param keyword 关键字
     * @param i       索引
     * @return 结果
     */
    private static boolean hasKeyword(String sql, String keyword, int i) {
        if (null == keyword || null == sql) {
            return false;
        }
        int length = keyword.length();
        if (sql.length() < i + length) {
            return false;
        }
        for (int j = 0; j < length; j++) {
            if (sql.charAt(i + j) != keyword.charAt(j)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取 #if (Expression)
     *
     * @param sql 模版
     * @param i   索引
     * @return Expression
     */
    private static Expression getExpression(String sql, int i) {

        StringBuilder el = new StringBuilder();
        int counter = 0;// `(` 计数器
        for (; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if ('(' == c) {
                counter++;
                if (1 == counter) {
                    continue;
                }
            }
            if (')' == c) {
                counter--;
                if (0 == counter) {
                    i++;
                    break;
                }
            }

            if (0 < counter) {
                el.append(c);
            }
        }
        return new Expression(i, el.toString());
    }

}
