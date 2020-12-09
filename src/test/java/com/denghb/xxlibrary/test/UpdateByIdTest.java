package com.denghb.xxlibrary.test;

import com.denghb.xxlibrary.domain.Student;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * @author denghb
 * @Date: 2019-05-26 00:49
 */
@EnableAutoConfiguration
public class UpdateByIdTest extends BaseTest {

    @Test
    public void update1() {
        Student student = db.selectById(Student.class, 1L);
        if (null == student) {
            student = new Student();
            student.setGender(1);
            student.setName("张三1");
            db.insert(student);
        }
        student.setName("张三2");
        db.updateById(student);

        student = db.selectById(Student.class, student.getId());
        Assert.assertEquals("张三2", student.getName());
    }

    @Test
    public void insert2() {

        try {

            db.deleteById(Student.class, 2);
        } catch (Exception e) {

        }

        Student user = new Student();
        user.setId(2);
        user.setName("张三");
        user.setGender(0);
        db.insert(user);

    }
}
