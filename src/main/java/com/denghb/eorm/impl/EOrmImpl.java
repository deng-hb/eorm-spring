package com.denghb.eorm.impl;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.EOrmException;
import com.denghb.eorm.support.EKeyHolder;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.ETraceHolder;
import com.denghb.eorm.support.domain.EColumnRef;
import com.denghb.eorm.support.domain.ETableRef;
import com.denghb.eorm.utils.EReflectUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 基本实现
 */
public class EOrmImpl extends CoreImpl implements EOrm {

    public EOrmImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public <T> void insert(T domain) {
        ETraceHolder.start();
        ETableRef table = ETableColumnParser.getTableRef(domain.getClass());

        StringBuilder csb = new StringBuilder();
        StringBuilder vsb = new StringBuilder();
        List<Object> params = new ArrayList<Object>();

        Object primaryKeyValue = null;
        Field primaryKeyFiled = null;
        String primaryKeyColumn = null;

        List<EColumnRef> pkColumns = table.getPrimaryKeyColumns();
        for (EColumnRef column : pkColumns) {

            Field pkFiled = column.getField();
            Object pkValue = EReflectUtils.getFieldValue(pkFiled, domain);
            primaryKeyFiled = pkFiled;
            primaryKeyColumn = column.getName();

            if (csb.length() > 0) {
                csb.append(", ");
                vsb.append(", ");
            }

            if (null != pkValue) {
                csb.append('`');
                csb.append(column.getName());
                csb.append('`');

                vsb.append('?');
                params.add(pkValue);

                primaryKeyValue = pkValue;
            }
        }

        for (EColumnRef column : table.getCommonColumns()) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            // 排除null
            if (null == value) {
                continue;
            }
            if (csb.length() > 0) {
                csb.append(", ");
                vsb.append(", ");
            }
            csb.append('`');
            csb.append(column.getName());
            csb.append('`');

            vsb.append('?');
            params.add(value);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(table.getName());
        sb.append(" (");
        sb.append(csb);
        sb.append(") values (");
        sb.append(vsb);
        sb.append(')');

        String sql = sb.toString();
        int res = 0;
        EKeyHolder keyHolder = null;

        // 主键是否自动赋值
        if (pkColumns.size() == 1 && null == primaryKeyValue && Number.class.isAssignableFrom(primaryKeyFiled.getType())) {
            keyHolder = new EKeyHolder();
            params.add(keyHolder);

        }
        res = execute(sql, params.toArray());
        if (1 != res) {
            throw new EOrmException("insert fail");
        }

        // 主键赋值
        if (null != keyHolder) {
            List<Object> keys = keyHolder.getKeys();
            if (null != keys && !keys.isEmpty()) {
                Object object = keys.get(0);
                EReflectUtils.setFieldValue(primaryKeyFiled, domain, object);
            }
        }
    }

    @Override
    public <T> void updateById(T domain) {
        ETraceHolder.start();
        ETableRef table = ETableColumnParser.getTableRef(domain.getClass());
        List<Object> values = new ArrayList<Object>();

        StringBuilder ssb = new StringBuilder();
        for (EColumnRef column : table.getCommonColumns()) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            if (null == value) {
                continue;
            }
            if (ssb.length() > 0) {
                ssb.append(", ");
            }
            ssb.append("`");
            ssb.append(column.getName());
            ssb.append("` = ?");
            values.add(value);
        }

        List<EColumnRef> pkColumns = table.getPrimaryKeyColumns();
        for (EColumnRef column : pkColumns) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            values.add(value);
        }

        String sql = String.format(Locale.CHINA, "update %s set %s %s", table.getName(), ssb,
                table.getWherePrimaryKeyColumns());

        Object[] args = values.toArray();
        int res = this.execute(sql, args);

        if (1 != res) {
            throw new EOrmException("updateById fail");
        }
    }

    @Override
    public <T> void deleteById(T domain) {
        ETraceHolder.start();
        ETableRef table = ETableColumnParser.getTableRef(domain.getClass());

        List<Object> params = new ArrayList<Object>();
        List<EColumnRef> pkColumns = table.getPrimaryKeyColumns();
        for (EColumnRef column : pkColumns) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            params.add(value);
        }

        deleteById(domain.getClass(), params.toArray());
    }

    @Override
    public <T> void deleteById(Class<T> clazz, Object... ids) {
        ETraceHolder.start();
        ETableRef table = ETableColumnParser.getTableRef(clazz);

        String sql = String.format("%s %s", table.getDeleteTable(),
                table.getWherePrimaryKeyColumns());
        int res = this.execute(sql, ids);

        if (1 != res) {
            throw new EOrmException("deleteById fail");
        }
    }

    @Override
    public <T> T selectOne(Class<T> clazz, String sql, Object... args) {
        ETraceHolder.start();
        List<T> list = select(clazz, sql, args);
        if (null != list && !list.isEmpty()) {
            return list.get(0);// 忽略返回多个？
        }
        return null;
    }

    @Override
    public <T> T selectById(Class<T> clazz, Object... ids) {
        ETraceHolder.start();
        // 表名
        ETableRef table = ETableColumnParser.getTableRef(clazz);

        String sql = String.format("%s %s", table.getSelectTable(), table.getWherePrimaryKeyColumns());
        return selectOne(clazz, sql, ids);
    }

}
