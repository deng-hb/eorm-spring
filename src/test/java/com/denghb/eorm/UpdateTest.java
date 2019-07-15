package com.denghb.eorm;

import com.denghb.eorm.domain.User;
import org.junit.Test;

/**
 * @author denghb
 * @since 2019-07-14 18:33
 */
public class UpdateTest extends AppTest {

    @Test
    public void test1() {
        String openId = genOpenId();
        User user = new User();
        user.setOpenId(openId);

        user.setMobile("1234234234");
        db.insert(user);
        User user1 = db.selectByPrimaryKey(User.class, user.getId());
        log.info(user1);

        user.setMobile("1231231231");
        db.update(user);

        User user2 = db.selectByPrimaryKey(User.class, user.getId());
        log.info(user2);
    }
}
