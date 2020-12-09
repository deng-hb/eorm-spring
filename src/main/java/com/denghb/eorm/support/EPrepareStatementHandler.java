package com.denghb.eorm.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * FIXME 简单介绍该接口
 *
 * @author denghongbing
 * @date 2020/11/30 23:19
 */
public abstract class EPrepareStatementHandler<T> {

    private T result;

    public abstract T onExecute(PreparedStatement ps) throws SQLException;

    public void setResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }
}
