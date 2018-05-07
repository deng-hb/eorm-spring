package com.denghb;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.impl.EormMySQLImpl;
import com.denghb.model.User;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Unit test for simple App.
 */

public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void test() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");

        // XML 配置
        Eorm eorm = ctx.getBean(Eorm.class);
        System.out.println(eorm);
        Long count = eorm.selectOne(Long.class, "select count(*) from user");
        System.out.println(count);

        // 代码创建
        JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
        Eorm eorm2 = new EormMySQLImpl(jdbcTemplate);
        System.out.println(eorm2);
        Long count2 = eorm2.selectOne(Long.class, "select count(*) from user");
        System.out.println(count2);


        User user = new User();

        user.setAge(10);
        user.setMobile("123123123");
        eorm2.insert(user);

        System.out.println(user);
    }

}
