package com.denghb.eorm.page;


import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple
 *
 * @author denghb 2019-05-25 20:17
 */
public class EPageReq implements java.io.Serializable {

    @ApiModelProperty(value = "页码", example = "1")
    private int page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private int pageSize = 10;

    @ApiModelProperty(value = "分页开始", hidden = true)
    private int pageStart;

    @ApiModelProperty(value = "排序降序字段", hidden = true)
    private Set<String> desc = new HashSet<String>();

    @ApiModelProperty(value = "排序升序字段", hidden = true)
    private Set<String> asc = new HashSet<String>();

    @ApiModelProperty(value = "可排序字段", hidden = true)
    private Set<String> sorts = new HashSet<String>();

    public EPageReq() {
    }

    /**
     * 计算
     *
     * @return
     */
    public int getPageStart() {
        return (page - 1) * pageSize;
    }

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

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public Set<String> getDesc() {
        return desc;
    }

    public void setDesc(Set<String> desc) {
        this.desc = desc;
    }

    public Set<String> getAsc() {
        return asc;
    }

    public void setAsc(Set<String> asc) {
        this.asc = asc;
    }

    public Set<String> getSorts() {
        return sorts;
    }

    public void setSorts(Set<String> sorts) {
        this.sorts = sorts;
    }

    @Override
    public String toString() {
        return "EPageReq{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", pageStart=" + pageStart +
                ", desc=" + desc +
                ", asc=" + asc +
                ", sorts=" + sorts +
                '}';
    }
}
