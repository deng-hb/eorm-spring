package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormException;
import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;
import com.denghb.eorm.utils.EormUtils;
import com.denghb.eorm.utils.ReflectUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MySQL 实现
 */
public class EormMySQLImpl extends EormAbstractImpl implements Eorm {


    public EormMySQLImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public <T> void insert(T domain) {

        EormUtils.TableInfo table = EormUtils.getTableInfo(domain);

        final List<Object> params = new ArrayList<Object>();

        StringBuilder csb = new StringBuilder();
        StringBuilder vsb = new StringBuilder();

        List<EormUtils.Column> columns = table.getAllColumns();
        for (int i = 0; i < columns.size(); i++) {

            EormUtils.Column column = columns.get(i);

            if (i > 0) {
                csb.append(", ");
                vsb.append(", ");
            }
            csb.append('`');
            csb.append(column.getName());
            csb.append('`');

            vsb.append("?");

            params.add(column.getValue());
        }

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table.getTableName());
        sb.append(" (");
        sb.append(csb);
        sb.append(") values (");
        sb.append(vsb);
        sb.append(")");

        final String sql = sb.toString();

        int res = 0;

        List<Field> fields = table.getAllPrimaryKeyFields();
        if (fields.size() == 1) {// 获取自增主键
            if (log.isDebugEnabled()) {
                log.debug("Params:" + Arrays.toString(params.toArray()));
                log.debug("Execute SQL:" + sql);
            }
            Field field = fields.get(0);
            final String keyColumn = EormUtils.getColumnName(field);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            res = jdbcTemplate.update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {

                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{keyColumn});
                    for (int i = 0; i < params.size(); i++) {
                        Object obj = params.get(i);
                        ps.setObject(i + 1, obj);
                    }
                    return ps;
                }
            }, keyHolder);

            // 获取自动生成的ID并填充
            Object object = ReflectUtils.getFieldValue(field, domain);
            if (null == object) {
                Number number = keyHolder.getKey();
                Class type = field.getType();
                if (type == Integer.class || type == int.class) {
                    ReflectUtils.setFieldValue(field, domain, number.intValue());
                } else if (type == Long.class || type == long.class) {
                    ReflectUtils.setFieldValue(field, domain, number.longValue());
                } else {
                    throw new EormException();
                }
            }

        } else {
            res = this.execute(sql, params.toArray());
        }
        if (1 != res) {
            throw new EormException();
        }
    }

    public <T> void batchInsert(List<T> list) {
        if (null == list || list.isEmpty()) {
            throw new EormException("list is empty...");
        }

        // 取第一个样本
        T type = list.get(0);
        EormUtils.TableInfo table = EormUtils.getTableInfo(type);

        StringBuilder csb = new StringBuilder();
        List<EormUtils.Column> allColumns = table.getAllColumns();
        int columnSize = allColumns.size();
        for (int i = 0; i < columnSize; i++) {
            EormUtils.Column column = allColumns.get(i);
            if (i > 0) {
                csb.append(", ");
            }
            csb.append('`');
            csb.append(column.getName());
            csb.append('`');

        }

        List<Object> params = new ArrayList<Object>();

        StringBuilder vsb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                vsb.append(", ");
            }

            vsb.append("(");

            table = EormUtils.getTableInfo(list.get(i));

            if (columnSize != table.getAllColumns().size()) {
                throw new EormException("column size difference ...");
            }

            for (int j = 0; j < columnSize; j++) {
                if (j > 0) {
                    vsb.append(", ");
                }
                vsb.append("?");

                params.add(table.getAllColumns().get(j).getValue());
            }

            vsb.append(")");

        }

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table.getTableName());
        sb.append(" (");
        sb.append(csb);

        sb.append(") values ");
        sb.append(vsb);

        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());
        if (res != list.size()) {
            throw new EormException();
        }
    }

    public <T> PagingResult<T> page(Class<T> clazz, StringBuffer sql, Paging paging) {
        PagingResult<T> result = new PagingResult<T>(paging);

        Object[] objects = paging.getParams().toArray();
        // 不分页 start
        long pageSize = paging.getPageSize();
        if (0 != pageSize) {
            // 总记录数
            String totalSql = "";
            if (paging.isFullTotal()) {
                totalSql = "select count(*) from (" + sql + ") temp";
            } else {
                String tempSql = sql.toString().toLowerCase();
                totalSql = "select count(*) " + sql.substring(tempSql.indexOf("from"), sql.length());
            }

            long total = this.selectOne(Long.class, totalSql, objects);

            paging.setTotal(total);
            if (0 == total) {
                return result;
            }
        }
        // 不分页 end

        // 排序
        if (paging.isSort()) {
            // 判断是否有排序字段
            String[] sorts = paging.getSorts();
            if (null != sorts && 0 < sorts.length) {
                int sortIndex = paging.getSortIndex();

                // 大于排序的长度默认最后一个
                if (sortIndex >= sorts.length) {
                    sortIndex = sorts.length - 1;
                }
                sql.append(" order by ");

                sql.append('`');
                sql.append(sorts[sortIndex]);
                sql.append('`');

                // 排序方式
                if (paging.isDesc()) {
                    sql.append(" desc");
                } else {
                    sql.append(" asc");
                }
            }
        }

        if (0 != pageSize) {
            // 分页
            sql.append(" limit ");
            sql.append(paging.getStart());
            sql.append(",");
            sql.append(pageSize);
        }

        List<T> list = select(clazz, sql.toString(), objects);
        result.setList(list);

        return result;
    }
}
