package com.denghb;

import com.denghb.criteria.UserCriteria;
import com.denghb.eorm.Eorm;
import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;
import com.denghb.eorm.impl.EormMySQLImpl;
import com.denghb.domain.User;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        Eorm db = ctx.getBean(Eorm.class);
        System.out.println(db);
        Long count = db.selectOne(Long.class, "select count(*) from user");
        System.out.println(count);

        // 代码创建
        JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
        Eorm db2 = new EormMySQLImpl(jdbcTemplate);
        System.out.println(db2);
        Long count2 = db2.selectOne(Long.class, "select count(*) from user");
        System.out.println(count2);


        User user = new User();

        user.setAge(10);
        user.setMobile("123123123");
        db2.insert(user);

        System.out.println(user);

        PagingResult<User> result = db2.page(User.class, new StringBuffer("select * from user"), new Paging() {
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

                    user.setAge(10);
                    user.setMobile("123123123");
                    db.insert(user);

                    idHub.set(user.getId());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // XML 配置
                            Eorm db = ctx.getBean(Eorm.class);
                            User user = new User();
                            user.setId(idHub.get());
                            user.setEmail("test@test.com");
                            db.update(user);
                            idHub.remove();
                        }
                    }).start();
                }
            }).start();
        }


        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void testAll() {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");

        // XML 配置
        Eorm db = ctx.getBean(Eorm.class);

        User user = new User();
        user.setName("张三");
        user.setEmail("zhangsan@email.com");
        user.setMobile("13838383888");
        user.setAge(24);
        db.insert(user);

        User user1 = db.selectByPrimaryKey(User.class, user.getId());

        user.setName("张三2");
        user1.setAge(25);
        user.setEmail("zhangsan2@email.com");
        db.update(user1);

        User user2 = db.selectOne(User.class, "select * from user where age = :age and name = :name ", user1);

        db.delete(user2);

        db.insert(user2);

        List<User> list = db.select(User.class, "select * from user where email like concat('%',:email,'%')", user2);
        System.out.println(list);

        String sql = ""/*{
        select * from user where 1 = 1
        and name = '${name}'
         #notEmpty(name)
           and name = :name
         #end

         #{ null != #email }
            and email like concat('%',:email,'%')
         #end
        }*/;

        UserCriteria p = new UserCriteria();
        p.setName("张三");
        p.setEmail("@");
        PagingResult<User> result = db.page(User.class, new StringBuffer(sql), p);

        System.out.println(result);

    }
}
