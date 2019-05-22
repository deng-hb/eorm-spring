package com.denghb;

import java.util.HashMap;
import java.util.Map;

public class SqlParserTest {

    public static void main(String[] args) {
        String sql = ""/*{
            select * from user where 1 = 1
            #if (null != :name)
                and name like :name
            #end
            limit :pageStart, :pageSize
        }*/;

        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("name", "%张%");

        System.out.println(parserSql(sql, params));
    }

    // #if #end
    private static String parserSql(String sqlTemp, Map<String, Object> params) {

        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < sqlTemp.length(); i++) {
            char c = sqlTemp.charAt(i);
            if ('#' == c && 'i' == sqlTemp.charAt(i + 1) && 'f' == sqlTemp.charAt(i + 2)
                    && ' ' == sqlTemp.charAt(i + 3) && '(' == sqlTemp.charAt(i + 4)) {
                i = i + 5;

                TempIfStructure tis = new TempIfStructure();
                Object[] o1 = getString(i, sqlTemp);
                i = Integer.parseInt(String.valueOf(o1[0]));
                tis.setLeft(String.valueOf(o1[1]));

                Object[] o2 = getString(i, sqlTemp);
                i = Integer.parseInt(String.valueOf(o2[0]));
                tis.setCondition(String.valueOf(o2[1]));

                Object[] o3 = getString(i, sqlTemp);
                i = Integer.parseInt(String.valueOf(o3[0]));
                tis.setRight(String.valueOf(o3[1]));


            } else if ('#' == c && 'e' == sqlTemp.charAt(i + 1) && 'n' == sqlTemp.charAt(i + 2)
                    && 'd' == sqlTemp.charAt(i + 3)) {
                i = i + 4;
            } else {
                sql.append(c);
            }
        }

        return sql.toString();
    }

    private static Object[] getString(int i, String sqlTemp) {
        StringBuilder str = new StringBuilder();
        for (; i < sqlTemp.length(); i++) {
            char c = sqlTemp.charAt(i);
            if (' ' == c) {
                break;
            }
            if (')' == c) {
                break;
            }
            str.append(c);
        }
        i++;
        return new Object[]{i, str.toString()};
    }


    public static class TempIfStructure {

        private String left;

        private String condition;

        private String right;

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }
    }
}
