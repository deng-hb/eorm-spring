/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.impl;

import com.denghb.eorm.Core;
import com.denghb.eorm.EOrmException;
import com.denghb.eorm.support.EKeyHolder;
import com.denghb.eorm.support.ETraceHolder;
import com.denghb.eorm.support.domain.EClassRef;
import com.denghb.eorm.support.domain.ESQLParameter;
import com.denghb.eorm.support.domain.ETrace;
import com.denghb.eorm.utils.EReflectUtils;
import com.denghb.eorm.utils.ESQLTemplateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/11/29 23:52
 */
public class CoreImpl implements Core {

    private DataSource dataSource;

    public CoreImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int execute(String sql, Object... args) {

        ETraceHolder.start();
        ETrace trace = ETraceHolder.get();
        String tid = trace.getId();

        Log log = LogFactory.getLog(trace.getLogName());
        boolean logDebug = log.isDebugEnabled();
        if (logDebug) {
            log.debug(MessageFormat.format("{0} -> ({1})", trace.getLogMethod(), tid));
        }

        ESQLParameter sp = ESQLTemplateUtils.parse(sql, args);
        sql = sp.getSql();
        args = sp.getArgs();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DataSourceUtils.getConnection(getDataSource());

            EKeyHolder keyHolder = sp.getKeyHolder();

            if (null != keyHolder) {
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                ps = conn.prepareStatement(sql);
            }
            int i = 1;

            StringBuilder argsLog = new StringBuilder();

            for (Object object : args) {
                if (object instanceof EKeyHolder) {
                    continue;
                }
                ps.setObject(i, object);
                if (i > 1) {
                    argsLog.append(", ");
                }
                argsLog.append(object);
                i++;
            }

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) -> Parameters  :{1}", tid, argsLog));
                log.debug(MessageFormat.format("({0}) -> Execute SQL :{1}", tid, sql));
            }
            int rows = ps.executeUpdate();

            if (null != keyHolder) {
                rs = ps.getGeneratedKeys();
                List<Object> list = new ArrayList<Object>();
                while (rs.next()) {
                    list.add(rs.getObject(1));
                }
                keyHolder.setKeys(list);
            }

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) <- Affected    :{1}", tid, rows));
                log.debug(MessageFormat.format("({0}) <- Taking      :{1}ms", tid, (System.currentTimeMillis() - trace.getStartTime())));
            }
            return rows;
        } catch (Exception e) {
            throw new EOrmException(e.getMessage(), e);
        } finally {
            //关闭资源
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            //释放资源
            DataSourceUtils.releaseConnection(conn, getDataSource());

            ETraceHolder.end();
        }
    }

    @Override
    public <T> List<T> select(Class<T> clazz, String sql, Object... args) {
        ETraceHolder.start();
        ETrace trace = ETraceHolder.get();
        String tid = trace.getId();

        Log log = LogFactory.getLog(trace.getLogName());
        boolean logDebug = log.isDebugEnabled();
        if (logDebug) {
            log.debug(MessageFormat.format("{0} -> ({1})", trace.getLogMethod(), tid));
        }
        ESQLParameter sp = ESQLTemplateUtils.parse(sql, args);
        sql = sp.getSql();
        args = sp.getArgs();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> list = null;
        try {
            conn = DataSourceUtils.getConnection(getDataSource());
            ps = conn.prepareStatement(sql);
            int i = 1;

            StringBuilder argsLog = new StringBuilder();
            for (Object object : args) {
                ps.setObject(i, object);
                if (i > 1) {
                    argsLog.append(", ");
                }
                argsLog.append(object);
                i++;
            }

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) -> Parameters  :{1}", tid, argsLog));
                log.debug(MessageFormat.format("({0}) -> Execute SQL :{1}", tid, sql));
            }
            rs = ps.executeQuery();
            list = toList(rs, clazz);

            if (logDebug) {
                log.debug(MessageFormat.format("({0}) <- Affected    :{1}", tid, list.size()));
                log.debug(MessageFormat.format("({0}) <- Taking      :{1}ms", tid, (System.currentTimeMillis() - trace.getStartTime())));
            }
            return list;

        } catch (Exception e) {
            throw new EOrmException(e.getMessage(), e);
        } finally {
            //关闭资源
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            //释放资源
            DataSourceUtils.releaseConnection(conn, getDataSource());

            ETraceHolder.end();

        }
    }

    protected List<Map<String, Object>> toList(ResultSet rs) throws Exception {
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> data = new HashMap<String, Object>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(data);
        }
        return list;
    }

    /**
     * int.class, Integer.class, String.class, Date.class
     * Entity.class, DTO.class
     * Map.class
     *
     * @param rs
     * @param clazz
     * @param <T>
     * @return
     * @throws SQLException
     */
    protected <T> List<T> toList(ResultSet rs, Class<T> clazz) throws Exception {
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();

        List<T> list = new ArrayList<T>();
        if (EReflectUtils.isSingleClass(clazz)) {
            while (rs.next()) {
                list.add(EReflectUtils.convert(rs.getObject(1), clazz));
            }
        } else {

            EClassRef classRef = EReflectUtils.getClassRef(clazz);
            T result = null;
            while (rs.next()) {
                result = clazz.newInstance();
                list.add(result);
                List<String> tableNames = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String tableName = md.getTableName(i);
                    if (tableNames.contains(tableName)) {
                        continue;
                    }
                    EClassRef.ESubClassRef subClassRef = classRef.getTableMap().get(tableName);
                    if (null != subClassRef) {
                        tableNames.add(tableName);

                        Field object2Field = subClassRef.getField();
                        Object object2 = EReflectUtils.constructorInstance(object2Field.getType());
                        EReflectUtils.setFieldValue(object2Field, result, object2);

                        for (int j = i; j <= columnCount; j++) {
                            String columnName2 = EReflectUtils.convertName(md.getColumnName(j));
                            Field field2 = subClassRef.getColumnMap().get(columnName2);
                            if (null != field2) {
                                EReflectUtils.setFieldValue(field2, object2, rs.getObject(j));
                            }
                        }

                    } else {
                        String columnName = EReflectUtils.convertName(md.getColumnName(i));
                        Field field = classRef.getColumnMap().get(columnName);
                        if (null != field) {
                            EReflectUtils.setFieldValue(field, result, rs.getObject(i));
                        }
                    }

                }
            }
        }
        return list;
    }
}
