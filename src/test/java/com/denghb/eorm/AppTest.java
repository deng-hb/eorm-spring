package com.denghb.eorm;

import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;
import com.denghb.eorm.impl.EormMySQLImpl;
import com.denghb.eorm.domain.User;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unit test for simple App.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring.xml")
public class AppTest {

    protected Logger log = Logger.getLogger(this.getClass());

    @Autowired
    protected Eorm db;

    protected String genOpenId() {
        String openId = UUID.randomUUID().toString().replaceAll("-", "");
        return openId;
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void test() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");

        // XML 配置
        Eorm db = ctx.getBean(Eorm.class);
        System.out.println(db);
        Long count = db.selectOne(Long.class, "select count(*) from tb_user");
        System.out.println(count);

        // 代码创建
        JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
        Eorm db2 = new EormMySQLImpl(jdbcTemplate);
        System.out.println(db2);
        Long count2 = db2.selectOne(Long.class, "select count(*) from tb_user");
        System.out.println(count2);


        User user = new User();

        user.setOpenId(genOpenId());
        user.setMobile("123123123");
        db2.insert(user);

        System.out.println(user);

        PagingResult<User> result = db2.page(User.class, new StringBuffer("select * from tb_user"), new Paging() {
            @Override
            public String[] getSorts() {
                return new String[]{"id"};
            }
        });

        System.out.println(result);
    }

    InheritableThreadLocal<Integer> idHub = new InheritableThreadLocal<Integer>();

    @Test
    public void test2() throws InterruptedException {
        final ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    User user = new User();

                    // XML 配置
                    Eorm db = ctx.getBean(Eorm.class);

                    user.setOpenId(genOpenId());
                    user.setMobile("123123123");
                    db.insert(user);

                    //idHub.set(user.getId());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // XML 配置
                            Eorm db = ctx.getBean(Eorm.class);
                            User user = new User();
                            user.setId(idHub.get());
                            db.update(user);
                            idHub.remove();
                        }
                    });
                }
            }).start();
        }


        Thread.sleep(Long.MAX_VALUE);
    }

}
