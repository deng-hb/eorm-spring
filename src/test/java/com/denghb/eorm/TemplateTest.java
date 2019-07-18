package com.denghb.eorm;

import com.denghb.eorm.support.EormSupport;
import org.apache.log4j.Logger;
import org.junit.Assert;
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
    public void t1() {

        doOut("1");
        doOut("a");
        doOut("b");
        doOut("c");
        doOut("d");
    }

    private void doOut(String p) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p", p);

        String input = ""/*{#if(#p=='a')a#elseIf(#p=='b')#if(#p=='c')#elseIf(1==2)#elseb#end#elseIf(#p=='c')c#else${p}#end}*/;

        String output = EormSupport.parse(input, params);
        System.out.println(output);
        Assert.assertTrue(p.equals(output));
    }

    @Test
    public void test1() {
        doTest1("a");
        doTest1("b");
        doTest1("c");
        doTest1("d");
        doTest1("e");
    }

    private void doTest1(String v) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("v", v);

        String sql1 = ""/*{
            #if (#v == 'a')
                aa
            #elseIf(#v == 'b'
            )\1
                bb
            #elseIf (#v == 'c')
                cc
            #else
                dd
            #end\2
        }*/;

        sql1 = EormSupport.parse(sql1, params);
        log.info(sql1);
    }

    @Test
    public void test2() {
        doTest2("a", "2");
        doTest2("b", "1");
        doTest2("b", "2");
        doTest2("c", "3");
        doTest2("d", "1");
        doTest2("f", "2");
    }

    private void doTest2(String v, String p) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("v", v);
        params.put("p", p);
        String sql = ""/*{
            #if (#v == 'a'    )
                a

            #elseIf (#v == 'b'    )1
                b
                #if (#p == '2' )
                    bb
                #else
                #end
            #elseIf (#v == 'f'    )1
                ff
                #if (#p == '2' )
                    ffffff
                #else
                #end
            #else
                c
                #if ( #p == '3' )
                    cc
                    #if ( #p == '2' )
                        cbb
                    #elseIf ( #p == 'b' )
                        cb
                        #if (#p == '2'    )
                            ccbb
                        #end
                    #else\
                    #end
                #end
            #end

            asds
        }*/;
//        System.out.println(sql);

        sql = EormSupport.parse(sql, params);
        log.info(sql);
    }
}
