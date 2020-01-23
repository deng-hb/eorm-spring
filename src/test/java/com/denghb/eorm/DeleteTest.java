package com.denghb.eorm;

import com.denghb.eorm.domain.User;
import org.junit.Test;

/**
 * @author denghb
 * @since 2019-07-14 18:33
 */
public class DeleteTest extends AppTest {

    @Test
    public void test1() {
        String openId = genOpenId();
        User user = new User();
        user.setOpenId(openId);

        user.setMobile("1234234234");
        db.insert(user);
        User user1 = db.selectByPrimaryKey(User.class, user.getId());
        log.info(user1);

        db.delete(user);


        String openId2 = genOpenId();
        User user2 = new User();
        user2.setOpenId(openId);

        user2.setMobile("1234234234");
        db.insert(user2);
        db.delete(User.class, user2.getId());

    }
}
