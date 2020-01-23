package com.denghb.eorm;

import com.denghb.eorm.domain.User;
import org.junit.Test;

/**
 * @author denghb
 * @since 2019-07-14 18:37
 */
public class ExecuteTest extends AppTest {

    @Test
    public void test1() {
        String sql = ""/*{
            update tb_user set created_time = now() where id in (
                select id from (select id from tb_user limit 10) t
            )
        }*/;
        int res = db.execute(sql);
        log.info(res);
    }

    @Test
    public void test2() {
        String sql = ""/*{
            update tb_user set created_time = now(), mobile = :mobile where id = :id
            #if (null != #gender)
                and gender = :gender
            #end
        }*/;

        User user = new User();
        user.setId(1);
        user.setMobile("123423423");

        int res = db.execute(sql,user);
        assert res == 1;
        log.info(res);
    }
}
