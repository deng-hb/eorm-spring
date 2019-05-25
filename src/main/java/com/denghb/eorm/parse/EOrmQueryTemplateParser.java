package com.denghb.eorm.parse;

import com.denghb.eorm.utils.ReflectUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * @Auther: denghb
 * @Date: 2019-05-22 23:04
 */
public class EOrmQueryTemplateParser {

    // 性能？
    private static SpelExpressionParser _expressionParser = new SpelExpressionParser();

    public static String parse(String sqlTemplate, Map<String, Object> params) {

        if (!sqlTemplate.contains("#")) {
            return sqlTemplate;
        }
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariables(params);

        StringBuilder sql = new StringBuilder();
        boolean append = true;
        for (int i = 0; i < sqlTemplate.length(); i++) {
            char c = sqlTemplate.charAt(i);

            // #{ SpEL }
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
                append = _expressionParser.parseExpression(el.toString()).getValue(ctx, Boolean.class);
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

    public static String parse(String sqlTemplate, Object object) {
        if (ReflectUtils.isSingleClass(object.getClass())) {
            return sqlTemplate;
        }
        return parse(sqlTemplate, ReflectUtils.objectToMap(object));
    }
}
