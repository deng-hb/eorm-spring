package com.denghb.xxlibrary.test;

import com.denghb.xxlibrary.domain.Student;
import org.junit.Test;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class SelectOneTest extends BaseTest {

    @Test
    public void selectOne1() {

        String sql = ""/*{
        select * from student where id = ?
        }*/;
        Student student = db.selectOne(Student.class, sql, 1);

        System.out.println(student);
    }


}
