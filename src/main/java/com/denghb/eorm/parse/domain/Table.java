package com.denghb.eorm.parse.domain;

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
     * 主键字段
     */
    private List<Column> pkColumns = new ArrayList<Column>();

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

    public List<Column> getPkColumns() {
        return pkColumns;
    }

    public void setPkColumns(List<Column> pkColumns) {
        this.pkColumns = pkColumns;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}