package com.denghb.eorm.support;


import com.denghb.eorm.EOrmException;
import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;
import com.denghb.eorm.support.domain.EColumnRef;
import com.denghb.eorm.support.domain.ETableRef;
import com.denghb.eorm.utils.EReflectUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public class ETableColumnParser {

    // <class,Table>
    private static final Map<Class<?>, ETableRef> TABLE_REF_CACHE = new ConcurrentHashMap<Class<?>, ETableRef>(200);

    public static ETableRef getTableRef(Class<?> clazz) {
        ETableRef tableRef = TABLE_REF_CACHE.get(clazz);
        if (null != tableRef) {
            return tableRef;
        }
        ETable etable = clazz.getAnnotation(ETable.class);
        if (null == etable) {
            throw new EOrmException("not find @ETable");
        }
        String tableName = etable.name();
        tableRef = new ETableRef();
        tableRef.setName(tableName);
        //
        List<Field> fields = EReflectUtils.getFields(clazz);

        boolean append = false;
        StringBuilder columns = new StringBuilder();
        for (Field field : fields) {

            EColumn e = field.getAnnotation(EColumn.class);
            if (null == e) {
                continue;
            }
            EColumnRef column = new EColumnRef();
            column.setField(field);
            column.setName(e.name());

            if (append) {
                columns.append(", ");
            }
            columns.append("`");
            columns.append(column.getName());
            columns.append("`");
            append = true;

            boolean primaryKey = e.primaryKey();
            if (primaryKey) {
                tableRef.getPrimaryKeyColumns().add(column);
            } else {
                tableRef.getCommonColumns().add(column);
            }

        }
        tableRef.setColumns(columns.toString());
        if (columns.length() == 0) {
            throw new EOrmException("not find @EColumn");
        }
        if (tableRef.getPrimaryKeyColumns().isEmpty()) {
            throw new EOrmException("not find @EColumn primaryKey = true");
        }

        // 主键
        StringBuilder wherePrimaryKeyColumns = new StringBuilder("where ");
        boolean append2 = false;
        List<EColumnRef> primaryKeys = tableRef.getPrimaryKeyColumns();
        for (EColumnRef column : primaryKeys) {
            if (append2) {
                wherePrimaryKeyColumns.append(" and ");
            }
            wherePrimaryKeyColumns.append("`");
            wherePrimaryKeyColumns.append(column.getName());
            wherePrimaryKeyColumns.append("` = ?");
            append2 = true;
        }
        tableRef.setWherePrimaryKeyColumns(wherePrimaryKeyColumns.toString());

        // 查询语句
        tableRef.setSelectTable(String.format(Locale.CHINA, "select %s from %s ", columns, tableName));

        // 更新语句
        tableRef.setUpdateTable(String.format(Locale.CHINA, "update %s set ", tableName));

        // 删除
        tableRef.setDeleteTable(String.format(Locale.CHINA, "delete from %s ", tableName));

        TABLE_REF_CACHE.put(clazz, tableRef);
        return tableRef;
    }

}
