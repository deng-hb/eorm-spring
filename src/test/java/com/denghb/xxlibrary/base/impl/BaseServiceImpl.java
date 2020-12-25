package com.denghb.xxlibrary.base.impl;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.EOrmException;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.domain.EColumnRef;
import com.denghb.eorm.support.domain.ETableRef;
import com.denghb.eorm.utils.EReflectUtils;
import com.denghb.xxlibrary.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author denghb 2019-06-24 23:59
 */
public class BaseServiceImpl<T> implements BaseService<T> {

    @Autowired
    private EOrm db;

    // 固定SQL缓存
    private final static Map<String, String> SQL_CACHE = new ConcurrentHashMap<String, String>();

    private Class<T> getDomainClass() {
        // get domain class
        Type genType = getClass().getGenericSuperclass();
        if (genType instanceof ParameterizedType) {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            return (Class<T>) params[0];
        }
        throw new RuntimeException("Type error");
    }

    @Override
    public void insert(T domain) {
        db.insert(domain);
    }

    @Override
    public void save(T domain) {
        ETableRef table = ETableColumnParser.getTableRef(getDomainClass());
        boolean isInsert = true;

        List<EColumnRef> pkColumns = table.getPrimaryKeyColumns();
        for (EColumnRef column : pkColumns) {
            Field field = column.getField();
            Object idValue = EReflectUtils.getFieldValue(field, domain);
            if (null != idValue) {
                isInsert = false;
                break;
            }
        }
        if (isInsert) {
            insert(domain);
        } else {
            updateById(domain);
        }
    }

    @Override
    public void updateById(T domain) {
        ETableRef table = ETableColumnParser.getTableRef(getDomainClass());
        List<Object> values = new ArrayList<Object>();


        // 版本号自动++
        Object versionValue = null;

        StringBuilder ssb = new StringBuilder();
        for (EColumnRef column : table.getCommonColumns()) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            if (null != value && "version".equals(column.getName())) {
                versionValue = value;
                continue;
            }
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
            Object pkValue = EReflectUtils.getFieldValue(column.getField(), domain);
            values.add(pkValue);
        }

        StringBuilder sb = new StringBuilder("update ");
        sb.append(table.getName());
        sb.append(" set ");
        sb.append(ssb);
        sb.append(", `version` = `version` + 1");
        sb.append(table.getWherePrimaryKeyColumns());
        sb.append(" and `deleted` = 0 ");
        if (null != versionValue) {
            sb.append(" and `version` = ? ");
            values.add(versionValue);
        }

        String sql = sb.toString();
        final Object[] args = values.toArray();
        int res = db.execute(sql, args);
        if (1 != res) {
            throw new EOrmException("update fail");
        }
    }

    @Override
    public void deleteById(Object id) {
        Class<T> clazz = getDomainClass();
        String key = clazz.getName() + "#deleteById";
        String sql = SQL_CACHE.get(key);
        if (null == sql) {
            ETableRef table = ETableColumnParser.getTableRef(clazz);
            sql = "update " + table.getName() + " set deleted = 1 where id = ? and deleted = 0";
            SQL_CACHE.put(key, sql);
        }
        int res = db.execute(sql, id);
        if (1 != res) {
            throw new EOrmException("delete fail");
        }
    }

    @Override
    public T selectById(Object id) {
        Class<T> clazz = getDomainClass();
        String key = clazz.getName() + "#selectById";
        String sql = SQL_CACHE.get(key);
        if (null == sql) {
            // 表名
            ETableRef table = ETableColumnParser.getTableRef(clazz);

            StringBuilder sb = new StringBuilder("select ");
            sb.append(table.getColumns());

            sb.append(" from ");
            sb.append(table.getName());
            sb.append(table.getWherePrimaryKeyColumns());
            sb.append(" and `deleted` = 0 ");

            sql = sb.toString();
            SQL_CACHE.put(key, sql);
        }

        return db.selectOne(clazz, sql, id);
    }

}
