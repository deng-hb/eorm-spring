package com.denghb.eorm.support.model;

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
     * 所有列
     */
    private List<Column> allColumns = new ArrayList<Column>();

    /**
     * 主键列
     */
    private List<Column> pkColumns = new ArrayList<Column>();
    /**
     * 其他列
     */
    private List<Column> otherColumns = new ArrayList<Column>();

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

    public List<Column> getAllColumns() {
        return allColumns;
    }

    public void setAllColumns(List<Column> allColumns) {
        this.allColumns = allColumns;
    }

    public List<Column> getOtherColumns() {
        return otherColumns;
    }

    public void setOtherColumns(List<Column> otherColumns) {
        this.otherColumns = otherColumns;
    }
}