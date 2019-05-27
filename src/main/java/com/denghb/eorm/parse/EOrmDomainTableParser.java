package com.denghb.eorm.parse;


import com.denghb.eorm.EOrmException;
import com.denghb.eorm.annotation.EColumn;
import com.denghb.eorm.annotation.ETable;
import com.denghb.eorm.parse.domain.Column;
import com.denghb.eorm.parse.domain.Table;
import com.denghb.eorm.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public class EOrmDomainTableParser {

    private static final Map<String, Table> DOMAIN_TABLE_CACHE = new ConcurrentHashMap<String, Table>();

    public static Table init(Class clazz) {
        String key = clazz.getName();
        Table table = DOMAIN_TABLE_CACHE.get(key);
        if (null != table) {
            return table;
        }
        table = new Table();
        table.setName(getTableName(clazz));

        //
        Set<Field> fields = ReflectUtils.getFields(clazz);
        for (Field field : fields) {

            EColumn ecolumn = field.getAnnotation(EColumn.class);
            if (null == ecolumn) {
                continue;
            }
            boolean primaryKey = ecolumn.primaryKey();
            if (primaryKey) {
                table.getPkColumns().add(new Column(ecolumn.name(), primaryKey, field));
            }

            table.getColumns().add(new Column(ecolumn.name(), primaryKey, field));
        }

        if (table.getColumns().isEmpty()) {
            throw new EOrmException("not found @EColumn");
        }
        if (table.getPkColumns().isEmpty()) {
            throw new EOrmException("not found @EColumn primaryKey = true");
        }
        DOMAIN_TABLE_CACHE.put(key, table);
        return table;
    }

    private static <T> String getTableName(Class<T> clazz) {

        // 获取表名
        ETable table = clazz.getAnnotation(ETable.class);
        if (null == table) {
            throw new EOrmException("not found @ETable");
        }
        StringBuilder sb = new StringBuilder("`");
        // 获取数据库名称
        String database = table.database();

        if (database.trim().length() != 0) {
            sb.append(database);
            sb.append("`.`");
        }
        // 获取注解的表名
        sb.append(table.name());
        sb.append("`");

        return sb.toString();
    }

}
