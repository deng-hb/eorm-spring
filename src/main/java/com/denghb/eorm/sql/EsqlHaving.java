package com.denghb.eorm.sql;

import com.denghb.eorm.support.EBiConsumer;

public class EsqlHaving<T> {

    private Esql<T> esql;

    public EsqlHaving(Esql<T> esql) {
        this.esql = esql;
    }

    public <U> EsqlHavingPredicate<T> sum(EBiConsumer<T, U> func) {
        return new EsqlHavingPredicate<T>(esql, func);
    }

    public <U> EsqlHavingPredicate<T> count(EBiConsumer<T, U> func) {
        return new EsqlHavingPredicate<T>(esql, func);
    }

    public <U> EsqlHavingPredicate<T> avg(EBiConsumer<T, U> func) {
        return new EsqlHavingPredicate<T>(esql, func);
    }

    public <U> EsqlHavingPredicate<T> min(EBiConsumer<T, U> func) {
        return new EsqlHavingPredicate<T>(esql, func);
    }

    public <U> EsqlHavingPredicate<T> max(EBiConsumer<T, U> func) {
        return new EsqlHavingPredicate<T>(esql, func);
    }
}
