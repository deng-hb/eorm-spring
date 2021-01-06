package com.denghb.eorm.impl;

import com.denghb.eorm.EOrmX;
import com.denghb.eorm.support.ESQLWhere;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.ETraceHolder;
import com.denghb.eorm.support.domain.EColumnRef;
import com.denghb.eorm.support.domain.ETableRef;
import com.denghb.eorm.utils.EReflectUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/12/25 20:49
 */
public class EOrmXImpl extends EOrmImpl implements EOrmX {

    public EOrmXImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public <T> int update(T t, ESQLWhere<T> where) {
        ETraceHolder.start();
        ETableRef ref = ETableColumnParser.getTableRef(t.getClass());

        StringBuilder sql = new StringBuilder(ref.getUpdateTable());

        List<Object> values = new ArrayList<Object>();

        for (EColumnRef column : ref.getCommonColumns()) {
            Object value = EReflectUtils.getFieldValue(column.getField(), t);
            if (null == value) {
                continue;
            }
            if (values.size() > 0) {
                sql.append(", ");
            }
            sql.append("`");
            sql.append(column.getName());
            sql.append("` = ?");
            values.add(value);
        }

        sql.append(" ");
        sql.append(where.sql());
        values.addAll(where.getArgs());

        return execute(sql.toString(), values.toArray());
    }

    @Override
    public <T> int delete(ESQLWhere<T> where) {
        ETraceHolder.start();
        ETableRef ref = ETableColumnParser.getTableRef(where.getType());
        StringBuilder sql = new StringBuilder(ref.getDeleteTable()).append(where.sql());
        return execute(sql.toString(), where.args());
    }

    @Override
    public <T> List<T> select(ESQLWhere<T> where) {
        ETraceHolder.start();
        ETableRef ref = ETableColumnParser.getTableRef(where.getType());
        StringBuilder sql = new StringBuilder(ref.getSelectTable()).append(where.sql());
        return select(where.getType(), sql.toString(), where.args());
    }

    @Override
    public <T> T selectOne(ESQLWhere<T> where) {
        ETraceHolder.start();
        ETableRef ref = ETableColumnParser.getTableRef(where.getType());
        StringBuilder sql = new StringBuilder(ref.getSelectTable()).append(where.sql());
        return selectOne(where.getType(), sql.toString(), where.args());
    }
}
