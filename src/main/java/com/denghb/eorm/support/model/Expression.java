package com.denghb.eorm.support.model;


public class Expression {

    private int endIndex;

    private String content;

    public Expression(int endIndex, String content) {
        this.endIndex = endIndex;
        this.content = content;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Expression{" +
                "endIndex=" + endIndex +
                ", content='" + content + '\'' +
                '}';
    }
}