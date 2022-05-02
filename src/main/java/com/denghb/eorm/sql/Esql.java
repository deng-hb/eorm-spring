package com.denghb.eorm.sql;

import com.denghb.eorm.EOrmException;
import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.support.EBiConsumer;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.domain.EClassRef;
import com.denghb.eorm.support.domain.ETableRef;
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
 * new Esql<Student>().select().distinct().from().alias().where().groupBy().orderBy().having().limit();
 *
 * @param <T>
 */
public class Esql<T> {
    private final static Map<String, String> COLUMN_CACHE = new ConcurrentHashMap<String, String>(500);
    private final static Map<String, Class<?>> TYPE_CACHE = new ConcurrentHashMap<String, Class<?>>(200);

    protected List<String> keys = new ArrayList<>();
    protected List<Object> args = new ArrayList<Object>();
    private Class<T> type;

    public Esql() {

    }

    public Esql<T> select() {
        if (!keys.contains("select")) {
            keys.add(0, "select");
        }
        return this;
    }

    public Esql<T> distinct() {
        if (!keys.contains("distinct")) {
            keys.add(1, "distinct");
        }
        return this;
    }


    public <U> Esql<T> select(EBiConsumer<T, U>... funcList) {
        select();
        boolean first = true;
        StringBuilder columns = new StringBuilder();
        for (EBiConsumer<T, U> func : funcList) {
            if (!first) {
                columns.append(", ");
            }
            columns.append(getColumnName(func));
            first = false;
        }
        keys.add(columns.toString());
        return this;
    }
    public <U> Esql<T> sum(EBiConsumer<T, U> func) {
        keys.add("sum(" + getColumnName(func) + ")");
        return this;
    }

    public <U> Esql<T> count() {
        keys.add("count(*)");
        return this;
    }

    public <U> Esql<T> count(EBiConsumer<T, U> func) {
        keys.add("count(" + getColumnName(func) + ")");
        return this;
    }

    public <U> Esql<T> avg(EBiConsumer<T, U> func) {
        keys.add("avg(" + getColumnName(func) + ")");
        return this;
    }

    public <U> Esql<T> min(EBiConsumer<T, U> func) {
        keys.add("min(" + getColumnName(func) + ")");
        return this;
    }

    public <U> Esql<T> max(EBiConsumer<T, U> func) {
        keys.add("max(" + getColumnName(func) + ")");
        return this;
    }

    public Esql<T> from(Class<T> type) {
        this.type = type;
        keys.add("from");
        keys.add(ETableColumnParser.getTableRef(type).getName());
        return this;
    }


    public Esql<T> alias(String alias) {
        keys.add(alias);
        return this;
    }


    public Esql<T> where() {
        keys.add("where");
        return this;
    }
    /**
     * and
     *
     * @return
     */
    public Esql<T> and() {
        keys.add("and");
        return this;
    }

    /**
     * or
     *
     * @return
     */
    public Esql<T> or() {
        keys.add("or");
        return this;
    }

    /**
     * (
     *
     * @return
     */
    public Esql<T> start() {
        keys.add("(");
        return this;
    }

    /**
     * )
     *
     * @return
     */
    public Esql<T> end() {
        keys.add(")");
        return this;
    }

    /**
     * column = ?
     *
     * @param func
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> eq(EBiConsumer<T, U> func, U arg) {
        keys.add(getColumnName(func) + " = ?");
        args.add(arg);
        return this;
    }

    /**
     * column != ?
     *
     * @param func
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> neq(EBiConsumer<T, U> func, U arg) {
        keys.add(getColumnName(func) + " != ?");
        args.add(arg);
        return this;
    }

    /**
     * column > ?
     *
     * @param func
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> gt(EBiConsumer<T, U> func, U arg) {
        keys.add(getColumnName(func) + " > ?");
        args.add(arg);
        return this;
    }

    /**
     * column >= ?
     *
     * @param func
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> gte(EBiConsumer<T, U> func, U arg) {
        keys.add(getColumnName(func) + " >= ?");
        args.add(arg);
        return this;
    }

    /**
     * column between ? and ?
     *
     * @param func
     * @param arg
     * @param arg2
     * @param <U>
     * @return
     */
    public <U> Esql<T> between(EBiConsumer<T, U> func, U arg, U arg2) {
        keys.add(getColumnName(func) + " between ? and ?");
        args.add(arg);
        args.add(arg2);
        return this;
    }

    /**
     * column < ?
     *
     * @param func
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> lt(EBiConsumer<T, U> func, U arg) {
        keys.add(getColumnName(func) + " < ?");
        args.add(arg);
        return this;
    }

    /**
     * column <= ?
     *
     * @param func
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> lte(EBiConsumer<T, U> func, U arg) {
        keys.add(getColumnName(func) + " <= ?");
        args.add(arg);
        return this;
    }

    /**
     * column like ?
     *
     * @param func
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> like(EBiConsumer<T, U> func, U arg) {
        keys.add(getColumnName(func) + " like ?");
        args.add(arg);
        return this;
    }

    /**
     * column in (?)
     *
     * @param func
     * @param args
     * @param <U>
     * @return
     */
    public <U> Esql<T> in(EBiConsumer<T, U> func, U... args) {
        doArgs("in", func, args);
        return this;
    }

    private <U> void doArgs(String key, EBiConsumer<T, U> func, U... args1) {
        boolean append = false;
        StringBuilder sql = new StringBuilder();
        sql.append(getColumnName(func));
        sql.append(key);
        sql.append(" (");
        for (Object a : args1) {
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
        keys.add(sql.toString());
    }

    /**
     * column not in (?)
     *
     * @param func
     * @param args
     * @param <U>
     * @return
     */
    public <U> Esql<T> notIn(EBiConsumer<T, U> func, U... args) {
        doArgs("not in", func, args);
        return this;
    }

    /**
     * column is not null
     *
     * @param func
     * @param <U>
     * @return
     */
    public <U> Esql<T> isNotNull(EBiConsumer<T, U> func) {
        keys.add(getColumnName(func) + " is not null");
        return this;
    }

    /**
     * column is null
     *
     * @param func
     * @param <U>
     * @return
     */
    public <U> Esql<T> isNull(EBiConsumer<T, U> func) {
        keys.add(getColumnName(func) + " is null");
        return this;
    }

    /**
     * exists (select 1 from t_ where alias.id = t_.id)
     *
     * @param sql
     * @return
     */
    public Esql<T> exists(String sql) {
        keys.add("exists (" + sql + ")");
        return this;
    }

    /**
     * not exists (select 1 from t_ where alias.id = t_.id)
     *
     * @param sql
     * @return
     */
    public Esql<T> notExists(String sql) {
        keys.add("not exists (" + sql + ")");
        return this;
    }

    public <U> Esql<T> groupBy(EBiConsumer<T, U>... funcList) {
        boolean first = true;
        StringBuilder columns = new StringBuilder();
        for (EBiConsumer<T, U> func : funcList) {
            if (!first) {
                columns.append(", ");
            }
            columns.append(getColumnName(func));
            first = false;
        }
        keys.add(columns.toString());
        return this;
    }


    public <U> EsqlOrderBy<T> orderBy(EBiConsumer<T, U> func) {
        keys.add("order by");
        return new EsqlOrderBy<T>(this, func);
    }


    public <U> EsqlHaving<T> having() {
        keys.add("having");
        return new EsqlHaving<T>(this);
    }


    public Esql<T> limit(long rows) {
        keys.add("limit 0, ?");
        args.add(rows);
        return this;
    }


    public Esql<T> limit(long offset, long rows) {
        keys.add("limit ?, ?");
        args.add(offset);
        args.add(rows);
        return this;
    }

    protected String getColumnName(Serializable lambda) {
        try {

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

    public List<Object> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        boolean first = true;
        for (String key : keys) {
            if (!first) {
                sql.append(" ");
            }
            sql.append(key);
            first = false;
        }
        return sql.toString();
    }
}
