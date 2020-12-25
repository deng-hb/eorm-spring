/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.test;

import com.denghb.eorm.Core;
import com.denghb.eorm.EOrm;
import com.denghb.eorm.EOrmX;
import com.denghb.eorm.support.EKeyHolder;
import com.denghb.eorm.support.ESQLSegment;
import com.denghb.xxlibrary.domain.Student;
import com.denghb.xxlibrary.domain.TbTest1;
import com.denghb.xxlibrary.model.ReadRecordModel;
import com.denghb.xxlibrary.model.TestSubSelectModel;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/11/29 23:54
 */
public class CoreTest {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ClassPathXmlApplicationContext ctx2 = new ClassPathXmlApplicationContext("classpath:spring.xml");

        EOrm eorm = ctx2.getBean(EOrm.class);
        EOrmX db = ctx2.getBean(EOrmX.class);

        Core core = ctx2.getBean(Core.class);

        List<Student> students1 = core.select(Student.class, "select * from student limit 1");

        core.execute("insert into tb_test1(id2) values (2),(3)", new EKeyHolder());


        List<Student> students = db.select(new ESQLSegment<Student>().eq(Student::setAge, 40));

        TbTest1 test = new TbTest1();
        test.setId2(123232);
        eorm.insert(test);

        String tsql = ""/*{
            select s.*, s1.* from student s inner join (
                select a.id, a.name from student a limit 10
            ) s1 on s1.id = s.id limit 10
        }*/;
        List<TestSubSelectModel> list0 = eorm.select(TestSubSelectModel.class, tsql);
        List<TestSubSelectModel> list = core.select(TestSubSelectModel.class, "select * from book b,read_record,student s where student_id = s.id and book_id = b.id limit 10");
        List<ReadRecordModel> list2 = core.select(ReadRecordModel.class, "select * from book,read_record,student where student_id = student.id and book_id = book.id limit 10");

        // 没有count快
        List<Student> list3 = core.select(Student.class, "select sql_calc_found_rows * from student limit 10");

        Integer id = 0;
        int s0 = core.execute("insert into student (name,gender) values (1,0)");

        int s1 = core.execute("update student set age = 2 where id = ? and name = ?", new Integer[]{1111, 2222});
        int s = core.execute("update student set age = 2 where id = ?", 100011);
        System.out.println(s);
    }

}
