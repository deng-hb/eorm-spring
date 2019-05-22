package com.denghb.eorm.utils;

import com.denghb.eorm.EormException;

import java.util.List;
import java.util.Map;

/**
 * @Auther: denghb
 * @Date: 2019-05-22 23:04
 */
public class SqlTemplateUtils {

    /**
     * stupid? use freemarker?
     *
     * <pre>
     *
     *     #ifNotNull(field)
     *      sql
     *     #end
     *
     *     #ifNotBlank(field)
     *      sql
     *     #end
     *
     *     #if(field)
     *      sql
     *     #end
     *
     * </pre>
     *
     * @param sqlTemplate
     * @param params
     * @return
     */
    public static String parse(String sqlTemplate, Map<String, Object> params) {

        sqlTemplate = sqlTemplate.replaceAll("\n", " ");
        sqlTemplate = sqlTemplate.replaceAll("   ", " ");
        sqlTemplate = sqlTemplate.replaceAll("  ", " ");
        sqlTemplate = sqlTemplate.replaceAll("   ", " ");
        sqlTemplate = sqlTemplate.replaceAll("  ", " ");

        StringBuilder sql = new StringBuilder();
        boolean append = true;
        for (int i = 0; i < sqlTemplate.length(); i++) {
            char c = sqlTemplate.charAt(i);

            // #ifNot
            if ('#' == c && 'i' == sqlTemplate.charAt(i + 1) && 'f' == sqlTemplate.charAt(i + 2)
                    && 'N' == sqlTemplate.charAt(i + 3) && 'o' == sqlTemplate.charAt(i + 4) && 't' == sqlTemplate.charAt(i + 5)) {
                i += 6;
                append = false;
                if ('B' == sqlTemplate.charAt(i) && 'l' == sqlTemplate.charAt(i + 1) && 'a' == sqlTemplate.charAt(i + 2)
                        && 'n' == sqlTemplate.charAt(i + 3) && 'k' == sqlTemplate.charAt(i + 4) && '(' == sqlTemplate.charAt(i + 5)) {
                    // Blank
                    i += 6;
                    Cell cell = getCell(i, sqlTemplate);
                    i = cell.getIndex();

                    String name = cell.getName();
                    Object object = params.get(name);
                    if (null != object) {
                        if (!(object instanceof CharSequence)) {
                            throw new EormException("Property [" + name + "] not String");
                        }
                        String str = String.valueOf(object);
                        append = str.trim().length() > 0;
                    }
                } else if ('N' == sqlTemplate.charAt(i) && 'u' == sqlTemplate.charAt(i + 1) && 'l' == sqlTemplate.charAt(i + 2)
                        && 'l' == sqlTemplate.charAt(i + 3) && '(' == sqlTemplate.charAt(i + 4)) {
                    // Null
                    i += 5;
                    Cell cell = getCell(i, sqlTemplate);
                    i = cell.getIndex();

                    String name = cell.getName();
                    Object object = params.get(name);
                    append = null != object;

                } else if ('E' == sqlTemplate.charAt(i) && 'm' == sqlTemplate.charAt(i + 1) && 'p' == sqlTemplate.charAt(i + 2)
                        && 't' == sqlTemplate.charAt(i + 3) && 'y' == sqlTemplate.charAt(i + 4) && '(' == sqlTemplate.charAt(i + 5)) {
                    // Empty
                    i += 6;
                    Cell cell = getCell(i, sqlTemplate);
                    i = cell.getIndex();

                    String name = cell.getName();
                    Object object = params.get(name);
                    if (null != object) {
                        if (object instanceof List) {
                            List list = (List) object;
                            append = !list.isEmpty();
                        } else if (object instanceof CharSequence) {
                            String str = String.valueOf(object);
                            append = str.length() > 0;
                        } else {
                            throw new EormException("#ifNotEmpty Error");
                        }
                    }
                }
            } else if ('#' == c && 'e' == sqlTemplate.charAt(i + 1) && 'n' == sqlTemplate.charAt(i + 2)
                    && 'd' == sqlTemplate.charAt(i + 3)) {
                i = i + 4;
                append = true;
            } else if (append) {
                sql.append(c);
            }
        }

        return sql.toString();
    }

    public static String parse(String sqlTemplate, Object object) {
        return parse(sqlTemplate, ReflectUtils.objectToMap(object));
    }

    private static Cell getCell(int i, String sqlTemp) {
        StringBuilder str = new StringBuilder();
        for (; i < sqlTemp.length(); i++) {
            char c = sqlTemp.charAt(i);
            if (')' == c) {
                break;
            }
            str.append(c);
        }
        i++;
        return new Cell(i, str.toString().trim());
    }


    private static class Cell {
        private int index;

        private String name;

        public Cell(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
