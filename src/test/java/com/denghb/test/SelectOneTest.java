package com.denghb.test;

import com.denghb.model.User;
import org.junit.Test;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class SelectOneTest extends BaseTest {

    @Test
    public void selectOne1() {

        User user = db.selectOne(User.class, "select * from user ");

        System.out.println(user);
    }


}
