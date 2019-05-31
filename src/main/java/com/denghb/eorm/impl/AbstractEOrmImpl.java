package com.denghb.eorm.impl;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.EOrmException;
import com.denghb.eorm.parse.EOrmDomainTableParser;
import com.denghb.eorm.parse.EOrmQueryTemplateParser;
import com.denghb.eorm.parse.domain.Column;
import com.denghb.eorm.parse.domain.Table;
import com.denghb.eorm.utils.ReflectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 抽象实现
 * insert
 * batchInsert
 * list
 * doTx
 */
public abstract class AbstractEOrmImpl implements EOrm {

    protected Log log = LogFactory.getLog(this.getClass());

    protected JdbcTemplate jdbcTemplate;

    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AbstractEOrmImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private void outLog(String sql, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("Params:" + Arrays.toString(args));
            log.debug("Execute SQL:" + sql);
        }
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
                throw new EOrmException("args is not null");
            }
            if (object instanceof Map) {
                params = (Map<String, Object>) object;
            } else {
                params = ReflectUtils.objectToMap(object);
            }
            sql = EOrmQueryTemplateParser.parse(sql, params);
            outLog(sql, args);
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

        Table table = EOrmDomainTableParser.init(domain.getClass());

        StringBuilder csb = new StringBuilder();
        final List<Object> params = new ArrayList<>();

        String pkColumnName = null;
        Object pkValue = null;
        Field pkField = null;
        StringBuilder vsb = new StringBuilder();
        for (Column column : table.getColumns()) {
            // 排除null
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
            if (column.getPrimaryKey()) {
                pkColumnName = column.getName();
                pkValue = value;
                pkField = column.getField();
            }
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
        final String fpkColumnName = pkColumnName;
        final Object[] args = params.toArray();
        // 只有一个主键而且是空的表示自动生成的
        if (1 == table.getPkColumns().size() && null == pkValue) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            outLog(sql, args);
            res = jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{fpkColumnName});
                    for (int i = 0; i < params.size(); i++) {
                        Object obj = params.get(i);
                        ps.setObject(i + 1, obj);
                    }
                    return ps;
                }
            }, keyHolder);
            Number number = keyHolder.getKey();
            Class type = pkField.getType();
            if (type == Integer.class || type == int.class) {
                ReflectUtils.setFieldValue(pkField, domain, number.intValue());
            } else if (type == Long.class || type == long.class) {
                ReflectUtils.setFieldValue(pkField, domain, number.longValue());
            } else {
                throw new EOrmException();
            }
        } else {
            res = execute(sql, args);
        }

        if (1 != res) {
            throw new EOrmException();
        }
    }

    @Override
    public <T> void updateById(T domain) {
        Table table = EOrmDomainTableParser.init(domain.getClass());
        List<Object> values = new ArrayList<Object>();
        List<Object> params = new ArrayList<Object>();

        StringBuilder ssb = new StringBuilder();
        StringBuilder wsb = new StringBuilder();
        for (Column column : table.getColumns()) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
            if (null == value) {
                continue;
            }
            if (column.getPrimaryKey()) {
                if (wsb.length() > 0) {
                    wsb.append(" and ");
                }
                wsb.append("`");
                wsb.append(column.getName());
                wsb.append("` = ?");
                params.add(value);
            } else {
                if (ssb.length() > 0) {
                    ssb.append(", ");
                }
                ssb.append("`");
                ssb.append(column.getName());
                ssb.append("` = ?");
                values.add(value);
            }

        }


        StringBuilder sb = new StringBuilder("update ");
        sb.append(table.getName());
        sb.append(" set ");
        sb.append(ssb);
        sb.append(" where ");
        sb.append(wsb);

        String sql = sb.toString();
        values.addAll(params);
        int res = this.execute(sql, values.toArray());

        if (1 != res) {
            throw new EOrmException();
        }
    }

    @Override
    public <T> void deleteById(T domain) {
        Table table = EOrmDomainTableParser.init(domain.getClass());
        List<Object> params = new ArrayList<Object>();

        StringBuilder wsb = new StringBuilder();
        for (Column column : table.getPkColumns()) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
            if (null == value) {
                continue;
            }
            if (wsb.length() > 0) {
                wsb.append(" and ");
            }
            wsb.append("`");
            wsb.append(column.getName());
            wsb.append("` = ?");

            params.add(value);
        }
        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.getName());
        sb.append(" where ");

        sb.append(wsb);
        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());

        if (1 != res) {
            throw new EOrmException();
        }
    }

    @Override
    public <T> void deleteById(Class<T> clazz, Object... ids) {
        Table table = EOrmDomainTableParser.init(clazz);

        StringBuilder wsb = new StringBuilder();
        for (Column column : table.getPkColumns()) {
            if (wsb.length() > 0) {
                wsb.append(" and ");
            }
            wsb.append("`");
            wsb.append(column.getName());
            wsb.append("` = ?");
        }

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.getName());
        sb.append(" where ");
        sb.append(wsb);

        String sql = sb.toString();
        int res = this.execute(sql, ids);

        if (1 != res) {
            throw new EOrmException();
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
    public <T> T selectById(Class<T> clazz, Object... args) {
        // 表名
        Table table = EOrmDomainTableParser.init(clazz);
        StringBuilder sb = new StringBuilder("select * from ");
        sb.append(table.getName());
        sb.append(" where ");

        // 主键名
        StringBuilder wsb = new StringBuilder();
        for (Column column : table.getPkColumns()) {
            if (wsb.length() > 0) {
                wsb.append(" and ");
            }
            wsb.append("`");
            wsb.append(column.getName());
            wsb.append("` = ?");
        }
        sb.append(wsb);

        return selectOne(clazz, sb.toString(), args);
    }
}
