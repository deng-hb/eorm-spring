package com.denghb.eorm.page;


import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: denghb
 * @Date: 2019-05-25 20:17
 */
public class EPageReq implements java.io.Serializable {

    private int page = 1;

    private int pageSize = 20;

    private List<String> desc = new ArrayList<String>();

    private List<String> asc = new ArrayList<String>();

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageStart() {
        return (page - 1) * pageSize;
    }

    public List<String> getDesc() {
        return desc;
    }

    public void setDesc(List<String> desc) {
        this.desc = desc;
    }

    public List<String> getAsc() {
        return asc;
    }

    public void setAsc(List<String> asc) {
        this.asc = asc;
    }
}
