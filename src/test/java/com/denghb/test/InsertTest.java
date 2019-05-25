package com.denghb.test;

import com.denghb.model.User;
import org.junit.Test;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class InsertTest extends BaseTest {

    @Test
    public void insert1() {
        User user = new User();
        user.setName("张三");
        db.insert(user);

    }
}
