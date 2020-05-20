package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormException;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.template.EQueryTemplate;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.EReflectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 基本实现
 */
public class EormImpl implements Eorm {

    public Log log = LogFactory.getLog(this.getClass());

    public JdbcTemplate jdbcTemplate;

    public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public EormImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private void outLog(String sql, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("Params:" + Arrays.toString(args));
            log.debug("Execute SQL:" + sql);
        }
    }

    private void outErrorLog(String sql, Object... args) {
        log.error("Params:" + Arrays.toString(args));
        log.error("Execute SQL:" + sql);
    }

    @Override
    public int execute(String sql, Object... args) {
        outLog(sql, args);
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public <T> List<T> select(Class<T> clazz, String sql, Object... args) {
        List<T> list = null;
        if (sql.contains(":")) {// namedParameter
            Map<String, Object> params = null;
            Object object = args[0];
            if (null == object) {
                throw new EormException("args is not null");
            }
            if (object instanceof Map) {
                params = (Map<String, Object>) object;
            } else {
                params = EReflectUtils.objectToMap(object);
            }
            sql = EQueryTemplate.parse(sql, params);
            outLog(sql, params);
            if (EReflectUtils.isSingleClass(clazz)) {
                list = namedParameterJdbcTemplate.queryForList(sql, params, clazz);
            } else {
                list = namedParameterJdbcTemplate.query(sql, params, BeanPropertyRowMapper.newInstance(clazz));
            }
        } else {
            outLog(sql, args);
            if (0 == args.length || !sql.contains("?")) {
                if (EReflectUtils.isSingleClass(clazz)) {
                    list = jdbcTemplate.queryForList(sql, clazz);
                } else {
                    list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz));
                }
            } else {
                if (EReflectUtils.isSingleClass(clazz)) {
                    list = jdbcTemplate.queryForList(sql, clazz, args);
                } else {
                    list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz), args);
                }
            }
        }
        return list;
    }

    @Override
    public <T> void insert(T domain) {

        Table table = ETableColumnParser.load(domain.getClass());

        StringBuilder csb = new StringBuilder();
        StringBuilder vsb = new StringBuilder();
        final List<Object> params = new ArrayList<Object>();

        final Column primaryKeyColumn = table.getPrimaryKeyColumn();
        Field primaryKeyFiled = primaryKeyColumn.getField();
        Object primaryKeyValue = EReflectUtils.getFieldValue(primaryKeyFiled, domain);

        if (null != primaryKeyValue) {
            csb.append('`');
            csb.append(primaryKeyColumn.getName());
            csb.append('`');

            vsb.append('?');
            params.add(primaryKeyValue);
        }

        for (Column column : table.getColumns()) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            ETableColumnParser.validate(column, value);
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

        final String sql = sb.toString();

        int res = 0;
        final Object[] args = params.toArray();
        // 主键是否自动赋值
        if (null == primaryKeyValue && Number.class.isAssignableFrom(primaryKeyFiled.getType())) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            outLog(sql, args);
            res = jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{primaryKeyColumn.getName()});
                    for (int i = 0; i < params.size(); i++) {
                        Object obj = params.get(i);
                        ps.setObject(i + 1, obj);
                    }
                    return ps;
                }
            }, keyHolder);

            Number number = keyHolder.getKey();
            Class type = primaryKeyFiled.getType();
            if (type == Integer.class || type == int.class) {
                EReflectUtils.setFieldValue(primaryKeyFiled, domain, number.intValue());
            } else if (type == Long.class || type == long.class) {
                EReflectUtils.setFieldValue(primaryKeyFiled, domain, number.longValue());
            } else {
                throw new EormException("insert set primaryKey[" + primaryKeyColumn.getName() + "] value fail");
            }
        } else {
            res = execute(sql, args);
        }

        if (1 != res) {
            outErrorLog(sql, args);
            throw new EormException("insert fail");
        }
    }

    @Override
    public <T> void updateById(T domain) {
        Table table = ETableColumnParser.load(domain.getClass());
        List<Object> values = new ArrayList<Object>();

        Column primaryKeyColumn = table.getPrimaryKeyColumn();
        Object primaryKeyValue = EReflectUtils.getFieldValue(primaryKeyColumn.getField(), domain);

        StringBuilder ssb = new StringBuilder();
        for (Column column : table.getColumns()) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            ETableColumnParser.validate(column, value);
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
        sb.append(" where ");
        sb.append("`");
        sb.append(primaryKeyColumn.getName());
        sb.append("` = ?");
        values.add(primaryKeyValue);

        String sql = sb.toString();
        final Object[] args = values.toArray();
        int res = this.execute(sql, args);

        if (1 != res) {
            outErrorLog(sql, args);
            throw new EormException("updateById fail");
        }
    }

    @Override
    public <T> void deleteById(T domain) {
        Table table = ETableColumnParser.load(domain.getClass());

        Column primaryKeyColumn = table.getPrimaryKeyColumn();
        Object primaryKeyValue = EReflectUtils.getFieldValue(primaryKeyColumn.getField(), domain);

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.getName());
        sb.append(" where ");
        sb.append("`");
        sb.append(primaryKeyColumn.getName());
        sb.append("` = ?");

        String sql = sb.toString();
        int res = this.execute(sql, primaryKeyValue);

        if (1 != res) {
            outErrorLog(sql, primaryKeyValue);
            throw new EormException("deleteById fail");
        }
    }

    @Override
    public <T> void deleteById(Class<T> clazz, Object... id) {
        Table table = ETableColumnParser.load(clazz);

        Column primaryKeyColumn = table.getPrimaryKeyColumn();

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.getName());
        sb.append(" where ");
        sb.append("`");
        sb.append(primaryKeyColumn.getName());
        sb.append("` = ?");

        String sql = sb.toString();
        int res = this.execute(sql, id);

        if (1 != res) {
            outErrorLog(sql, id);
            throw new EormException("deleteById fail");
        }
    }

    @Override
    public <T> T selectOne(Class<T> clazz, String sql, Object... args) {
        List<T> list = select(clazz, sql, args);
        if (null != list && !list.isEmpty()) {
            int size = list.size();
            if (size == 1) {
                return list.get(0);
            }
            throw new EormException("select one but:" + size);
        }
        return null;
    }

    @Override
    public <T> T selectById(Class<T> clazz, Object... id) {
        // 表名
        Table table = ETableColumnParser.load(clazz);

        Column primaryKeyColumn = table.getPrimaryKeyColumn();

        String sb = "select * from " + table.getName() +
                " where " +
                "`" +
                primaryKeyColumn.getName() +
                "` = ?";
        return selectOne(clazz, sb, id);
    }

}
