package com.denghb.xxlibrary.base.impl;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.EOrmException;
import com.denghb.eorm.page.EPageReq;
import com.denghb.eorm.page.EPageRes;
import com.denghb.eorm.support.EOrmTableParser;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.ReflectUtils;
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
        Table table = EOrmTableParser.load(getDomainClass());
        Field field = table.getPrimaryKeyColumn().getField();

        Object idValue = ReflectUtils.getFieldValue(field, domain);
        if (null == idValue) {
            insert(domain);
        } else {
            updateById(domain);
        }
    }

    @Override
    public void updateById(T domain) {
        Table table = EOrmTableParser.load(getDomainClass());
        List<Object> values = new ArrayList<Object>();

        Column primaryKeyColumn = table.getPrimaryKeyColumn();
        Object primaryKeyValue = ReflectUtils.getFieldValue(primaryKeyColumn.getField(), domain);

        // 版本号自动++
        Object versionValue = null;

        StringBuilder ssb = new StringBuilder();
        for (Column column : table.getColumns()) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
            if (null != value && "version".equals(column.getName())) {
                versionValue = value;
                continue;
            }
            EOrmTableParser.validate(column, value);
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

        StringBuilder sb = new StringBuilder("update ");
        sb.append(table.getName());
        sb.append(" set ");
        sb.append(ssb);
        sb.append(", `version` = `version` + 1");
        sb.append(" where ");
        sb.append("`");
        sb.append(primaryKeyColumn.getName());
        sb.append("` = ?");
        values.add(primaryKeyValue);
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
            Table table = EOrmTableParser.load(clazz);
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
            Table table = EOrmTableParser.load(clazz);

            Column primaryKeyColumn = table.getPrimaryKeyColumn();

            StringBuilder sb = new StringBuilder("select ");
            sb.append("`");
            sb.append(table.getPrimaryKeyColumn().getName());
            sb.append("`");

            List<Column> columns = table.getColumns();
            for (Column column : columns) {
                sb.append(",`");
                sb.append(column.getName());
                sb.append("`");
            }

            sb.append(" from ");
            sb.append(table.getName());
            sb.append(" where ");
            sb.append("`");
            sb.append(primaryKeyColumn.getName());
            sb.append("` = ?");
            sb.append(" and `deleted` = 0 ");

            sql = sb.toString();
            SQL_CACHE.put(key, sql);
        }

        return db.selectOne(clazz, sql, id);
    }

    @Override
    public EPageRes<T> selectPage(EPageReq req) {
        Class<T> clazz = getDomainClass();
        String key = clazz.getName() + "#selectPage";
        String sql = SQL_CACHE.get(key);
        if (null == sql) {

            Table table = EOrmTableParser.load(getDomainClass());

            StringBuilder sb = new StringBuilder("select ");

            sb.append("`");
            sb.append(table.getPrimaryKeyColumn().getName());
            sb.append("`");

            List<Column> columns = table.getColumns();
            for (Column column : columns) {
                sb.append(",`");
                sb.append(column.getName());
                sb.append("`");
            }
            sb.append(" from ");
            sb.append(table.getName());
            sb.append(" where ");
            sb.append("`deleted` = 0 ");

            sql = sb.toString();
            SQL_CACHE.put(key, sql);
        }
        return db.selectPage(clazz, sql, req);
    }
}
