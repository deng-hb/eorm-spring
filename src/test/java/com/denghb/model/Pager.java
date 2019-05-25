package com.denghb.model;

import com.denghb.eorm.page.EPageReq;

/**
 * @Auther: denghb
 * @Date: 2019-05-21 23:57
 */
public class Pager extends EPageReq {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
