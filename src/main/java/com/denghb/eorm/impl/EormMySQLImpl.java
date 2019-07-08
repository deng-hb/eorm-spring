package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormException;
import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;
import com.denghb.eorm.support.EormSupport;
import com.denghb.eorm.support.model.Column;
import com.denghb.eorm.support.model.Table;
import com.denghb.eorm.utils.ReflectUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL 批量插入和分页实现
 */
public class EormMySQLImpl extends EormAbstractImpl implements Eorm {


    public EormMySQLImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * insert into table_name(c1,c2,c3) values (?,?,?),(?,?,?),(?,?,?) ...
     *
     * @param list 对象列表
     * @param <T>  类型
     */
    public <T> void batchInsert(List<T> list) {
        if (null == list || list.isEmpty()) {
            throw new EormException("list is empty...");
        }

        // 取第一个样本，后面的值必须要和第一个一样
        T domain = list.get(0);
        Table table = EormSupport.load(domain.getClass());

        StringBuilder csb = new StringBuilder();
        List<Column> allColumns = table.getAllColumns();
        int columnSize = allColumns.size();
        for (Column column : allColumns) {
            Object value = ReflectUtils.getFieldValue(column.getField(), domain);
            if (null == value) {
                continue;
            }
            if (csb.length() > 0) {
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

            boolean append = false;
            for (Column column : allColumns) {
                Object value = ReflectUtils.getFieldValue(column.getField(), list.get(i));
                if (null == value) {
                    continue;
                }
                if (append) {
                    vsb.append(", ");
                }
                vsb.append("?");

                params.add(value);
                append = true;
            }

            vsb.append(")");

        }

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table.getName());
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

    /**
     * 分页
     *
     * @param clazz  类
     * @param sql    查询SQL
     * @param paging 分页对象
     * @param <T>    类型
     * @return 分页结果
     */
    public <T> PagingResult<T> page(Class<T> clazz, StringBuffer sql, Paging paging) {
        PagingResult<T> result = new PagingResult<T>(paging);
        Object[] objects = paging.getParams().toArray();
        if (0 == objects.length && -1 < sql.indexOf(":")) {
            objects = new Object[1];
            objects[0] = ReflectUtils.objectToMap(paging);
        }

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
