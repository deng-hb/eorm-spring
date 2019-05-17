package com.denghb.eorm;

@TestEorm
public class Test {


    public void output() {
        String s = ""/**{
         hello world,
         eorm
         Select * from tb_user
         }*/;

        String s2 = ""/**{
         }*/;

        System.out.println(s);
    }
}
