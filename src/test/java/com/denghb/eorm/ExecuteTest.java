package com.denghb.eorm;

import org.junit.Test;

/**
 * @author denghb
 * @since 2019-07-14 18:37
 */
public class ExecuteTest extends AppTest {

    @Test
    public void test1() {
        String sql = ""/*{
            update tb_user set created_time = now() where id in (select id from (select id from tb_user limit 10) t)
        }*/;
        int res = db.execute(sql);
        log.info(res);
    }
}
