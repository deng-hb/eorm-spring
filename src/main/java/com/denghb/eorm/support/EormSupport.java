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
        StringBuilder ss = new StringBuilder();
        int sqlLength = sql.length();
        for (int i = 0; i < sqlLength; i++) {
            char c = sql.charAt(i);
            // a${b}c > abc
            if ($ == c && '{' == sql.charAt(i + 1)) {
                i += 2;
                StringBuilder name = new StringBuilder();
                for (; i < sqlLength; i++) {
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
            } else if (HASH == c) {
                i = parseIfElseIfElseEnd(sql, sqlLength, i, ss, seContext);
            } else {
                ss.append(c);
            }
        }
        return ss.toString();
    }

    /**
     * 判断接下来是否有 #if #end
     *
     * @param sql       模版
     * @param sqlLength 长度
     * @param i         索引
     * @return
     */
    private static boolean hasNextHashIfEnd(String sql, int sqlLength, int i) {
        for (int j = i; j < sqlLength; j++) {
            if (HASH != sql.charAt(j)) {
                continue;
            }
            if (hasNextKeyword(sql, sqlLength, HASH_IF, j)) {
                return true;
            }
            if (hasNextKeyword(sql, sqlLength, HASH_ELSE_IF, j)) {
                return false;
            }
            if (hasNextKeyword(sql, sqlLength, HASH_ELSE, j)) {
                return false;
            }
            if (hasNextKeyword(sql, sqlLength, HASH_END, j)) {
                return false;
            }
        }
        return false;
    }

    /**
     * 处理 #if #elseIf #else #end
     *
     * @param sql       模版
     * @param sqlLength 长度
     * @param i         索引
     * @param ss        结果
     * @param seContext 判断
     * @return
     */
    private static int parseIfElseIfElseEnd(String sql, int sqlLength, int i, StringBuilder ss, StandardEvaluationContext seContext) {

        if (!hasNextHashIfEnd(sql, sqlLength, i)) {
            return i;
        }

        boolean append = true;

        // 协助流程判断
        boolean _if = false, _elseIf = false;

        int j = i;
        for (; j < sqlLength; j++) {
            char c = sql.charAt(j);
            if (HASH != c) {
                if (append) {
                    ss.append(c);
                }
                continue;
            }
            // #if
            if (hasNextKeyword(sql, sqlLength, HASH_IF, j)) {
                j += 3;
                Expression e = getExpression(sql, sqlLength, j);
                j = e.getEndIndex();
                append = SpEL.parseExpression(e.getContent()).getValue(seContext, Boolean.class);
                _if = append;

                if (append) {
                    j = parseIfElseIfElseEnd(sql, sqlLength, j, ss, seContext);
                    continue;
                } else {
                    j = ignoreInternalIfEnd(sql, sqlLength, j);
                }
            }
            // #elseIf
            else if (hasNextKeyword(sql, sqlLength, HASH_ELSE_IF, j)) {
                j += 7;
                if (_if || _elseIf) {
                    append = false;
                    continue;
                }
                Expression e = getExpression(sql, sqlLength, j);
                j = e.getEndIndex();
                append = SpEL.parseExpression(e.getContent()).getValue(seContext, Boolean.class);
                _elseIf = append;

                if (append) {
                    j = parseIfElseIfElseEnd(sql, sqlLength, j, ss, seContext);
                    continue;
                } else {
                    j = ignoreInternalIfEnd(sql, sqlLength, j);
                }
            }
            // #else
            else if (hasNextKeyword(sql, sqlLength, HASH_ELSE, j)) {
                j += 5;
                if (_if || _elseIf) {
                    append = false;
                    continue;
                }
                j = parseIfElseIfElseEnd(sql, sqlLength, j, ss, seContext);
                continue;
            }
            // #end
            else if (hasNextKeyword(sql, sqlLength, HASH_END, j)) {
                j += 3;
                break;
            }

            // append #other ?
            if (append) {
                ss.append(c);
            }

        }
        return j;
    }

    /**
     * 忽略#if、#elseIf 内部 #if ... #end
     *
     * @param sql 模版
     * @param i   索引
     * @return 接下来的索引
     */
    private static int ignoreInternalIfEnd(String sql, int sqlLength, int i) {
        int countIf = 0;
        int index = i;
        for (; i < sqlLength; i++) {
            char c = sql.charAt(i);
            if (HASH == c) {
                if (hasNextKeyword(sql, sqlLength, HASH_IF, i)) {
                    countIf++;
                } else if (0 < countIf && hasNextKeyword(sql, sqlLength, HASH_END, i)) {
                    countIf--;
                    index = i;
                } else if (0 == countIf && (hasNextKeyword(sql, sqlLength, HASH_ELSE_IF, i)
                        || hasNextKeyword(sql, sqlLength, HASH_ELSE, i)
                        || hasNextKeyword(sql, sqlLength, HASH_END, i))) {
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 判断接下来的字符串是否为keyword
     *
     * @param sql       模版
     * @param sqlLength 长度
     * @param keyword   关键字
     * @param i         索引
     * @return 结果
     */
    private static boolean hasNextKeyword(String sql, int sqlLength, String keyword, int i) {
        if (null == keyword || null == sql) {
            return false;
        }
        int length = keyword.length();
        if (sqlLength < i + length) {
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
    private static Expression getExpression(String sql, int sqlLength, int i) {

        StringBuilder el = new StringBuilder();
        int counter = 0;// `(` 计数器
        int j = i;
        for (; j < sqlLength; j++) {
            char c = sql.charAt(j);
            if ('(' == c) {
                counter++;
                if (1 == counter) {
                    continue;
                }
            }
            if (')' == c) {
                counter--;
                if (0 == counter) {
                    break;
                }
            }

            if (0 < counter) {
                el.append(c);
            }
        }
        return new Expression(j, el.toString());
    }

}
