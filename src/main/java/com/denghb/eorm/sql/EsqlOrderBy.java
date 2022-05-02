package com.denghb.eorm.sql;

import com.denghb.eorm.support.EBiConsumer;

public class EsqlOrderBy<T> {
    private Esql<T> esql;
    private EBiConsumer func;

    public <U> EsqlOrderBy(Esql<T> esql, EBiConsumer<T, U> func) {
        this.esql = esql;
        this.func = func;
    }

    public Esql<T> asc() {
        esql.keys.add(esql.getColumnName(func) + " asc");
        return esql;
    }

    public Esql<T> desc() {
        esql.keys.add(esql.getColumnName(func) + " desc");
        return esql;
    }

}
