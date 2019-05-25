package com.denghb;

import com.denghb.eorm.EOrm;
import com.denghb.eorm.impl.MySQLEOrmImpl;
import com.denghb.eorm.parse.EOrmQueryTemplateParser;
import com.denghb.model.Pager;
import com.denghb.model.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unit test for simple App.
 */
@Service
public class AppTest {

    private ApplicationContext ctx;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private JdbcTemplate jdbcTemplate;
    private EOrm db;

    @Before
    public void before() {
        ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");
        jdbcTemplate = ctx.getBean(JdbcTemplate.class);
        namedParameterJdbcTemplate = ctx.getBean(NamedParameterJdbcTemplate.class);
        db = ctx.getBean(EOrm.class);
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void test() {


        // XML 配置

        System.out.println(db);
        Long count = db.selectOne(Long.class, "select count(*) from user");
        System.out.println(count);

        // 代码创建
        EOrm db2 = new MySQLEOrmImpl(jdbcTemplate, namedParameterJdbcTemplate);
        System.out.println(db2);
        Long count2 = db2.selectOne(Long.class, "select count(*) from user");
        System.out.println(count2);


        User user = new User();

        user.setAge(10);
        user.setMobile("123123123");
        db2.insert(user);

        System.out.println(user);

    }

    InheritableThreadLocal<Integer> idHub = new InheritableThreadLocal<Integer>();

    @Test
    public void test2() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    User user = new User();


                    user.setAge(10);
                    user.setMobile("123123123");
                    db.insert(user);

                    idHub.set(user.getId());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // XML 配置
                            User user = new User();
                            user.setId(idHub.get());
                            user.setEmail("test@test.com");
                            db.updateById(user);
                            idHub.remove();
                        }
                    }).start();
                }
            }).start();
        }


        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void testName() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");


        String sql = "select * from user where id in (:ids)";
        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("ids", new int[]{1,2,3}); // fail
//        map.put("ids", Arrays.asList(1, 2, 3));

        Pager p = new Pager() {
            private List<Integer> ids = Arrays.asList(1, 2, 3);

            public List<Integer> getIds() {
                return ids;
            }

            public void setIds(List<Integer> ids) {
                this.ids = ids;
            }
        };

        List<User> list = namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(p), BeanPropertyRowMapper.newInstance(User.class));

        System.out.println(list);
    }


    @Test
    public void testPager() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");


        NamedParameterJdbcTemplate namedParameterJdbcTemplate = ctx.getBean(NamedParameterJdbcTemplate.class);

        String sql = ""/*{
        select * from user where 1 = 1

        #ifNotBlank(name)
                and name like :name
            #end
            #ifNotEmpty(ids)
                and id in (:ids)
            #end limit :pageStart, :pageSize
        }*/;
        Pager p = new Pager() {

            private String name;// = "%x";

            private List<Integer> ids = Arrays.asList(1, 2, 3);

            public List<Integer> getIds() {
                return ids;
            }

            public void setIds(List<Integer> ids) {
                this.ids = ids;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        };
        p.setPage(1);

        sql = EOrmQueryTemplateParser.parse(sql, p);

        List<User> list = namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(p), BeanPropertyRowMapper.newInstance(User.class));

        System.out.println(list);
    }
}
