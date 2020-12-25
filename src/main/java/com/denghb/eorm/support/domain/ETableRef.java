package com.denghb.eorm.support.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息
 */
@Data
public class ETableRef {

    /**
     * 表名
     * table_name
     */
    private String name;

    /**
     * 所有列名串
     * `id`, `name`, `age`
     */
    private String columns;

    /**
     * 查询
     * select `id`, `name`, `age` from table_name
     */
    private String selectTable;

    /**
     * 删除
     * delete from table_name
     */
    private String deleteTable;

    /**
     * 更新
     * update table_name set
     */
    private String updateTable;

    /**
     * 主键条件
     * where `id` = ? and `union_id` = ?
     */
    private String wherePrimaryKeyColumns;

    /**
     * 主键列
     */
    private List<EColumnRef> primaryKeyColumns = new ArrayList<EColumnRef>();

    /**
     * 普通列
     */
    private List<EColumnRef> commonColumns = new ArrayList<EColumnRef>();

}