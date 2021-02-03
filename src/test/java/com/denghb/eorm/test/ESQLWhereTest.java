package com.denghb.eorm.test;

import com.denghb.eorm.support.ESQLWhere;
import com.denghb.xxlibrary.domain.Student;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2021/01/08 22:28
 */
public class ESQLWhereTest {

    @Test
    public void test1() {
        String sql = new ESQLWhere<Student>().eq(Student::setId, 1).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where id = ?");
    }

    @Test
    public void test2() {
        String sql = new ESQLWhere<Student>().in(Student::setId, Arrays.asList(1)).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where id in (?)");
    }

    @Test
    public void test3() {
        String sql = new ESQLWhere<Student>().notIn(Student::setId, Arrays.asList(1, 3)).eq(Student::setDeleted, false).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where id not in (?, ?) and deleted = ?");
    }

    @Test
    public void test4() {
        String sql = new ESQLWhere<Student>().between(Student::setId, 1, 3).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where id between ? and ?");
    }

    @Test
    public void test5() {
        String sql = new ESQLWhere<Student>().neq(Student::setId, 1).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where id != ?");
    }

    @Test
    public void test6() {
        String sql = new ESQLWhere<Student>().gte(Student::setId, 1).lte(Student::setAge, 18).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where id >= ? and age <= ?");
    }

    @Test
    public void test7() {
        String sql = new ESQLWhere<Student>().isNull(Student::setName).isNotNull(Student::setAge).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where name is null and age is not null");
    }

    @Test
    public void test8() {
        String sql = new ESQLWhere<Student>().like(Student::setName, "%王四%").isNotNull(Student::setAge).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where name like ? and age is not null");
    }

    @Test
    public void test9() {
        String sql = new ESQLWhere<Student>().like(Student::setName, "%王四%").lt(Student::setAge, 10).sql();
        System.out.println(sql);
        Assert.assertEquals(sql, "where name like ? and age < ?");
    }
}
