/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.test;

import com.denghb.eorm.support.domain.ESQLParameter;
import com.denghb.eorm.utils.ESQLTemplateUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/07/19 01:20
 */
public class SQLTemplate2Test {

    @Test
    public void test1() {
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
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("month", "12");
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(1);
        ids.add(2);
        ids.add(3);
        List<Integer> ids2 = Arrays.asList(1, 2, 3);

        params.put("ids", ids);
        ESQLParameter sp = ESQLTemplateUtils.parse(sql, params);
        System.out.println(sp);
    }
}
