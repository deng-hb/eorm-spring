package com.denghb;

import com.denghb.eorm.template.ESQLTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author denghb
 */
public class TemplateTest {

    public static void main(String[] args) {
        String sql = ""/*{
            select *,date_format(birthday, '%Y-%M-%i') from tb_user where deleted = 0
            # 是是是是
            #if (null != #name and '' != #name)
                and name = :name
            #end
        }*/;

        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        String res = ESQLTemplate.format(sql, params);
        System.out.println(res);

        String res2 = ESQLTemplate.parse(res, params);
        System.out.println(res2);
    }

}
