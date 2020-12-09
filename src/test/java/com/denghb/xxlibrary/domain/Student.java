package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;

/**
 * 学生信息表
 *
 * @author denghb
 */
@lombok.Data()
@Etable(name = "student")
public class Student implements java.io.Serializable {

    /** ID */
    @Ecolumn(name = "id", primaryKey = true)
    private Integer id;

    /** 姓名 */
    @Ecolumn(name = "name")
    private String name;

    /** 0:女，1:男 */
    @Ecolumn(name = "gender")
    private Integer gender;

    /** 0:女，1:男 */
    @Ecolumn(name = "age")
    private Integer age;

    /** 生日 */
    @Ecolumn(name = "birthday")
    private java.util.Date birthday;

    /** 插入时间 */
    @Ecolumn(name = "created_time")
    private java.util.Date createdTime;

    /** 更新时间 */
    @Ecolumn(name = "updated_time")
    private java.util.Date updatedTime;

    /** 逻辑删除 */
    @Ecolumn(name = "deleted")
    private Boolean deleted;

    /** 版本号 */
    @Ecolumn(name = "version")
    private Integer version;


}