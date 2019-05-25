package com.denghb.test;

import com.denghb.model.User;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class SelectTest extends BaseTest {

    @Test
    public void select1() {

        List<User> list = db.select(User.class, "select * from user ");

        System.out.println(list);
    }


    @Test
    public void select2() {

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张%");
        List<User> list = db.select(User.class, "select * from user where name like :name", params);
        System.out.println(list);
    }

    @Test
    public void select3() {

        String sql = ""/*{
        select * from user
        #{#name != null}
        where name like :name
        #end
        }*/;

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张%");
        List<User> list = db.select(User.class, sql, params);
        System.out.println(list);
    }
}
