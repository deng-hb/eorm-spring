package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;

/**
 * 书籍信息
 *
 * @author denghb
 */
@lombok.Data()
@ETable(name = "book")
public class Book implements java.io.Serializable {

    /** 编号 */
    @EColumn(name = "id", primaryKey = true)
    private Integer id;

    /** 书名 */
    @EColumn(name = "title")
    private String title;

    /** 封面 */
    @EColumn(name = "cover_url")
    private String coverUrl;

    /** 作者 */
    @EColumn(name = "author")
    private String author;

    /** 简介 */
    @EColumn(name = "intro")
    private String intro;

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