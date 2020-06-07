package com.denghb.eorm.support;



import com.denghb.eorm.support.domain.Trace;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 方法跟踪
 * start() 后必须 end()
 *
 * @author denghb
 * @since 2019-07-14 17:53
 */
public class ETraceSupport {

    private static final String PREFIX = "E";

    private static final String PACKAGE_NAME = "com.denghb.eorm.impl";
    private static final String PACKAGE_NAME2 = "com.denghb.eorm.support";

    private static final AtomicLong counter = new AtomicLong(100000000);

    private static final ThreadLocal<Trace> local = new ThreadLocal<Trace>();

    public static void start() {
        Trace trace = get();
        if (null == trace) {
            trace = new Trace();
            trace.setId(genId());
            trace.setStartTime(System.currentTimeMillis());

            StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
            // 不是 @see PACKAGE_NAME 的才塞进来
            for (StackTraceElement ste : stackTraceElements) {
                String className = ste.getClassName();
                if (!className.startsWith(PACKAGE_NAME) && !className.startsWith(PACKAGE_NAME2)) {
                    trace.setLogName(className);
                    String logMethod = String.format("%s(%s:%d)", ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
                    trace.setLogMethod(logMethod);
                    break;
                }
            }
            local.set(trace);
        }

    }

    private static String genId() {
        return PREFIX + counter.getAndIncrement();
    }

    public static Trace get() {
        return local.get();
    }

    public static void end() {
        local.remove();
    }
}