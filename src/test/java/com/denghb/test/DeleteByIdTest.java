package com.denghb.test;

import com.denghb.model.User;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class DeleteByIdTest extends BaseTest {

    @Transactional
    @Test
    public void deleteById() {

        User user = new User();
        user.setId(11111);
        db.insert(user);

        db.deleteById(user);
    }
}
