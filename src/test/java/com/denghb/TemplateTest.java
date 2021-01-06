package com.denghb;

import com.denghb.eorm.utils.ESQLTemplateUtils;

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
        String res = ESQLTemplateUtils.format(sql, params);
        System.out.println(res);

        ESQLTemplateUtils.parse(res, params);
    }

}
