package com.denghb.test;

import com.denghb.test.domain.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

/**
 * @author denghb
 * @since 2019-07-13 22:56
 */
public class InsertTest extends AppTest {


    @Test
    public void test1() {
        User user = new User();
        user.setOpenId(genOpenId());
        db.insert(user);
        throw new RuntimeException();
    }

    @Test
    public void test2() throws InterruptedException {

        final String mobile = 1 + RandomStringUtils.randomAlphanumeric(10);
        new Thread(new Runnable() {
            @Override
            public void run() {


                User user = new User();
                user.setMobile(mobile);
                while (true) {
                    user.setOpenId(genOpenId());
                    user.setId(null);
                    db.insert(user);
                }
            }
        }).start();

        // 5秒插入数量
        Thread.sleep(5000);

        String sql = ""/*{
        select count(*) from tb_user where mobile = ?
        }*/;

        int count = db.selectOne(Integer.class, sql, mobile);

        log.info("5000ms insert:" + count);
    }

}
