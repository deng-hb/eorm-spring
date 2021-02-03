package com.denghb.eorm.test;

import com.denghb.eorm.EOrmX;
import com.denghb.eorm.support.ESQLWhere;
import com.denghb.xxlibrary.domain.Student;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2021/01/06 22:23
 */
public class EOrmXTest {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");
        EOrmX db = ctx.getBean(EOrmX.class);

        int id = 1;
        Student student0 = db.selectById(Student.class, id);
        if (null != student0) {
            db.deleteById(student0);
        }

        Student student = new Student();
        student.setId(id);
        student.setName("denny");
        student.setGender(1);
        db.insert(student);

        student.setGender(0);
        db.updateById(student);

        List<Student> list = db.select(new ESQLWhere<Student>().eq(Student::setId, id));

        List<Student> list2 = db.select(new ESQLWhere<Student>().between(Student::setId, id, 2));

        List<Student> list3 = db.select(new ESQLWhere<Student>().gte(Student::setId, id));

        Student student1 = db.selectById(Student.class, id);

        Student student2 = db.selectOne(new ESQLWhere<Student>().eq(Student::setId, id));

        student.setAge(100);
        db.update(student, new ESQLWhere<Student>().in(Student::setId, Arrays.asList(id)));
        db.delete(new ESQLWhere<Student>().eq(Student::setId, id));

    }
}
