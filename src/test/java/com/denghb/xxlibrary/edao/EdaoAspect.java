package com.denghb.xxlibrary.edao;

import org.aspectj.lang.ProceedingJoinPoint;

public class EdaoAspect {

    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        System.out.println("@Around：切点方法环绕start.....");
        Object[] args = pjp.getArgs();
        Object o = pjp.proceed(args);
        System.out.println("@Around：切点方法环绕end.....");
        return o;
    }
}
