package com.denghb.eorm.support;

import com.denghb.eorm.EOrmException;
import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.utils.EReflectUtils;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/12/25 17:25
 */
public class ESQLWhere<T> implements Serializable {
    // 缓存
    private final static Map<String, String> COLUMN_CACHE = new ConcurrentHashMap<String, String>(500);
    private final static Map<String, Class<?>> TYPE_CACHE = new ConcurrentHashMap<String, Class<?>>(200);

    private StringBuilder sql = new StringBuilder("where ");

    private List<Object> args = new ArrayList<Object>();

    private Class<T> type;

    public ESQLWhere() {
    }

    /**
     * " ( "
     */
    public ESQLWhere<T> start() {
        sql.append(" ( ");
        return this;
    }

    /**
     * " ) "
     */
    public ESQLWhere<T> end() {
        sql.append(" ) ");
        return this;
    }

    /**
     * " and "
     */
    public ESQLWhere<T> and() {
        sql.append(" and ");
        return this;
    }

    private void setDefaultAnd() {
        // " = ?" or "in (?, ?)" or " null"
        int last1 = sql.length() - 1;
        if (sql.lastIndexOf("?") == last1 || sql.lastIndexOf(")") == last1
                || sql.lastIndexOf(" null") == sql.length() - 5) {
            and();
        }
    }

    public ESQLWhere<T> or() {
        sql.append(" or ");
        return this;
    }

    public <U> ESQLWhere<T> eq(EBiConsumer<T, U> func, U arg) {
        sql.append(getColumnName(func)).append(" = ?");
        args.add(arg);
        return this;
    }

    public <U> ESQLWhere<T> neq(EBiConsumer<T, U> func, U arg) {
        sql.append(getColumnName(func)).append(" != ?");
        args.add(arg);
        return this;
    }

    public <U> ESQLWhere<T> gt(EBiConsumer<T, U> func, U arg) {
        sql.append(getColumnName(func)).append(" > ?");
        args.add(arg);
        return this;
    }

    public <U> ESQLWhere<T> gte(EBiConsumer<T, U> func, U arg) {
        sql.append(getColumnName(func)).append(" >= ?");
        args.add(arg);
        return this;
    }

    public <U> ESQLWhere<T> between(EBiConsumer<T, U> func, U arg, U arg2) {
        sql.append(getColumnName(func)).append(" between ? and ?");
        args.add(arg);
        args.add(arg2);
        return this;
    }

    public <U> ESQLWhere<T> lt(EBiConsumer<T, U> func, U arg) {
        sql.append(getColumnName(func)).append(" < ?");
        args.add(arg);
        return this;
    }


    public <U> ESQLWhere<T> lte(EBiConsumer<T, U> func, U arg) {
        sql.append(getColumnName(func)).append(" <= ?");
        args.add(arg);
        return this;
    }


    public <U> ESQLWhere<T> like(EBiConsumer<T, U> func, U arg) {
        sql.append(getColumnName(func)).append(" like ?");
        args.add(arg);
        return this;
    }

    public <U> ESQLWhere<T> in(EBiConsumer<T, U> func, List<U> list) {
        sql.append(getColumnName(func)).append(" in ");
        appendListArgs(list);
        return this;
    }

    private <U> void appendListArgs(List<U> list) {
        boolean append = false;
        sql.append("(");
        for (Object a : list) {
            if (null == a) {
                continue;// fix "column in (null)" error
            }
            if (append) {
                sql.append(", ");
            }
            sql.append("?");
            args.add(a);
            append = true;
        }
        sql.append(")");
    }

    public <U> ESQLWhere<T> notIn(EBiConsumer<T, U> func, List<U> list) {
        sql.append(getColumnName(func)).append(" not in ");
        appendListArgs(list);
        return this;
    }

    public <U> ESQLWhere<T> isNotNull(EBiConsumer<T, U> func) {
        sql.append(getColumnName(func)).append(" is not null");
        return this;
    }

    public <U> ESQLWhere<T> isNull(EBiConsumer<T, U> func) {
        sql.append(getColumnName(func)).append(" is null");
        return this;
    }

    public ESQLWhere<T> exists(String existsSql) {
        sql.append(" exists ");
        sql.append("(");
        sql.append(existsSql);
        sql.append(")");
        return this;
    }

    public ESQLWhere<T> notExists(String notExistsSql) {
        sql.append(" not exists ");
        sql.append("(");
        sql.append(notExistsSql);
        sql.append(")");
        return this;
    }

    public String sql() {
        return sql.toString();
    }

    public StringBuilder getSql() {
        return sql;
    }

    public List<Object> getArgs() {
        return args;
    }

    public Object[] args() {
        return args.toArray();
    }

    public Class<T> getType() {
        return type;
    }

    private String getColumnName(Serializable lambda) {
        try {

            // ??
            setDefaultAnd();

            Method method = lambda.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(lambda);

            String objectClassName = serializedLambda.getImplClass().replaceAll("/", ".");
            String key = objectClassName + "#" + serializedLambda.getImplMethodName();

            type = (Class<T>) TYPE_CACHE.get(objectClassName);

            String columnName = COLUMN_CACHE.get(key);
            if (null != columnName) {
                return columnName;
            }
            if (null == type) {
                type = (Class<T>) Class.forName(objectClassName);
                TYPE_CACHE.put(objectClassName, type);
            }

            String methodName = serializedLambda.getImplMethodName();
            String fieldName = Introspector.decapitalize(methodName.replace("set", ""));
            Field field = EReflectUtils.getField(type, fieldName);
            if (null == field) {
                throw new EOrmException("field [" + fieldName + "] not find");
            }
            EColumn column = field.getAnnotation(EColumn.class);
            if (null == column) {
                throw new EOrmException("field [" + fieldName + "] not find @EColumn");
            }

            columnName = column.name();
            COLUMN_CACHE.put(key, columnName);
            return columnName;
        } catch (Exception e) {
            throw new EOrmException(e.getMessage(), e);
        }
    }

}
