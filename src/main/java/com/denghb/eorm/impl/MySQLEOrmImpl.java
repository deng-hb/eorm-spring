package com.denghb.eorm.impl;

import com.denghb.eorm.EOrm;
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
    public <T> EPageRes<T> selectPage(Class<T> clazz, String sql, Object pageReq) {
        EPageRes<T> res = new EPageRes<T>();
        Map<String, Object> params = ReflectUtils.objectToMap(pageReq);
        String totalSql = "select count(*) from (" + sql + ") temp";
        long total = selectOne(Long.class, totalSql, params);
        res.setTotal(total);

        if (null == params || params.isEmpty()) {
            List<T> list = select(clazz, sql, params);
            res.setList(list);
            return res;
        }

        StringBuilder asb = new StringBuilder();
        Object ascObject = params.get("asc");
        if (null != ascObject && ascObject instanceof List) {

            List<String> asc = (List<String>) ascObject;
            if (null != asc && !asc.isEmpty()) {
                for (String column : asc) {
                    if (0 < asb.length()) {
                        asb.append(',');
                    }
                    asb.append('`');
                    asb.append(column.replaceAll(" ", ""));
                    asb.append('`');
                }
                asb.append(" asc");
            }
        }

        StringBuilder dsb = new StringBuilder();
        Object descObject = params.get("desc");
        if (null != descObject && descObject instanceof List) {
            List<String> desc = (List<String>) descObject;
            if (null != desc && !desc.isEmpty()) {
                if (0 < asb.length()) {
                    asb.append(',');
                }
                for (String column : desc) {
                    if (0 < dsb.length()) {
                        dsb.append(',');
                    }
                    dsb.append('`');
                    dsb.append(column.replaceAll(" ", ""));
                    dsb.append('`');
                }
                dsb.append(" desc");
            }
        }

        if (asb.length() > 0 || dsb.length() > 0) {
            sql += " order by ";
        }
        sql += asb.toString();
        sql += dsb.toString();


        Object pageObject = params.get("page");
        Object pageSizeObject = params.get("pageSize");
        if (null != pageObject && null != pageSizeObject) {
            sql += " limit :pageStart, :pageSize";
            int page = (int) pageObject;
            int pageSize = (int) pageSizeObject;
            params.put("pageStart", (page - 1) * pageSize);
        }

        List<T> list = select(clazz, sql, params);
        res.setList(list);
        return res;
    }

}
