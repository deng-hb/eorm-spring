package com.denghb;

/**
 * @Auther: denghb
 * @Date: 2019-05-22 00:27
 */
public class MultiLineTest {

    private static String A = ""/*{
        Hello World!
    }*/;

    public static void main(String[] args) {
        String a = ""/*{
            I'm multi-line
        }*/;
        System.out.println(A);
        System.out.println(a);
    }
}
