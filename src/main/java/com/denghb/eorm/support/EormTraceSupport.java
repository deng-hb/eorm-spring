package com.denghb.eorm.support;

import com.denghb.eorm.support.model.Trace;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 方法跟踪
 * start() 后必须 end()
 *
 * @author denghb
 * @since 2019-07-14 17:53
 */
public class EormTraceSupport {

    private static final String PREFIX = "E";

    private static final String PACKAGE_NAME = "com.denghb.eorm";

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
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                if (!stackTraceElement.getClassName().startsWith(PACKAGE_NAME)) {
                    trace.setStackTraceElement(stackTraceElement);
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