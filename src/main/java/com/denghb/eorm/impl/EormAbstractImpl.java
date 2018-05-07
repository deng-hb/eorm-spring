package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.utils.EormUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 抽象实现
 * insert
 * batchInsert
 * list
 * doTx
 */
public abstract class EormAbstractImpl implements Eorm {

    private Log log = LogFactory.getLog(this.getClass());

    private JdbcTemplate jdbcTemplate;

    public EormAbstractImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int execute(String sql, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("Params:" + Arrays.toString(args));
            log.debug("Execute SQL:" + sql);
        }
        return jdbcTemplate.update(sql, args);
    }

    private boolean isSingleClass(Class clazz) {
        return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || CharSequence.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz);
    }

    public <T> List<T> select(Class<T> clazz, String sql, Object... args) {

        if (log.isDebugEnabled()) {
            log.debug("Params:" + Arrays.toString(args));
            log.debug("Query SQL:" + sql);
        }
        List<T> list = null;

        if (null == args || 0 == args.length || args[0] == null) {
            if (isSingleClass(clazz)) {
                list = jdbcTemplate.queryForList(sql, clazz);
            } else {
                list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz));
            }
        } else {
            if (isSingleClass(clazz)) {
                list = jdbcTemplate.queryForList(sql, clazz, args);
            } else {
                list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz), args);
            }
        }
        return list;
    }

    public <T> int update(T domain) {

        EormUtils.TableInfo table = EormUtils.getTableInfo(domain);
        List<Object> params = new ArrayList<Object>();

        List<EormUtils.Column> commonColumns = table.getCommonColumns();
        StringBuilder ssb = new StringBuilder();
        for (int i = 0; i < commonColumns.size(); i++) {
            EormUtils.Column column = commonColumns.get(i);

            if (i > 0) {
                ssb.append(", ");
            }

            ssb.append("`");
            ssb.append(column.getName());
            ssb.append("` = ?");

            params.add(column.getValue());
        }

        List<EormUtils.Column> primaryKeyColumns = table.getPrimaryKeyColumns();
        StringBuilder wsb = new StringBuilder();
        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            EormUtils.Column column = primaryKeyColumns.get(i);
            if (i > 0) {
                wsb.append(" and ");
            }
            wsb.append("`");
            wsb.append(column.getName());
            wsb.append("` = ?");

            params.add(column.getValue());
        }

        StringBuilder sb = new StringBuilder("update ");
        sb.append(table.getTableName());
        sb.append(" set ");
        sb.append(ssb);
        sb.append(" where ");
        sb.append(wsb);

        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());
        return res;
    }

    public <T> int delete(T domain) {
        EormUtils.TableInfo table = EormUtils.getTableInfo(domain);
        List<Object> params = new ArrayList<Object>();

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.getTableName());
        sb.append(" where ");

        List<EormUtils.Column> primaryKeyColumns = table.getPrimaryKeyColumns();
        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            EormUtils.Column column = primaryKeyColumns.get(i);
            if (i > 0) {
                sb.append(" and ");
            }
            sb.append("`");
            sb.append(column.getName());
            sb.append("` = ?");

            params.add(column.getValue());
        }

        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());
        return res;
    }

    public <T> int delete(Class<T> clazz, Object... ids) {

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(EormUtils.getTableName(clazz));
        sb.append(" where ");

        List<String> primaryKeyNames = EormUtils.getPrimaryKeyNames(clazz);
        for (int i = 0; i < primaryKeyNames.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            sb.append("`");
            sb.append(primaryKeyNames.get(i));
            sb.append("` = ?");
        }

        String sql = sb.toString();
        int res = this.execute(sql, ids);
        return res;
    }

    public <T> T selectOne(Class<T> clazz, String sql, Object... args) {

        List<T> list = select(clazz, sql, args);
        if (null != list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public <T> T selectByPrimaryKey(Class<T> clazz, Object... args) {

        // 表名
        String tableName = EormUtils.getTableName(clazz);
        StringBuilder sb = new StringBuilder("select * from ");
        sb.append(tableName);
        sb.append(" where ");

        // 主键名
        List<String> primaryKeyNames = EormUtils.getPrimaryKeyNames(clazz);
        for (int i = 0; i < primaryKeyNames.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            sb.append("`");
            sb.append(primaryKeyNames.get(i));
            sb.append("` = ?");
        }

        return selectOne(clazz, sb.toString(), args);
    }

}
