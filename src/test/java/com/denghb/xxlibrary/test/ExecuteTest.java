/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.xxlibrary.test;

import com.denghb.xxlibrary.domain.Student;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/06/06 20:51
 */
public class ExecuteTest extends BaseTest {

    @Test
    public void insert1() {
        String sql = ""/*{
            insert into book (
               #if (null != #title)
                title
               #end
               #if (null != #coverUrl)
               , cover_url
               #end
               #if (null != #author)
               , author
               #end
               #if (null != #intro)
               , intro
               #end
            ) values (
               #if (null != #title)
                :title
               #end
               #if (null != #coverUrl)
                , :coverUrl
               #end
               #if (null != #author)
                , :author
               #end
               #if (null != #intro)
                , :intro
               #end
            )
        }*/;


        db.execute(sql, new HashMap<String, Object>() {{
            put("title", "标题");
            put("coverUrl", "1==1");
        }});
    }
}
