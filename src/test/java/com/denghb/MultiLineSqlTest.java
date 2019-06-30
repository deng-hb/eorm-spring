package com.denghb;

public class MultiLineSqlTest {

    public static void main(String[] args) {
        String sql = ""/*{
           SELECT count(*) from tb_user u where name is not null and u
           name

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
