package com.denghb.xxlibrary.model.req;

import com.denghb.eorm.page.EPageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author denghb 2019-05-21 23:57
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StudentPageReq extends EPageReq {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "StudentPageReq{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
