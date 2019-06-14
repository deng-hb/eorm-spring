package com.denghb.eorm.support.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息
 */
public class Table {

    /**
     * 表名
     */
    private String name;

    /**
     * 主键字（不考虑复合主键）
     */
    private Column primaryKeyColumn;

    /**
     * 所有的列
     */
    private List<Column> columns = new ArrayList<Column>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Column getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(Column primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}