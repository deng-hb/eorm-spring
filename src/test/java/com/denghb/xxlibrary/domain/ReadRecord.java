package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;

/**
 * 阅读记录
 *
 * @author denghb
 */
@lombok.Data()
@Etable(name = "read_record")
public class ReadRecord implements java.io.Serializable {

    /** ID */
    @Ecolumn(name = "id", primaryKey = true)
    private Integer id;

    /** 学生ID */
    @Ecolumn(name = "student_id")
    private Integer studentId;

    /** 书籍ID */
    @Ecolumn(name = "book_id")
    private Integer bookId;

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