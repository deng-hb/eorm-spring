package com.denghb.eorm;

import com.denghb.eorm.page.EPageReq;
import com.denghb.eorm.page.EPageRes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Set;

/**
 * MySQL 实现
 */
public class EOrmMySQLImpl extends EOrmImpl implements EOrm {

    public EOrmMySQLImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Override
    public <T> EPageRes<T> selectPage(Class<T> clazz, String sql, EPageReq pageReq) {
        EPageRes<T> res = new EPageRes<T>();
        String totalSql = "select count(*) from (" + sql + ") temp";
        long total = selectOne(Long.class, totalSql, pageReq);
        res.setTotal(total);

        sql += buildOrderBy(pageReq);

        if (0 < pageReq.getPage() && 0 < pageReq.getPageSize()) {
            sql += " limit :pageStart, :pageSize";
        }

        List<T> list = select(clazz, sql, pageReq);
        res.setList(list);
        return res;
    }

    private String buildOrderBy(EPageReq pageReq) {

        //获取所有预置可排序字段
        Set<String> sorts = pageReq.getSorts();
        if (null == sorts || sorts.isEmpty()) {
            return "";
        }


        // asc append
        StringBuilder asb = new StringBuilder();
        Set<String> asc = pageReq.getAsc();
        if (null != asc && !asc.isEmpty()) {
            for (String column : asc) {
                if (!sorts.contains(column)) {
                    throw new EOrmException("column [" + column + "] undefined sort!");
                }
                if (0 < asb.length()) {
                    asb.append(',');
                }
                asb.append('`');
                asb.append(column);
                asb.append('`');
            }
            asb.append(" asc");
        }

        StringBuilder dsb = new StringBuilder();
        Set<String> desc = pageReq.getDesc();
        if (null != desc && !desc.isEmpty()) {
            if (0 < asb.length()) {
                asb.append(',');
            }
            for (String column : desc) {
                if (!sorts.contains(column)) {
                    throw new EOrmException("column [" + column + "] undefined sort!");
                }
                if (0 < dsb.length()) {
                    dsb.append(',');
                }
                dsb.append('`');
                dsb.append(column);
                dsb.append('`');
            }
            dsb.append(" desc");

        }

        if (asb.length() > 0 || dsb.length() > 0) {
            return " order by " + asb.toString() + dsb.toString();
        }
        return "";
    }
}
