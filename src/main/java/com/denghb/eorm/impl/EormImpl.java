package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormException;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.ETraceSupport;
import com.denghb.eorm.support.domain.Trace;
import com.denghb.eorm.template.EAsteriskColumn;
import com.denghb.eorm.template.ESQLTemplate;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.EClassScannerUtils;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基本实现
 */
public class EormImpl implements Eorm {

    public Log log = LogFactory.getLog(this.getClass());

    public JdbcTemplate jdbcTemplate;

    public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private String domainPackages;

    public EormImpl(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, null);
    }

    public EormImpl(JdbcTemplate jdbcTemplate, String domainPackages) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.domainPackages = domainPackages;
        if (null != domainPackages) {
            Set<Class<?>> domains = new HashSet<>();
            String[] packages = domainPackages.split(",");
            for (String s : packages) {
                domains.addAll(EClassScannerUtils.getClasses(s));
            }
            for (Class<?> clazz : domains) {
                ETableColumnParser.load(clazz);
            }
        }
    }

    @Override
    public int execute(String sql, Object... args) {
        ETraceSupport.start();
        Trace trace = ETraceSupport.get();
        sql = ESQLTemplate.format(sql);
        String tid = trace.getId();

        Log log = LogFactory.getLog(trace.getLogName());
        boolean logDebug = log.isDebugEnabled();
        if (logDebug) {
            log.debug(MessageFormat.format("{0} -> ({1})", trace.getLogMethod(), tid));
        }
        int affected = 0;

        if (null != args && 1 == args.length && !EReflectUtils.isSingleClass(args[0].getClass())) {// namedParameter
            Object arg = args[0];
            Map<String, Object> params = EReflectUtils.objectToMap(arg);
            sql = ESQLTemplate.parse(sql, params);

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) -> Parameters  :{1}", tid, Arrays.toString(args)));
                log.debug(MessageFormat.format("({0}) -> Execute SQL :{1}", tid, sql));
            }
            affected = namedParameterJdbcTemplate.update(sql, params);
        } else {
            if (logDebug) {
                log.debug(MessageFormat.format("({0}) -> Parameters  :{1}", tid, Arrays.toString(args)));
                log.debug(MessageFormat.format("({0}) -> Execute SQL :{1}", tid, sql));
            }
            affected = jdbcTemplate.update(sql, args);
        }
        if (logDebug) {
            log.debug(MessageFormat.format("({0}) <- Affected    :{1}", tid, affected));
            log.debug(MessageFormat.format("({0}) <- Taking      :{1}ms", tid, (System.currentTimeMillis() - trace.getStartTime())));
        }
        ETraceSupport.end();

        return affected;
    }

    @Override
    public <T> List<T> select(Class<T> clazz, String sql, Object... args) {
        ETraceSupport.start();
        Trace trace = ETraceSupport.get();
        String tid = trace.getId();

        Log log = LogFactory.getLog(trace.getLogName());
        boolean logDebug = log.isDebugEnabled();
        if (logDebug) {
            log.debug(MessageFormat.format("{0} -> ({1})", trace.getLogMethod(), tid));
        }

        List<T> list = null;
        if (null != args && 1 == args.length && !EReflectUtils.isSingleClass(args[0].getClass())) {// namedParameter
            Object object = args[0];
            Map<String, Object> params = EReflectUtils.objectToMap(object);
            sql = ESQLTemplate.format(sql, params);
            sql = ESQLTemplate.parse(sql, params);

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) -> Parameters  :{1}", tid, params));
                log.debug(MessageFormat.format("({0}) -> Execute SQL :{1}", tid, sql));
            }

            if (EReflectUtils.isSingleClass(clazz)) {
                list = namedParameterJdbcTemplate.queryForList(sql, params, clazz);
            } else {
                EAsteriskColumn ac = ESQLTemplate.parseAsteriskColumn(sql, clazz);
                if (ac.getFields().isEmpty()) {
                    list = namedParameterJdbcTemplate.query(sql, params, BeanPropertyRowMapper.newInstance(clazz));
                } else {
                    list = loadAsteriskColumn(clazz, ac, params);
                }
            }
        } else {
            sql = ESQLTemplate.format(sql);

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) -> Parameters  :{1}", tid, Arrays.toString(args)));
                log.debug(MessageFormat.format("({0}) -> Execute SQL :{1}", tid, sql));
            }

            if (null == args || 0 == args.length || !sql.contains("?")) {
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

        if (logDebug) {
            log.debug(MessageFormat.format("({0}) <- Affected    :{1}", tid, list.size()));
            log.debug(MessageFormat.format("({0}) <- Taking      :{1}ms", tid, (System.currentTimeMillis() - trace.getStartTime())));
        }
        ETraceSupport.end();

        return list;
    }

    private <T> List<T> loadAsteriskColumn(Class<T> clazz, EAsteriskColumn ac, Map<String, Object> params) {
        String sql = ac.getTsql();
        List<Map<String, Object>> mapList = namedParameterJdbcTemplate.queryForList(sql, params);

        List<T> list = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            Object e = EReflectUtils.constructorInstance(clazz);
            for (String key : map.keySet()) {
                for (String fieldName : ac.getFields().keySet()) {
                    String fd = fieldName + "__";
                    if (key.contains(fd)) {
                        String realFieldName = key.substring(fd.length());
                        realFieldName = EReflectUtils.underlineToHump(realFieldName, false);

                        Field field = ac.getFields().get(fieldName);
                        Object fieldObj = EReflectUtils.getFieldValue(field, e);
                        if (null == fieldObj) {
                            fieldObj = EReflectUtils.constructorInstance(field.getType());
                            EReflectUtils.setFieldValue(field, e, fieldObj);
                        }
                        Object v = map.get(key);
                        Class<?> subFieldType = EReflectUtils.getField(field.getType(), realFieldName).getType();
                        if (subFieldType.getSuperclass() == Number.class) {
                            // 数字
                            v = EReflectUtils.constructorInstance(subFieldType, String.class, String.valueOf(v));
                        }
                        EReflectUtils.setValue(fieldObj, realFieldName, v);
                    }
                }
            }
            list.add((T) e);
        }
        return list;
    }

    @Override
    public <T> void insert(T domain) {
        ETraceSupport.start();
        Table table = ETableColumnParser.load(domain.getClass());

        StringBuilder csb = new StringBuilder();
        StringBuilder vsb = new StringBuilder();
        final List<Object> params = new ArrayList<Object>();

        Object primaryKeyValue = null;
        Field primaryKeyFiled = null;
        Column primaryKeyColumn = null;

        List<Column> pkColumns = table.getPkColumns();
        for (Column column : pkColumns) {

            Field pkFiled = column.getField();
            Object pkValue = EReflectUtils.getFieldValue(pkFiled, domain);
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

        final String sql = sb.toString();

        int res = 0;
        final Object[] args = params.toArray();
        // 主键是否自动赋值
        if (pkColumns.size() == 1 && null == primaryKeyValue && Number.class.isAssignableFrom(primaryKeyFiled.getType())) {
            Trace trace = ETraceSupport.get();
            String tid = trace.getId();
            Log log = LogFactory.getLog(trace.getLogName());
            boolean logDebug = log.isDebugEnabled();

            if (logDebug) {
                log.debug(MessageFormat.format("{0} -> ({1})", trace.getLogMethod(), tid));
                log.debug(MessageFormat.format("({0}) -> Parameters  :{1}", tid, Arrays.toString(args)));
                log.debug(MessageFormat.format("({0}) -> Execute SQL :{1}", tid, sql));
            }
            KeyHolder keyHolder = new GeneratedKeyHolder();
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
                EReflectUtils.setFieldValue(primaryKeyFiled, domain, number.intValue());
            } else if (type == Long.class || type == long.class) {
                EReflectUtils.setFieldValue(primaryKeyFiled, domain, number.longValue());
            } else {
                throw new EormException("insert set primaryKey[" + primaryKeyColumn.getName() + "] value fail");
            }

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) <- Affected    :{1}", tid, res));
                log.debug(MessageFormat.format("({0}) <- Taking      :{1}ms", tid, (System.currentTimeMillis() - trace.getStartTime())));
            }
            ETraceSupport.end();
        } else {
            res = execute(sql, args);
        }

        if (1 != res) {
            throw new EormException("insert fail");
        }
    }

    @Override
    public <T> void updateById(T domain) {
        ETraceSupport.start();
        Table table = ETableColumnParser.load(domain.getClass());
        List<Object> values = new ArrayList<Object>();

        StringBuilder ssb = new StringBuilder();
        for (Column column : table.getOtherColumns()) {
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


        StringBuilder sb = new StringBuilder("update ");
        sb.append(table.getName());
        sb.append(" set ");
        sb.append(ssb);
        sb.append(ETableColumnParser.loadWherePrimaryKey(table));

        List<Column> pkColumns = table.getPkColumns();
        for (Column column : pkColumns) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            values.add(value);
        }

        String sql = sb.toString();

        Object[] args = values.toArray();
        int res = this.execute(sql, args);

        if (1 != res) {
            throw new EormException("updateById fail");
        }
    }

    @Override
    public <T> void updateByArgs(T domain, T args) {

    }

    @Override
    public <T> void deleteById(T domain) {
        ETraceSupport.start();
        Table table = ETableColumnParser.load(domain.getClass());

        List<Object> params = new ArrayList<Object>();
        List<Column> pkColumns = table.getPkColumns();
        for (Column column : pkColumns) {
            Object value = EReflectUtils.getFieldValue(column.getField(), domain);
            params.add(value);
        }

        deleteById(domain.getClass(), params.toArray());
    }

    @Override
    public <T> void deleteById(Class<T> clazz, Object... ids) {
        ETraceSupport.start();
        Table table = ETableColumnParser.load(clazz);

        String sql = String.format("delete from %s %s", table.getName(),
                ETableColumnParser.loadWherePrimaryKey(table));
        int res = this.execute(sql, ids);

        if (1 != res) {
            throw new EormException("deleteById fail");
        }
    }

    @Override
    public <T> void deleteByArgs(T domain, T args) {

    }

    @Override
    public <T> T selectOne(Class<T> clazz, String sql, Object... args) {
        ETraceSupport.start();
        List<T> list = select(clazz, sql, args);
        if (null != list && !list.isEmpty()) {
            return list.get(0);// 忽略返回多个？
        }
        return null;
    }

    @Override
    public <T> T selectById(Class<T> clazz, Object... ids) {
        ETraceSupport.start();
        // 表名
        Table table = ETableColumnParser.load(clazz);

        String sql = String.format("select %s from %s %s", ETableColumnParser.loadAllColumnName(table),
                table.getName(), ETableColumnParser.loadWherePrimaryKey(table));
        return selectOne(clazz, sql, ids);
    }

}
