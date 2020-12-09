/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.impl;

import com.denghb.eorm.Core;
import com.denghb.eorm.EormException;
import com.denghb.eorm.annotation.Etable;
import com.denghb.eorm.support.EPrepareStatementHandler;
import com.denghb.eorm.utils.EReflectUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DataSourceUtils.getConnection(getDataSource());
            ps = conn.prepareStatement(sql);
            int i = 1;
            EPrepareStatementHandler handler = null;
            for (Object object : args) {
                if (object instanceof EPrepareStatementHandler) {
                    handler = (EPrepareStatementHandler) object;
                    continue;
                }
                ps.setObject(i, object);
                //StatementCreatorUtils.setParameterValue(ps, i, StatementCreatorUtils.javaTypeToSqlParameterType(object.getClass()), object);
                i++;
            }

            int rows = ps.executeUpdate();

            // , Statement.RETURN_GENERATED_KEYS
            // ResultSet rs = ps.getGeneratedKeys();
//            ResultSet rs = ps.executeQuery("select last_insert_id() as id");
            if (null != handler) {
                Object result = handler.onExecute(ps);
                handler.setResult(result);
            }
            return rows;
        } catch (Exception e) {
            throw new EormException(e.getMessage());
        } finally {
            //关闭资源
            JdbcUtils.closeStatement(ps);
            //释放资源
            DataSourceUtils.releaseConnection(conn, getDataSource());
        }
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
    private <T> List<T> toList(ResultSet rs, Class<T> clazz) throws Exception {
        ResultSetMetaData md = rs.getMetaData(); //获得结果集结构信息,元数据
        int columnCount = md.getColumnCount();   //获得列数
        ConversionService service = DefaultConversionService.getSharedInstance();

        List<T> list = new ArrayList<T>();
        if (Map.class.isAssignableFrom(clazz)) {
            while (rs.next()) {
                Map<String, Object> data = new HashMap<String, Object>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    data.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add((T) data);
            }
        } else if (EReflectUtils.isSingleClass(clazz)) {
            while (rs.next()) {
                list.add(service.convert(rs.getObject(1), clazz));
            }
        } else {

            // tableName, Field
            Map<String, Field> tableFieldMap = new HashMap<>();

            Set<Field> fields = EReflectUtils.getFields(clazz);
            Map<String, Field> fieldMap = new HashMap<>();
            for (Field field : fields) {
                String fieldName = field.getName().toLowerCase().replaceAll("_", "");
                if (!EReflectUtils.isSingleClass(field.getType())) {
                    Etable etable = field.getType().getAnnotation(Etable.class);
                    if (null != etable) {
                        tableFieldMap.put(etable.name(), field);
                    }
                    tableFieldMap.put(fieldName, field);
                } else {
                    fieldMap.put(fieldName, field);
                }
            }
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
                    Field objectField = tableFieldMap.get(tableName);
                    if (null != objectField) {
                        tableNames.add(tableName);
                        Class<?> clazz2 = objectField.getType();
                        Object object2 = EReflectUtils.constructorInstance(clazz2);
                        EReflectUtils.setFieldValue(objectField, result, object2);

                        Set<Field> field2s = EReflectUtils.getFields(clazz2);
                        Map<String, Field> field2Map = new HashMap<>();

                        for (Field field2 : field2s) {
                            String fieldName2 = field2.getName().toLowerCase().replaceAll("_", "");
                            field2Map.put(fieldName2, field2);
                        }
                        for (int j = i; j <= columnCount; j++) {
                            String columnName2 = md.getColumnName(j).toLowerCase().replaceAll("_", "");
                            Field field2 = field2Map.get(columnName2);
                            if (null != field2) {
                                EReflectUtils.setFieldValue(field2, object2, service.convert(rs.getObject(j), field2.getType()));
                            }
                        }

                    } else {
                        String columnName = md.getColumnName(i).toLowerCase().replaceAll("_", "");
                        Field field = fieldMap.get(columnName);
                        if (null != field) {
                            EReflectUtils.setFieldValue(field, result, service.convert(rs.getObject(i), field.getType()));
                        }
                    }

                }
            }
        }
        return list;
    }

    @Override
    public <T> List<T> select(Class<T> clazz, String sql, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        List<T> list = null;
        try {
            conn = DataSourceUtils.getConnection(getDataSource());
            ps = conn.prepareStatement(sql);
            int i = 1;
            EPrepareStatementHandler handler = null;
            for (Object object : args) {
                if (object instanceof EPrepareStatementHandler) {
                    handler = (EPrepareStatementHandler) object;
                    continue;
                }
                ps.setObject(i, object);
                //StatementCreatorUtils.setParameterValue(ps, i, StatementCreatorUtils.javaTypeToSqlParameterType(object.getClass()), object);
                i++;
            }
            rs = ps.executeQuery();
            list = toList(rs, clazz);

            if (null != handler) {
                Object result = handler.onExecute(ps);
                handler.setResult(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeResultSet(rs1);
            JdbcUtils.closeStatement(ps);
            //释放资源
            DataSourceUtils.releaseConnection(conn, getDataSource());
        }
        return list;
    }
}
