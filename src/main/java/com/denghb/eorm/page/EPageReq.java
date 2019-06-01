package com.denghb.eorm.page;


import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Simple
 *
 * @Auther: denghb
 * @Date: 2019-05-25 20:17
 */
public class EPageReq implements java.io.Serializable {

    @ApiModelProperty(value = "页码", example = "1")
    private int page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private int pageSize = 10;

    @ApiModelProperty(value = "排序降序字段", hidden = true)
    private List<String> desc;

    @ApiModelProperty(value = "排序升序字段", hidden = true)
    private List<String> asc;

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
