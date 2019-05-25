package com.denghb.eorm.page;


/**
 * @Auther: denghb
 * @Date: 2019-05-25 20:17
 */
public class EPageReq implements java.io.Serializable {

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
