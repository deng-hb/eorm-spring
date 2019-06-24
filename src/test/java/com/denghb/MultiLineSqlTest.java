package com.denghb;

public class MultiLineSqlTest {

    public static void main(String[] args) {
        String sql = ""/*{
           select count(*) from tb_user where name is not null and
           deleted = 0 and name like '张%'
           order by age desc limit 10
        }*/;

        System.out.println(sql);


        String key = " and ";
        doRun(0, sql, key);


    }

    private static void doRun(int start, String source, String key) {
        int i = source.indexOf(key);
        if (-1 < i) {

            String next = source.substring(i + key.length());
            System.out.println(next);
            doRun(start, next, key);
        }

    }
}
