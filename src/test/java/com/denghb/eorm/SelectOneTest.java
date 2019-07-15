package com.denghb.eorm;

import com.denghb.eorm.domain.User;
import org.junit.Test;

/**
 * @author denghb
 * @since 2019-07-14 18:09
 */
public class SelectOneTest extends AppTest {


    @Test
    public void test1() {

        User user = db.selectOne(User.class, "select open_id from tb_user where id > 0");
        log.info(user);
    }
}
