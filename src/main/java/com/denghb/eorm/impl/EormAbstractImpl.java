package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormException;
import com.denghb.eorm.support.EormSupport;
import com.denghb.eorm.support.model.Column;
import com.denghb.eorm.support.model.Table;
import com.denghb.eorm.utils.ReflectUtils;
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
 * 核心实现
 */
public abstract class EormAbstractImpl implements Eorm {

    protected Log log = LogFactory.getLog(this.getClass());

    protected JdbcTemplate jdbcTemplate;

    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public EormAbstractImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    protected void outLog(String sql, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("Params:" + Arrays.toString(args));
            log.debug("Execute SQL:" + sql);
        }
    }

    protected void outErrorLog(String sql, Object... args) {
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
        if (sql.contains(":") && null != args && 1 == args.length && !ReflectUtils.isSingleClass(args[0].getClass())) {// namedParameter
            Map<String, Object> params = null;
            Object object = args[0];
            if (null == object) {
                throw new EormException("args is not null");
            }
            if (object instanceof Map) {
                params = (Map<String, Object>) object;
            } else {
                params = ReflectUtils.objectToMap(object);
            }
            sql = EormSupport.parse(sql, params);
            outLog(sql, params);
            if (ReflectUtils.isSingleClass(clazz)) {
                list = namedParameterJdbcTemplate.queryForList(sql, params, clazz);
            } else {
                list = namedParameterJdbcTemplate.query(sql, params, BeanPropertyRowMapper.newInstance(clazz));
            }
        } else {
            outLog(sql, args);
            if (0 == args.length || !sql.contains("?")) {
                if (ReflectUtils.isSingleClass(clazz)) {
                    list = jdbcTemplate.queryForList(sql, clazz);
                } else {
                    list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz));
                }
            } else {
                if (ReflectUtils.isSingleClass(clazz)) {
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

        Table table = EormSupport.load(domain.getClass());

        StringBuilder csb = new StringBuilder();
        StringBuilder vsb = new StringBuilder();
        final List<Object> params = new ArrayList<Object>();

        Object primaryKeyValue = null;
        Field primaryKeyFiled = null;
        Column primaryKeyColumn = null;

        List<Column> pkColumns = table.getPkColumns();
        for (Column column : pkColumns) {

            Field pkFiled = column.getField();
            Object pkValue = ReflectUtils.getFieldValue(pkFiled, domain);
            primaryKeyFiled = pkFiled;
            primaryKeyColumn = column;

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

        for (Column column : table.getOtherColumns()) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
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
        if (pkColumns.size() == 1 && null == primaryKeyValue && Number.class.isAssignableFrom(primaryKeyFiled.getType())) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            outLog(sql, args);
            final Column finalPrimaryKeyColumn = primaryKeyColumn;
            res = jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{finalPrimaryKeyColumn.getName()});
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
                ReflectUtils.setFieldValue(primaryKeyFiled, domain, number.intValue());
            } else if (type == Long.class || type == long.class) {
                ReflectUtils.setFieldValue(primaryKeyFiled, domain, number.longValue());
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
    public <T> void update(T domain) {
        Table table = EormSupport.load(domain.getClass());
        List<Object> values = new ArrayList<Object>();

        StringBuilder ssb = new StringBuilder();
        for (Column column : table.getOtherColumns()) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
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
        sb.append(EormSupport.loadWherePrimaryKey(table));

        List<Column> pkColumns = table.getPkColumns();
        for (Column column : pkColumns) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
            values.add(value);
        }

        String sql = sb.toString();

        Object[] args = values.toArray();
        int res = this.execute(sql, args);

        if (1 != res) {
            outErrorLog(sql, args);
            throw new EormException("updateById fail");
        }
    }

    @Override
    public <T> void delete(T domain) {
        Table table = EormSupport.load(domain.getClass());

        List<Object> params = new ArrayList<Object>();
        List<Column> pkColumns = table.getPkColumns();
        for (Column column : pkColumns) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
            params.add(value);
        }

        delete(domain.getClass(), params.toArray());
    }

    @Override
    public <T> void delete(Class<T> clazz, Object... ids) {
        Table table = EormSupport.load(clazz);

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.getName());
        sb.append(EormSupport.loadWherePrimaryKey(table));

        String sql = sb.toString();
        int res = this.execute(sql, ids);

        if (1 != res) {
            outErrorLog(sql, ids);
            throw new EormException("deleteById fail");
        }
    }

    @Override
    public <T> T selectOne(Class<T> clazz, String sql, Object... args) {
        List<T> list = select(clazz, sql, args);
        if (null != list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public <T> T selectByPrimaryKey(Class<T> clazz, Object... ids) {
        // 表名
        Table table = EormSupport.load(clazz);

        StringBuilder sb = new StringBuilder("select ");
        sb.append(EormSupport.loadAllColumnName(table));
        sb.append(" from ");
        sb.append(table.getName());
        sb.append(EormSupport.loadWherePrimaryKey(table));

        return selectOne(clazz, sb.toString(), ids);
    }

}
