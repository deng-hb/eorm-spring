package com.denghb.eorm.page;


import java.util.List;

/**
 * @Auther: denghb
 * @Date: 2019-05-25 20:17
 */
public class EPageRes<T> implements java.io.Serializable {

    private List<T> list;

    private long total;

    public EPageRes() {
    }

    public EPageRes(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "EPageRes{" +
                "list=" + list +
                ", total=" + total +
                '}';
    }
}
