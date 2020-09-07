package com.denghb.xxlibrary.test;

import com.denghb.xxlibrary.domain.Student;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author denghb
 * @Date: 2019-05-26 00:49
 */
public class DeleteByIdTest extends BaseTest {

    @Transactional
    @Test
    public void deleteById() {

        Student student = new Student();
        student.setId(11111);
        student.setName("123123131");
        student.setGender(1);
        db.insert(student);

        db.deleteById(student);
    }
}
