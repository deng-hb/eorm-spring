package com.denghb.eorm.sql;

import com.denghb.eorm.support.EBiConsumer;

public class EsqlHavingPredicate<T> {

    private Esql<T> esql;
    private String method;

    public <U> EsqlHavingPredicate(Esql<T> esql, EBiConsumer<T, U> func) {
        this.esql = esql;
        String methodName = new Throwable().getStackTrace()[1].getMethodName();
        String column = esql.getColumnName(func);
        this.method = String.format("%s(%s)", methodName, column);
    }


    /**
     * column = ?
     *
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> eq(U arg) {
        esql.keys.add(method + " = ?");
        esql.args.add(arg);
        return esql;
    }

    /**
     * column != ?
     *
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> neq(U arg) {
        esql.keys.add(method + " != ?");
        esql.args.add(arg);
        return esql;
    }

    /**
     * column > ?
     *
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> gt(U arg) {
        esql.keys.add(method + " > ?");
        esql.args.add(arg);
        return esql;
    }

    /**
     * column >= ?
     *
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> gte(U arg) {
        esql.keys.add(method + " >= ?");
        esql.args.add(arg);
        return esql;
    }


    /**
     * column < ?
     *
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> lt(U arg) {
        esql.keys.add(method + " < ?");
        esql.args.add(arg);
        return esql;
    }

    /**
     * column <= ?
     *
     * @param arg
     * @param <U>
     * @return
     */
    public <U> Esql<T> lte(U arg) {
        esql.keys.add(method + " <= ?");
        esql.args.add(arg);
        return esql;
    }

}
