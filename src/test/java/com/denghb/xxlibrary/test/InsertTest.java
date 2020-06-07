package com.denghb.xxlibrary.test;

import com.denghb.xxlibrary.domain.Student;
import org.junit.Test;

/**
 * @author denghb
 * @Date: 2019-05-26 00:49
 */
public class InsertTest extends BaseTest {

    @Test
    public void insert1() {
        Student user = new Student();
        user.setName("张三");
        user.setGender(1);
        db.insert(user);

    }

    @Test
    public void insert2() {

//        db.deleteById(Student.class, 2L);
        db.execute("delete from `xxlibrary`.`student` where `id` = ?", 2L);

        Student user = new Student();
        user.setId(2L);
        user.setName("张三");
        user.setGender(0);
        db.insert(user);

    }
}
