package com.denghb.eorm.impl;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.EOrmException;
import com.denghb.eorm.support.EPrepareStatementHandler;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.ETraceHolder;
import com.denghb.eorm.support.EAsteriskColumn;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.EReflectUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基本实现
 */
public class EOrmImpl extends CoreImpl implements EOrm {

    public EOrmImpl(DataSource dataSource) {
        super(dataSource);
    }

    private <T> List<T> loadAsteriskListMap(List<Map<String, Object>> mapList, Class<T> clazz, EAsteriskColumn ac) {

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
                        } else if (subFieldType == Boolean.class && !(v instanceof Boolean)) {
                            v = "1".equals(v);
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
        ETraceHolder.start();
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

        EPrepareStatementHandler<Object> key = null;

        // 主键是否自动赋值
        if (pkColumns.size() == 1 && null == primaryKeyValue && Number.class.isAssignableFrom(primaryKeyFiled.getType())) {
            Field finalPrimaryKeyFiled = primaryKeyFiled;
            key = new EPrepareStatementHandler<Object>() {
                @Override
                public Object onExecute(PreparedStatement ps) throws SQLException {
                    ResultSet rs = ps.executeQuery("select last_insert_id() as id");
                    if (rs.next()) {
                        return getConversionService().convert(rs.getObject(1), finalPrimaryKeyFiled.getType());
                    }
                    return null;
                }
            };

            Object number = key.getResult();
            EReflectUtils.setFieldValue(primaryKeyFiled, domain, number);
        }
        res = execute(sql, args, key);
        if (1 != res) {
            throw new EOrmException("insert fail");
        }
    }

    @Override
    public <T> void updateById(T domain) {
        ETraceHolder.start();
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
            throw new EOrmException("updateById fail");
        }
    }

    @Override
    public <T> void deleteById(T domain) {
        ETraceHolder.start();
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
        ETraceHolder.start();
        Table table = ETableColumnParser.load(clazz);

        String sql = String.format("delete from %s %s", table.getName(),
                ETableColumnParser.loadWherePrimaryKey(table));
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
        Table table = ETableColumnParser.load(clazz);

        String sql = String.format("select %s from %s %s", ETableColumnParser.loadAllColumnName(table),
                table.getName(), ETableColumnParser.loadWherePrimaryKey(table));
        return selectOne(clazz, sql, ids);
    }

}
