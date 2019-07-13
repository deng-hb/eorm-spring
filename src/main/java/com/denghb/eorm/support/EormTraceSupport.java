package com.denghb.eorm.support;

import com.denghb.eorm.support.model.Trace;

import java.util.concurrent.atomic.AtomicLong;

public class EormTraceSupport {

    private static final AtomicLong counter = new AtomicLong(100000000);

    private static final ThreadLocal<Trace> local = new ThreadLocal<Trace>();

    public static void start() {
        Trace trace = get();
        if (null == trace) {
            trace = new Trace();
            trace.setId(String.valueOf(counter.getAndIncrement()));
            trace.setStartTime(System.currentTimeMillis());

            StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
            trace.setStackTraceElements(stackTraceElements);

            StackTraceElement e = stackTraceElements[2];
            String method = String.format("%s.%s(%s:%d)", e.getClassName(), e.getMethodName(), e.getFileName(), e.getLineNumber());
            trace.setMethod(e.toString());
            local.set(trace);
        }

    }

    /**
     * 获取
     */
    public static Trace get() {
        return local.get();
    }

    public static void end() {
        local.remove();
    }
}