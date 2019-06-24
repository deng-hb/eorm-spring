package com.denghb.xxlibrary;

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
        p.setPageSize(3);
//        p.setAsc(Arrays.asList("name"));
//        p.setDesc(Arrays.asList("birthday"));
        String sql = ""/*{
            select * from student where 1 = 1
            #{null != #name && #name != ''}
                and name like :name
            #end
            #if (name)
                and name like :name
            #end

        }*/;

        p.setSorts(Arrays.asList("ids"));
        p.setDesc(Arrays.asList("id"));

        EPageRes<Student> res = db.selectPage(Student.class, sql, p);
        System.out.println(res);
    }
}
