package com.denghb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SqlParserTest {

    public static void main(String[] args) {
        String sql = ""/*{
            select * from user where 1 = 1
            #ifNotBlank(name)
                and name like :name
            #end
            #ifNotNull(age)
                and age = :age
            #end
            #ifNotEmpty(ids)
                and id in (:ids)
            #end
            limit :pageStart, :pageSize
        }*/;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "%张%");
        params.put("age", 18);
        params.put("ids", Arrays.asList(1, 2, 3));

        long start = System.currentTimeMillis();
//        System.out.println(SqlTemplateUtils.parse(sql, params));
//        System.out.println(sql);
        System.out.println(System.currentTimeMillis() - start);
    }


}
