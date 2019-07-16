package com.denghb.eorm;

import com.denghb.eorm.support.EormSupport;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author denghb
 * @since 2019-07-14 18:44
 */
public class TemplateTest {

    private Logger log = Logger.getLogger(TemplateTest.class);

    @Test
    public void test1() {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("v", "b");

        String sql1 = ""/*{
            #if (#v == 'a')
                aa
            #elseIf (#v == 'b')\
                bb
            #elseIf (#v == 'c')
                cc
            #else
                dd
            #end\
        }*/;

        sql1 = EormSupport.parse(sql1, params);
        log.info(sql1);
    }

    @Test
    public void test2() {

        // 只是测试 #if #elseif #else #end 生效
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("v", "b");
        String sql = ""/*{
            #if (#v == 'a'    )
                a

            #elseIf (#v == 'b')
                b
                #if (#v == '2' )
                    bb
                #else
                #end
            #else
                c
                #if ( #v == '3' )
                    cc
                    #if ( #v == '2' )
                        bb
                    #elseIf ( #v == 'b' )
                        b
                        #if (#v == '2'    )
                            bb
                        #end
                    #else\
                    #end
                #end
            #end

            asds
        }*/;

        sql = EormSupport.parse(sql, params);
        log.info(sql);
    }
}
