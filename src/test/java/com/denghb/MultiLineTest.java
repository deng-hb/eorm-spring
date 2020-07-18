package com.denghb;

/**
 * @author denghb
 * @Date: 2019-05-22 00:27
 */
public class MultiLineTest<M> {

    private static String A = ""/*{
        Hello World!
    }*/;

    public static void main(String[] args) {
        String a = ""/*{
            I'm multi-line
        }*/;

        StringBuilder s = new StringBuilder(a);
        s.append("a");
        a = s.toString();
        System.out.println(A);
        System.out.println(a);

        new MultiLineTest<String>().aa();

    }

    private void aa() {
        return;
    }
}
