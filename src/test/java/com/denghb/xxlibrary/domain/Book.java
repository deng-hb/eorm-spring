package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;

/**
 * 书籍信息
 *
 * @author denghb
 */
@lombok.Data()
@Etable(name = "book")
public class Book implements java.io.Serializable {

    /** 编号 */
    @Ecolumn(name = "id", primaryKey = true)
    private Integer id;

    /** 书名 */
    @Ecolumn(name = "title")
    private String title;

    /** 封面 */
    @Ecolumn(name = "cover_url")
    private String coverUrl;

    /** 作者 */
    @Ecolumn(name = "author")
    private String author;

    /** 简介 */
    @Ecolumn(name = "intro")
    private String intro;

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