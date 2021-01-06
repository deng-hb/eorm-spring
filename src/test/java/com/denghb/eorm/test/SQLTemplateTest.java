/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.test;

import com.denghb.eorm.support.ESQLWhere;
import com.denghb.eorm.utils.ESQLTemplateUtils;
import com.denghb.xxlibrary.domain.Student;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/07/19 01:20
 */
public class SQLTemplateTest {


    public static void main(String[] args) {
        int[] s = new int[]{1, 2, 3};
        String sql = ""/*{# 测注释
            select * from student${month} s # 不知道
            where id in (:ids)
            #if (1==1)
                #if (1==2)

                #elseif (1 ==1)# 这个是注释

                #else
                    aaa
                #end
            #end
        }*/;

        Map<String, Object> params = new HashMap<>();
        params.put("month", "2017_12");
        params.put("ids", Arrays.asList(1, 2, 3));

        String tsql = ESQLTemplateUtils.format(sql, params);
        System.out.println(tsql);

        System.out.println(ESQLTemplateUtils.parse(tsql, params));

        Supplier<Double> f4 = Math::random;

        ESQLWhere<Student> ss = new ESQLWhere<Student>()
                .gt(Student::setAge, 10)
                .eq(Student::setName, "张三")
                .in(Student::setAge, Arrays.asList(1, 2, 3));

        System.out.println(ss);

        ExecutorService es = Executors.newFixedThreadPool(1);
        es.submit(() -> {
        });
    }

}
