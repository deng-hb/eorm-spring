package com.denghb.xxlibrary.domain;

import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;


/**
 * 书籍信息
 * DDL
 *
 * <pre>
 * CREATE TABLE `book` (
 * `id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '编号',
 * `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '书名',
 * `cover_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面',
 * `author` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者',
 * `intro` text COLLATE utf8mb4_unicode_ci COMMENT '简介',
 * `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
 * `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 * `deleted` tinyint(2) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
 * `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
 * PRIMARY KEY (`id`),
 * KEY `idx_created_time` (`created_time`),
 * KEY `idx_updated_time` (`updated_time`),
 * KEY `idx_deleted` (`deleted`)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍信息'
 * <pre>
 * @author denghb
 * @generateTime Tue May 28 00:50:45 CST 2019
 */
@ETable(name = "book", database = "xxlibrary")
public class Book implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @EColumn(name = "id", primaryKey = true, autoIncrement = true)
    private Integer id;

    /**
     * 书名
     */
    @EColumn(name = "title")
    private String title;

    /**
     * 封面
     */
    @EColumn(name = "cover_url")
    private String coverUrl;

    /**
     * 作者
     */
    @EColumn(name = "author")
    private String author;

    /**
     * 简介
     */
    @EColumn(name = "intro")
    private String intro;

    /**
     * 插入时间
     */
    @EColumn(name = "created_time")
    private java.util.Date createdTime;

    /**
     * 更新时间
     */
    @EColumn(name = "updated_time")
    private java.util.Date updatedTime;

    /**
     * 逻辑删除
     */
    @EColumn(name = "deleted")
    private Integer deleted;

    /**
     * 版本号
     */
    @EColumn(name = "version")
    private Integer version;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public java.util.Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(java.util.Date createdTime) {
        this.createdTime = createdTime;
    }

    public java.util.Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(java.util.Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("{");
        str.append("\"id\":\"");
        str.append(id);
        str.append("\"");
        str.append(",");
        str.append("\"title\":\"");
        str.append(title);
        str.append("\"");
        str.append(",");
        str.append("\"coverUrl\":\"");
        str.append(coverUrl);
        str.append("\"");
        str.append(",");
        str.append("\"author\":\"");
        str.append(author);
        str.append("\"");
        str.append(",");
        str.append("\"intro\":\"");
        str.append(intro);
        str.append("\"");
        str.append(",");
        str.append("\"createdTime\":\"");
        str.append(createdTime);
        str.append("\"");
        str.append(",");
        str.append("\"updatedTime\":\"");
        str.append(updatedTime);
        str.append("\"");
        str.append(",");
        str.append("\"deleted\":\"");
        str.append(deleted);
        str.append("\"");
        str.append(",");
        str.append("\"version\":\"");
        str.append(version);
        str.append("\"");

        return str.append("}").toString();
    }
}