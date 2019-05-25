package com.denghb.eorm.impl;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.page.EPageReq;
import com.denghb.eorm.page.EPageRes;
import com.denghb.eorm.utils.ReflectUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * MySQL 实现
 */
public class MySQLEOrmImpl extends AbstractEOrmImpl implements EOrm {

    public MySQLEOrmImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Override
    public <T> EPageRes<T> selectPage(Class<T> clazz, String sql, EPageReq pageReq) {
        EPageRes<T> res = new EPageRes<T>();
        Map<String, Object> params = ReflectUtils.objectToMap(pageReq);
// TODO order by

        String totalSql = "select count(*) from (" + sql + ") temp";
        long total = selectOne(Long.class, totalSql, params);
        res.setTotal(total);

        List<String> asc = pageReq.getAsc();
        List<String> desc = pageReq.getDesc();
        StringBuilder asb = new StringBuilder();
        StringBuilder dsb = new StringBuilder();
        if (!asc.isEmpty() || !desc.isEmpty()) {
            sql += " order by ";
        }
        if (!asc.isEmpty()) {
            for (String column : asc) {
                if (0 < asb.length()) {
                    asb.append(',');
                }
                asb.append('`');
                asb.append(column);
                asb.append('`');
            }
            asb.append(" asc");
        }

        if (!desc.isEmpty()) {
            if (0 < asb.length()) {
                asb.append(',');
            }
            for (String column : desc) {
                if (0 < dsb.length()) {
                    dsb.append(',');
                }
                dsb.append('`');
                dsb.append(column);
                dsb.append('`');
            }
            dsb.append(" desc");
        }

        sql += asb.toString();
        sql += dsb.toString();
        sql += " limit :pageStart, :pageSize";
        params.put("pageStart", pageReq.getPageStart());

        List<T> list = select(clazz, sql, params);
        res.setList(list);
        return res;
    }

}
