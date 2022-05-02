package com.denghb.xxlibrary.esql;

import com.denghb.eorm.sql.Esql;
import com.denghb.xxlibrary.domain.Student;
import com.denghb.xxlibrary.edao.StudentDao;
import com.denghb.xxlibrary.test.BaseTest;
import org.junit.Test;

import java.util.List;

public class EsqlTest extends BaseTest {
    public static void main(String[] args) {


        Esql<Student> sql = new Esql<Student>();
        sql.select(Student::setId, Student::setGender)
                //.count()
                .from(Student.class)
                .where()
                .eq(Student::setName, "zzz")
                .or().start().gt(Student::setAge, 10).and().lt(Student::setAge, 20).end()
                .orderBy(Student::setAge)
                .asc()
                .having().avg(Student::setAge).eq(10)
                .limit(10);

        System.out.println(sql);
        System.out.println(sql.getArgs());
    }

    @Test
    public void test1() {

        StudentDao studentDao = ctx.getBean(StudentDao.class);
        List<Student> list = studentDao.list(new Esql<Student>().isNull(Student::setName));
        System.out.println(list);
    }
}
