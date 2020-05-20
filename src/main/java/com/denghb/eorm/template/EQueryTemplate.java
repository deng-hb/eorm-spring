package com.denghb.eorm.template;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author denghb
 */
public class EQueryTemplate {

    // 性能？
    private static SpelExpressionParser SpEL = new SpelExpressionParser();

    public static String parse(String sql, Map<String, Object> params) {

        sql = sql.replaceAll("\n", " ");
        sql = sql.replaceAll("\t", " ");
        sql = sql.replaceAll("\r", " ");
        sql = sql.replaceAll("   ", " ");
        sql = sql.replaceAll("  ", " ");
        sql = sql.replaceAll("   ", " ");
        sql = sql.replaceAll("  ", " ");

        if (!sql.contains("#")) {
            return sql;
        }
        StandardEvaluationContext ctx = null;
        if (sql.contains("#{")) {
            ctx = new StandardEvaluationContext();
            ctx.setVariables(params);
        }
        StringBuilder ss = new StringBuilder();
        boolean append = true;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            // #{ SpEL }
            if ('#' == c && '{' == sql.charAt(i + 1)) {
                i = i + 2;
                StringBuilder el = new StringBuilder();
                for (; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if ('}' == c) {
                        break;
                    }
                    el.append(c);
                }
                append = SpEL.parseExpression(el.toString()).getValue(ctx, Boolean.class);
            } else if ('#' == c && 'i' == sql.charAt(i + 1) && 'f' == sql.charAt(i + 2)) {
                // #if ( )
                i += 3;
                StringBuilder name = new StringBuilder();
                for (; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if (')' == c) {
                        break;
                    }
                    if ('(' != c && ' ' != c) {
                        name.append(c);
                    }
                }
                Object object = params.get(name.toString());
                if (object instanceof Boolean || object instanceof Number || object instanceof Date) {
                    append = true;
                } else if (object instanceof CharSequence) {
                    append = String.valueOf(object).trim().length() > 0;
                } else if (object instanceof List) {
                    append = !((List) object).isEmpty();
                } else {
                    append = false;
                }

            } else if ('#' == c && 'e' == sql.charAt(i + 1) && 'n' == sql.charAt(i + 2)
                    && 'd' == sql.charAt(i + 3)) {
                // #end
                i = i + 4;
                append = true;
            } else if (append) {
                ss.append(c);
            }
        }
        return ss.toString();

    }

}
