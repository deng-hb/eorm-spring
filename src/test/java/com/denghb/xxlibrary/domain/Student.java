package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;

/**
 * 学生信息表
 *
 * @author denghb
 */
@lombok.Data()
@ETable(name = "student")
public class Student implements java.io.Serializable {

    /** ID */
    @EColumn(name = "id", primaryKey = true)
    private Integer id;

    /** 姓名 */
    @EColumn(name = "name")
    private String name;

    /** 0:女，1:男 */
    @EColumn(name = "gender")
    private Integer gender;

    /** 0:女，1:男 */
    @EColumn(name = "age")
    private Integer age;

    /** 生日 */
    @EColumn(name = "birthday")
    private java.util.Date birthday;

    /** 插入时间 */
    @EColumn(name = "created_time")
    private java.util.Date createdTime;

    /** 更新时间 */
    @EColumn(name = "updated_time")
    private java.util.Date updatedTime;

    /** 逻辑删除 */
    @EColumn(name = "deleted")
    private Integer deleted;

    /** 版本号 */
    @EColumn(name = "version")
    private Integer version;


}