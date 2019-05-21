package com.denghb.model;

/**
 * @Auther: denghb
 * @Date: 2019-05-21 23:57
 */
public class Pager {

    private int page = 1;

    private int pageSize = 20;

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
}
