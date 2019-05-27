package com.denghb.xxlibrary;

import com.denghb.xxlibrary.domain.Student;
import org.junit.Test;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class SelectOneTest extends BaseTest {

    @Test
    public void selectOne1() {

        Student student = db.selectOne(Student.class, "select * from student ");

        System.out.println(student);
    }


}
