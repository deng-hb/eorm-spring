package com.denghb;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;

/**
 * @author denghb
 */
public class TemplateTest {

    private static final ExpressionParser SpEL = new SpelExpressionParser();

    public static void main(String[] args) {
        String sql = ""/*{
            select *,date_format(birthday, '%Y-%M-%i') from tb_user where deleted = 0
            # 是是是是
            #if (null != #name and '' != #name)
                and name = :name
            #end
        }*/;

        StringBuilder sb = new StringBuilder();
        boolean isChar = false;
        boolean appendSpace = false;
        boolean annotation = false;

        StandardEvaluationContext seContext = new StandardEvaluationContext();
        seContext.setVariables(new HashMap<String, Object>() {{
            put("name", "1");
        }});
        boolean a = SpEL.parseExpression("(null != #name and '' != #name)").getValue(seContext, boolean.class);
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if ('\'' == c) {
                isChar = !isChar;
            } else if (!isChar && '#' == c) {
                i = exec(sql, i, sb, seContext);
                continue;
            } else if (!isChar && (' ' == c || '\n' == c || '\r' == c || '\t' == c)) {
                if (appendSpace) {
                    sb.append(' ');
                    appendSpace = false;
                }
                continue;
            }
            appendSpace = true;
            sb.append(c);
        }

        System.out.println(sb);
    }

    private static int exec(String sql, int i, StringBuilder sb, StandardEvaluationContext seContext) {
        if (hasNextKeyword(sql, "#if", i)) {

        } else if (hasNextKeyword(sql, "#elseif", i)) {

        } else if (hasNextKeyword(sql, "#else", i)) {

        } else if (hasNextKeyword(sql, "#end", i)) {

        } else {
            // #
        }
        for (; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if ((' ' == c || '\n' == c || '\r' == c || '\t' == c)) {
                continue;
            }

        }
        return i;
    }

    /**
     * 判断接下来的字符串是否为keyword
     *
     * @param sql     模版
     * @param keyword 关键字
     * @param i       索引
     * @return 结果
     */
    private static boolean hasNextKeyword(String sql, String keyword, int i) {
        if (null == keyword || null == sql) {
            return false;
        }

        int end = i + keyword.length();
        if (sql.length() < end) {
            return false;
        }

        return keyword.equalsIgnoreCase(sql.substring(i, end));
    }
}
