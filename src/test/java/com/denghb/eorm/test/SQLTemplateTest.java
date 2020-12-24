/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.test;

import com.denghb.eorm.utils.ESQLTemplateUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/07/19 01:20
 */
public class SQLTemplateTest {


    public static void main(String[] args) {
        int[] s = new int[]{1, 2, 3};
        String sql = ""/*{# 绅士手
            select * from student${month} s # 不知道
            '# sss'
            #if (1==1)
                #if (1==2)

                #elseif (1 ==1)# 这个是注释
                elseif
                #else
                    aaa
                #end
            #end
        }*/;

        Map<String, Object> params = new HashMap<>();
        params.put("month", "2017_12");

        String tsql = ESQLTemplateUtils.format(sql, params);
        System.out.println(tsql);

        System.out.println(ESQLTemplateUtils.parse(tsql, params));
    }
}
