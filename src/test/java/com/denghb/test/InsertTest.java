package com.denghb.test;

import com.denghb.test.domain.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        log.info(user);
    }

    @Test
    public void test2() throws InterruptedException {

        final String mobile = 1 + RandomStringUtils.randomNumeric(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.submit(new InsertThread(mobile));
        }

        // 5秒插入数量
        Thread.sleep(5000);

        String sql = ""/*{
        select count(*) from tb_user where mobile = ?
        }*/;

        int count = db.selectOne(Integer.class, sql, mobile);

        log.info("5000ms insert:" + count);
    }

    public class InsertThread implements Runnable {
        String mobile;

        public InsertThread(String mobile) {
            this.mobile = mobile;
        }

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
    }
}
