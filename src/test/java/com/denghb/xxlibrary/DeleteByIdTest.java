package com.denghb.xxlibrary;

import com.denghb.xxlibrary.domain.Student;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Auther: denghb
 * @Date: 2019-05-26 00:49
 */
public class DeleteByIdTest extends BaseTest {

    @Transactional
    @Test
    public void deleteById() {

        Student student = new Student();
        student.setId(11111L);
        db.insert(student);

        db.deleteById(student);
    }
}
