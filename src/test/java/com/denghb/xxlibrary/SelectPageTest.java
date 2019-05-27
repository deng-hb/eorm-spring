package com.denghb.xxlibrary;

import com.alibaba.fastjson.JSON;
import com.denghb.eorm.page.EPageRes;
import com.denghb.xxlibrary.domain.Student;
import com.denghb.xxlibrary.model.req.StudentPageReq;
import org.junit.Test;

import java.util.Arrays;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 02:15
 */
public class SelectPageTest extends BaseTest {

    @Test
    public void selectPage1() {
        StudentPageReq p = new StudentPageReq();
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
        EPageRes<Student> res = db.selectPage(Student.class, sql, p);
        System.out.println(res);
    }
}