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

        sql += " limit :pageStart, :pageSize";
        params.put("pageStart", pageReq.getPageStart());

        List<T> list = select(clazz, sql, params);
        res.setList(list);
        return res;
    }

}
