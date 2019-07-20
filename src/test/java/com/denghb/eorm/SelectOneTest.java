package com.denghb.eorm;

import com.denghb.eorm.domain.User;
import org.junit.Test;

import java.util.HashMap;

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

    @Test
    public void test2() {

        String sql = ""/*{
            select count(*) from tb_user u where u.deleted = 0
            #if (null != #nickName)
                and u.nick_name like concat('%', :nickName, '%')
            #elseIf (null != #openId)
                and u.openId = :openId
            #end 
        }*/;
        Integer count = db.selectOne(Integer.class, sql, new HashMap<String, String>() {{
            put("nickName", "张三");
        }});

        System.out.println(count);
    }

    @Test
    public void test3() {
        String sql = ""/*{
select * from tb_user
where tb_user.mobile = 's'
}*/;
        User user = db.selectOne(User.class, sql);
        log.info(user);
    }
}
