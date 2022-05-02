package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;

/**
 * 阅读记录
 *
 * @author denghb
 */
@lombok.Data()
@ETable(name = "read_record")
public class ReadRecord implements java.io.Serializable {

    /** ID */
    @EColumn(name = "id", primaryKey = true)
    private Integer id;

    /** 学生ID */
    @EColumn(name = "student_id")
    private Integer studentId;

    /** 书籍ID */
    @EColumn(name = "book_id")
    private Integer bookId;

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