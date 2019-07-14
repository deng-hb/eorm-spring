package com.denghb.eorm.support.model;

/**
 * @author denghb
 * @since 2019-07-13 23:07
 */
public class Trace {

    private String id;

    private long startTime;

    private StackTraceElement stackTraceElement;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public StackTraceElement getStackTraceElement() {
        return stackTraceElement;
    }

    public void setStackTraceElement(StackTraceElement stackTraceElement) {
        this.stackTraceElement = stackTraceElement;
    }
}
