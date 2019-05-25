package com.denghb;

import com.denghb.model.User;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

public class SqlParser2Test {
    static ExpressionParser ep = new SpelExpressionParser();

    public static void main(String[] args) {
        String sql = ""/*{
            select * from user where 1 = 1
            #{ null != #name && #name != '' }
                and name like :name
            #end
            limit :pageStart, :pageSize
        }*/;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "%张%");
        params.put("name", " ");

        User u = new User();
        u.setName("a");
        long start = System.currentTimeMillis();
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariables(params);
//        System.out.println(parse(sql, params));
//        System.out.println(sql);
//        ctx.setVariable("name", "Hello");


        System.out.println(ep.parseExpression("#name != null").getValue(ctx));
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
            if ('#' == c && '{' == sqlTemplate.charAt(i + 1)) {
                i = i + 2;
                StringBuilder el = new StringBuilder();
                for (; i < sqlTemplate.length(); i++) {
                    c = sqlTemplate.charAt(i);
                    if ('}' == c) {
                        break;
                    }
                    el.append(c);
                }
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                ctx.setVariables(params);
                append = ep.parseExpression(el.toString()).getValue(ctx, Boolean.class);
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
