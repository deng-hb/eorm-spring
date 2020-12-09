/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.test;

import com.denghb.eorm.Core;
import com.denghb.eorm.Eorm;
import com.denghb.eorm.impl.CoreImpl;
import com.denghb.eorm.support.EPrepareStatementHandler;
import com.denghb.xxlibrary.domain.Student;
import com.denghb.xxlibrary.model.ReadRecordModel;
import com.denghb.xxlibrary.model.TestSubSelectModel;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
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

        Eorm eorm = ctx2.getBean(Eorm.class);


        Core core = (Core) ctx2.getBean("core");


        String tsql = ""/*{
            select s.*, s1.* from student s inner join (
                select a.id, a.name from student a limit 10
            ) s1 on s1.id = s.id limit 10
        }*/;
        List<TestSubSelectModel> list0 = core.select(TestSubSelectModel.class, tsql);
        List<TestSubSelectModel> list = core.select(TestSubSelectModel.class, "select * from book b,read_record,student s where student_id = s.id and book_id = b.id limit 10");
        List<ReadRecordModel> list2 = core.select(ReadRecordModel.class, "select * from book,read_record,student where student_id = student.id and book_id = book.id limit 10");

        EPrepareStatementHandler<Integer> total = new EPrepareStatementHandler<Integer>() {
            @Override
            public Integer onExecute(PreparedStatement ps) throws SQLException {

                ResultSet rs = ps.executeQuery("select found_rows() as total_count");
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        };
        // 没有count快
        List<Student> list3 = core.select(Student.class, "select sql_calc_found_rows * from student limit 10", total);
        int rows = total.getResult();

        Integer id = 0;
        EPrepareStatementHandler<Integer> key = new EPrepareStatementHandler<Integer>() {
            @Override
            public Integer onExecute(PreparedStatement ps) throws SQLException {
                ResultSet rs = ps.executeQuery("select last_insert_id() as id");
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        };
        int s0 = core.execute("insert into student (name,gender) values (1,0)", key);

        int id2 = key.getResult();
        int s1 = core.execute("update student set age = 2 where id = ? and name = ?", new Integer[]{1111, 2222});
        int s = core.execute("update student set age = 2 where id = ?", 100011);
        System.out.println(s);
    }

}
