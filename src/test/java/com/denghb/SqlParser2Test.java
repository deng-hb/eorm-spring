package com.denghb;

import java.util.*;

public class SqlParser2Test {

    public static void main(String[] args) {
        String sql = ""/*{
            select * from user where 1 = 1
            #if (name)
                and name like :name
            #end
            #if (age)
                and age = :age
            #end
            #if (ids)
                and id in (:ids)
            #end
            #if (xyz)
                and id in (:ids)
            #end
            limit :pageStart, :pageSize
        }*/;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "%张%");
        params.put("age", 18);
        params.put("ids", Arrays.asList(1, 2, 3));
        params.put("ids", Arrays.asList(1, 2, 3));
        params.put("ids", Arrays.asList(1, 2, 3));
        params.put("ids", Arrays.asList(1, 2, 3));

        long start = System.currentTimeMillis();
        System.out.println(parse(sql, params));
//        System.out.println(sql);
        System.out.println(System.currentTimeMillis() - start);
    }


    public static String parse(String sqlTemplate, Map<String, Object> params) {

//        sqlTemplate = sqlTemplate.replaceAll("\n", " ");
//        sqlTemplate = sqlTemplate.replaceAll("   ", " ");
//        sqlTemplate = sqlTemplate.replaceAll("  ", " ");
//        sqlTemplate = sqlTemplate.replaceAll("   ", " ");
//        sqlTemplate = sqlTemplate.replaceAll("  ", " ");

        StringBuilder sql = new StringBuilder();
        boolean append = true;
        for (int i = 0; i < sqlTemplate.length(); i++) {
            char c = sqlTemplate.charAt(i);

            // #if
            if ('#' == c && 'i' == sqlTemplate.charAt(i + 1) && 'f' == sqlTemplate.charAt(i + 2)) {
                i += 3;
                append = false;
                StringBuilder name = new StringBuilder();
                for (; i < sqlTemplate.length(); i++) {
                    c = sqlTemplate.charAt(i);
                    if (')' == c) {
                        break;
                    }
                    if ('(' != c && ' ' != c) {
                        name.append(c);
                    }
                }
                Object object = params.get(name.toString());
                if (null != object) {
                    if (object instanceof Boolean) {
                        append = (Boolean) object;
                    } else if (object instanceof List) {
                        append = !((Collection) object).isEmpty();
                    } else if (object instanceof CharSequence) {
                        append = String.valueOf(object).trim().length() > 0;
                    } else if (object instanceof Number) {
                        append = true;
                    } else if (object instanceof Date) {
                        append = true;
                    }
                }
            } else if ('#' == c && 'e' == sqlTemplate.charAt(i + 1) && 'n' == sqlTemplate.charAt(i + 2)
                    && 'd' == sqlTemplate.charAt(i + 3)) {
                // #end
                i = i + 4;
                append = true;
            } else if (append) {
                sql.append(c);
            }
        }
        return sql.toString();
    }
}
