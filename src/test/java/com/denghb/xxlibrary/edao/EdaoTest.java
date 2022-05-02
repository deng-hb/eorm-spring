package com.denghb.xxlibrary.edao;

import com.denghb.eorm.Edao;
import com.denghb.xxlibrary.domain.Student;
import com.denghb.xxlibrary.test.BaseTest;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EdaoTest extends BaseTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext app = new AnnotationConfigApplicationContext();




    }

    @Test
    public void test1() {


        StudentDao studentDao = ctx.getBean(StudentDao.class);

        Student student = new Student();

        studentDao.insert(student);

        System.out.println("aaa");
    }

    @Test
    public void test2() {
        StudentDao studentDao = ctx.getBean(StudentDao.class);
        List<Student> students = studentDao.selectYoung();
        System.out.println(students);

    }
}
