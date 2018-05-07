package com.denghb.eorm.utils;

public class JdbcUtils {


    /**
     * 驼峰格式转换为下划线格式
     */
    public static String humpToUnderline(String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i > 0 && Character.isUpperCase(ch)) {// 首字母是大写不需要添加下划线
                builder.append('_');
            }
            builder.append(ch);
        }

        int startIndex = 0;
        if (builder.charAt(0) == '_') {//如果以下划线开头则忽略第一个下划线
            startIndex = 1;
        }
        return builder.substring(startIndex).toLowerCase();
    }


    /**
     * 下划线格式转换为驼峰格式
     */
    public static String underlineToHump(String name, boolean firstCharToUpper) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i == 0 && firstCharToUpper) {
                builder.append(Character.toUpperCase(ch));
            } else {
                if (i > 0 && ch == '_') {// 首字母是大写不需要添加下划线
                    i++;
                    ch = name.charAt(i);
                    builder.append(Character.toUpperCase(ch));
                } else {
                    builder.append(ch);
                }
            }
        }
        return builder.toString();
    }


}
