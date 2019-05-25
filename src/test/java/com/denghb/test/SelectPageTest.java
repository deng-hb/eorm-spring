package com.denghb.test;

import com.alibaba.fastjson.JSON;
import com.denghb.eorm.page.EPageRes;
import com.denghb.model.Pager;
import com.denghb.model.User;
import org.junit.Test;

import java.util.Arrays;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 02:15
 */
public class SelectPageTest extends BaseTest {

    @Test
    public void selectPage1() {
        Pager p = new Pager();
        System.out.println(JSON.toJSON(p));
        p.setAsc(Arrays.asList("name"));
        p.setDesc(Arrays.asList("age"));
        String sql = ""/*{
            select * from user where 1 = 1
            #{ null != #name && #name != '' }
                and name like :name
            #end

        }*/;


        System.out.println(JSON.toJSON(p));
        EPageRes<User> res = db.selectPage(User.class, sql, p);
        System.out.println(res);
    }
}
