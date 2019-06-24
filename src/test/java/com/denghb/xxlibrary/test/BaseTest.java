package com.denghb.xxlibrary.test;

import com.denghb.eorm.EOrm;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class BaseTest {


    protected ApplicationContext ctx;
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    protected JdbcTemplate jdbcTemplate;
    protected EOrm db;

    @Before
    public void before() {
        ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");
        jdbcTemplate = ctx.getBean(JdbcTemplate.class);
        namedParameterJdbcTemplate = ctx.getBean(NamedParameterJdbcTemplate.class);
        db = ctx.getBean(EOrm.class);
    }
}
