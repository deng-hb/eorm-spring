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

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/12/25 17:25
 */
public class ESQLWhere<T> implements Serializable {

    private StringBuilder sql = new StringBuilder("where ");

    private List<Object> args = new ArrayList<Object>();

    private Class<T> type;

    public ESQLWhere() {
    }

    /**
     * " ( "
     */
    public ESQLWhere<T> bracketLeft() {
        sql.append(" ( ");
        return this;
    }

    /**
     * " ) "
     */
    public ESQLWhere<T> bracketRight() {
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
        if (sql.length() > 0) {
            String last = sql.substring(sql.length() - 1, sql.length());
            // " = ?" or "in (?, ?)"
            if (last.equals("?") || last.equals(")")) {
                and();
            }
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

    public <U> ESQLWhere<T> betweenAnd(EBiConsumer<T, U> func, U arg, U arg2) {
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
        sql.append(getColumnName(func)).append(" is not null ");
        return this;
    }

    public <U> ESQLWhere<T> isNull(EBiConsumer<T, U> func) {
        sql.append(getColumnName(func)).append(" is null ");
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
            if (null == type) {
                String className = serializedLambda.getImplClass().replaceAll("/", ".");
                type = (Class<T>) Class.forName(className);
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

            return column.name();
        } catch (Exception e) {
            throw new EOrmException(e.getMessage(), e);
        }
    }

}
