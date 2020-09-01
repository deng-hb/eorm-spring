package com.denghb.eorm.support.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息
 */
@Data
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

}