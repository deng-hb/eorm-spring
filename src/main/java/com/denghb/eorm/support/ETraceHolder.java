package com.denghb.eorm.support;


import com.denghb.eorm.support.domain.ETrace;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 方法跟踪
 * start() 后必须 end()
 *
 * @author denghb
 * @since 2019-07-14 17:53
 */
public class ETraceHolder {

    private static final String PREFIX = "E";

    private static final String PACKAGE_NAME = "com.denghb.eorm.impl";
    private static final String PACKAGE_NAME2 = "com.denghb.eorm.support";

    private static final AtomicLong counter = new AtomicLong(100000000);

    private static final ThreadLocal<ETrace> local = new InheritableThreadLocal<>();

    public static void start() {
        ETrace trace = get();
        if (null == trace) {
            trace = new ETrace();
            setTrace(trace);
        }

    }

    public static void setTrace(ETrace trace) {
        if (null == trace) {
            trace = new ETrace();
        }
        if (null == trace.getId()) {
            trace.setId(genId());
        }
        if (null == trace.getStartTime()) {
            trace.setStartTime(System.currentTimeMillis());
        }

        if (null == trace.getLogName() || null == trace.getLogMethod()) {
            String className = null;
            String logMethod = null;

            StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
            // 不是 @see PACKAGE_NAME 的才塞进来
            for (StackTraceElement ste : stackTraceElements) {
                className = ste.getClassName();
                if (!className.startsWith(PACKAGE_NAME) && !className.startsWith(PACKAGE_NAME2)) {
                    logMethod = String.format("%s(%s:%d)", ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
                    break;
                }
            }
            trace.setLogName(className);
            trace.setLogMethod(logMethod);
        }
        local.set(trace);
    }

    private static String genId() {
        return PREFIX + counter.getAndIncrement();
    }

    public static ETrace get() {
        return local.get();
    }

    public static void end() {
        local.remove();
    }
}