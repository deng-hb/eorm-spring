/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.support;

import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.SelectSQL;
import com.denghb.eorm.support.domain.SelectTable;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.template.ESQLTemplate;
import com.denghb.eorm.utils.EReflectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/09/10 00:21
 */
public abstract class ESelectParser {

    public static EAsteriskColumn parse(String sql, Class<?> clazz) {
        EAsteriskColumn ac = new EAsteriskColumn();
        SelectSQL ss = parse(sql);
        String tsql = ss.getSql();

        // 是那种xxx.*的
        List<String> aliasList = ss.getFields().stream().filter(f -> f.endsWith(".*")).map(f -> f.replace(".*", "")).collect(Collectors.toList());


        if (!aliasList.isEmpty()) {

            Set<Field> fields = EReflectUtils.getFields(clazz);

            Map<String, Field> fieldNames = new HashMap<>();
            for (Field field : fields) {
                fieldNames.put(field.getName(), field);
                fieldNames.put(EReflectUtils.humpToUnderline(field.getName()), field);
            }

            for (String alias : aliasList) {
                Field field = fieldNames.get(alias);
                if (null == field) {
                    continue;
                }
                ac.getFields().put(alias, field);
                String tableName = ss.getTable().get(alias);
                List<String> columns = new ArrayList<>();
                if (tableName.contains(" ")) {
                    SelectSQL ss1 = parse(tableName);
                    List<String> ff = ss1.getFields();
                    if (ff.size() == 1 && "*".equals(ff.get(0))) {
                        String tn = ss1.getTable().keySet().iterator().next();
                        columns = ETableColumnParser.getTable(tn).getAllColumns().stream().map(Column::getName).collect(Collectors.toList());
                    } else {
                        for (String f : ff) {
                            // x.a , count(a) as c
                            StringBuilder sb = new StringBuilder();
                            for (int i = f.length() - 1; i > 0; i--) {
                                char c = f.charAt(i);
                                if (' ' == c || '.' == c || ')' == c) {
                                    break;
                                }
                                sb.append(c);
                            }
                            columns.add(sb.reverse().toString());
                        }
                    }
                } else {
                    columns = ETableColumnParser.getTable(tableName).getAllColumns().stream().map(Column::getName).collect(Collectors.toList());
                }


                StringBuilder sss = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    String column = columns.get(i);
                    sss.append(alias);
                    sss.append(".");
                    sss.append(column);
                    sss.append(" ");
                    sss.append(alias);
                    sss.append("__");
                    sss.append(column);
                    if (i < columns.size() - 1) {
                        sss.append(",");
                    }
                }
                tsql = tsql.replace(alias + ".*", sss);
            }
        }
        ac.setTsql(tsql);
        return ac;
    }

    public static SelectSQL parse(String sql) {
        SelectSQL ss = new SelectSQL();
        sql = ESQLTemplate.format(sql);
        ss.setSql(sql);

        StringBuilder field = new StringBuilder();
        int ct = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (ESQLTemplate.hasNextKeyword(sql, "select ", i)) {
                i += 6;
            } else if (ESQLTemplate.hasNextKeyword(sql, " from ", i)) {
                ss.getFields().add(field.toString().trim());
                i += 5;

                boolean hasTable = true;
                for (; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if (' ' == c) {
                        if (ESQLTemplate.hasNextKeyword(sql, " join ", i)) {
                            i += 5;
                            SelectTable st = getTable(sql, i);
                            ss.getTable().put(st.getAlias(), st.getTable());
                            i = st.getIndex();
                        }
                    } else if (',' == c) {
                        i++;
                        SelectTable st = getTable(sql, i);
                        ss.getTable().put(st.getAlias(), st.getTable());
                        i = st.getIndex();
                    } else {
                        if (hasTable) {
                            SelectTable st = getTable(sql, i);
                            ss.getTable().put(st.getAlias(), st.getTable());
                            i = st.getIndex();
                            hasTable = false;
                        }
                    }

                }
                break;
            } else {
                if ('(' == c) {
                    ct++;
                } else if (')' == c) {
                    ct--;
                } else if (0 == ct && ',' == c) {
                    ss.getFields().add(field.toString().trim());
                    field = new StringBuilder();
                    continue;
                }
                field.append(c);
            }
        }
        return ss;
    }

    // fail pivot
    private static SelectTable getTable(String sql, int i) {
        SelectTable st = new SelectTable();
        boolean isSpace = false;
        StringBuilder table = new StringBuilder();
        StringBuilder alias = new StringBuilder();
        f1:
        for (; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if ('(' == c) {// 子查询
                int ct = 1;
                i++;
                for (; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if ('(' == c) {
                        ct++;
                    } else if (')' == c) {
                        ct--;
                    }
                    if (ct == 0) {
                        i++;
                        // 找别名 除了"as"
                        isSpace = false;
                        for (; i < sql.length(); i++) {
                            c = sql.charAt(i);
                            if (' ' == c) {
                                if (ESQLTemplate.hasNextKeyword(sql, " as ", i)) {
                                    i += 3;
                                }
                                if (isSpace) {
                                    break f1;
                                }
                                isSpace = true;
                            } else if (',' == c) {
                                i--;
                                break f1;
                            } else {
                                alias.append(c);
                            }
                        }

                        break f1;
                    }
                    table.append(c);
                }
            } else if (' ' == c) {
                if (table.length() > 0) {
                    if (isSpace) {
                        break;
                    }
                    isSpace = true;
                }
            } else if (',' == c) {
                i--;
                break;
            } else {
                if (isSpace) {
                    alias.append(c);
                } else {
                    table.append(c);
                }
            }
        }
        String a = alias.toString().trim();
        String t = table.toString().trim();
        if (a.equals("") || hasIgnoreKeywords(a)) {
            a = t;
        }
        st.setAlias(a);
        st.setTable(t);
        st.setIndex(i);
        return st;
    }

    private final static List<String> s = Arrays.asList("left", "right", "inner", "on", "order", "group", "having", "where");

    private static boolean hasIgnoreKeywords(String alias) {
        alias = alias.toLowerCase();
        return s.contains(alias);
    }
}
