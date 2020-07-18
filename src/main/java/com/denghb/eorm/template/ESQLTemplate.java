package com.denghb.eorm.template;

import com.denghb.eorm.EormException;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.EReflectUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 支持使用Annotation标注表结构
 */
public abstract class ESQLTemplate {
    // 性能？
    private static final ExpressionParser SpEL = new SpelExpressionParser();

    private static final char $ = '$';
    private static final char HASH = '#';

    // 不区分大小写
    private static final String HASH_IF = "#if";
    private static final String HASH_ELSE_IF = "#elseif";
    private static final String HASH_ELSE = "#else";
    private static final String HASH_END = "#end";

    /**
     * 美化SQL
     * 去掉多余的换行和空格及注释
     * # 注释（#{空格}文字直到换行都算注释内容）
     *
     * @param sql 原SQL
     * @return 美化后的SQL
     */
    public static String format(String sql) {
        return format(sql, null);
    }

    public static String format(String sql, Map<String, Object> params) {
        if (null == sql) {
            return null;
        }
        boolean isQuote = false;
        StringBuilder ss = new StringBuilder();
        int sqlLength = sql.length();
        for (int i = 0; i < sqlLength; i++) {

            char c = sql.charAt(i);
            if ($ == c && '{' == sql.charAt(i + 1) && null != params) {
                // a${b}c > abc
                i += 2;
                StringBuilder name = new StringBuilder();
                for (; i < sql.length(); i++) {
                    c = sql.charAt(i);
                    if (' ' == c) {
                        continue;
                    }
                    if ('}' == c) {
                        break;
                    }
                    name.append(c);
                }
                Object object = params.get(name.toString());
                ss.append(object);// 只当成字符串拼接
                continue;
            } else if ('\'' == c) {
                isQuote = !isQuote;
            } else if (!isQuote) {
                if (' ' == c || '\n' == c || '\r' == c || '\t' == c) {
                    int newStrLength = ss.length();
                    // 新SQL前面不是空格，老SQL后面不是空格给补一个空格
                    int nextI = i + 1;
                    if (newStrLength > 0 && ' ' != ss.charAt(newStrLength - 1)
                            && sqlLength > nextI && ' ' != sql.charAt(nextI)) {
                        ss.append(' ');
                    }
                    continue;
                } else if (HASH == c && ' ' == sql.charAt(i + 1)) {
                    for (; i < sqlLength; i++) {
                        if ('\n' == sql.charAt(i)) {
                            break;
                        }
                    }
                    continue;
                }
            }
            ss.append(c);
        }

        return ss.toString();
    }

    /**
     * 转换SQL模版
     * <pre>
     *     String month = "201905";
     *     tb_user_${month} > tb_user_201905
     *
     *
     *     #if()
     *
     *     #elseif()
     *
     *     #else
     *
     *     #end
     * </pre>
     *
     * @param sql    模版
     * @param params 参数
     * @return 分析后的SQL
     */
    public static String parse(String sql, Map<String, Object> params) {

        if (-1 == sql.indexOf(HASH) && -1 == sql.indexOf($)) {
            return sql;
        }
        StandardEvaluationContext seContext = null;
        if (sql.contains(HASH_IF)) {
            seContext = new StandardEvaluationContext();
            seContext.setVariables(params);
        }
        StringBuilder ss = new StringBuilder();
        int sqlLength = sql.length();
        for (int i = 0; i < sqlLength; i++) {
            char c = sql.charAt(i);
            if (HASH == c) {
                i = parseIfElseIfElseEnd(sql, i, ss, seContext);
            } else {
                ss.append(c);
            }
        }
        return ss.toString();
    }

    /**
     * 判断接下来是否有 #if #end
     *
     * @param sql 模版
     * @param i   索引
     * @return
     */
    private static boolean hasNextHashIfEnd(String sql, int i) {
        for (int j = i; j < sql.length(); j++) {
            if (HASH != sql.charAt(j)) {
                continue;
            }
            if (hasNextKeyword(sql, HASH_IF, j)) {
                return true;
            }
            if (hasNextKeyword(sql, HASH_ELSE_IF, j)) {
                return false;
            }
            if (hasNextKeyword(sql, HASH_ELSE, j)) {
                return false;
            }
            if (hasNextKeyword(sql, HASH_END, j)) {
                return false;
            }
        }
        return false;
    }

    /**
     * 处理 #if #elseIf #else #end
     *
     * @param sql       模版
     * @param i         索引
     * @param ss        结果
     * @param seContext 判断
     * @return
     */
    private static int parseIfElseIfElseEnd(String sql, int i, StringBuilder ss, StandardEvaluationContext seContext) {
        if (!hasNextHashIfEnd(sql, i)) {
            return i;
        }
        char c = sql.charAt(i);
        if (')' == c || 'e' == c) {
            i++;// 递归会用到 ? ? ?
        }

        boolean append = true;

        // 协助流程判断
        boolean _if = false, _elseIf = false;

        int j = i;
        for (; j < sql.length(); j++) {
            c = sql.charAt(j);
            if (HASH != c) {
                if (append) {
                    ss.append(c);
                }
                continue;
            }
            // #if
            if (hasNextKeyword(sql, HASH_IF, j)) {
                j += 3;
                Expression e = getExpression(sql, HASH_IF, j);
                j = e.getEndIndex();

                append = SpEL.parseExpression(e.getContent()).getValue(seContext, Boolean.class);
                _if = append;

                if (append) {
                    j = parseIfElseIfElseEnd(sql, j, ss, seContext);
                    continue;
                } else {
                    j = ignoreInternalIfEnd(sql, j);
                }
            }
            // #elseIf
            else if (hasNextKeyword(sql, HASH_ELSE_IF, j)) {
                j += 7;
                if (_if || _elseIf) {
                    j = ignoreInternalIfEnd(sql, j);
                    append = false;
                    continue;
                }
                Expression e = getExpression(sql, HASH_ELSE_IF, j);
                j = e.getEndIndex();
                append = SpEL.parseExpression(e.getContent()).getValue(seContext, Boolean.class);
                _elseIf = append;

                if (append) {
                    j = parseIfElseIfElseEnd(sql, j, ss, seContext);
                    continue;
                } else {
                    j = ignoreInternalIfEnd(sql, j);
                }
            }
            // #else
            else if (hasNextKeyword(sql, HASH_ELSE, j)) {
                j += 4;
                if (_if || _elseIf) {
                    j = ignoreInternalIfEnd(sql, j);
                    append = false;
                    continue;
                }
                j = parseIfElseIfElseEnd(sql, j, ss, seContext);
                append = true;
                continue;
            }
            // #end
            else if (hasNextKeyword(sql, HASH_END, j)) {
                j += 3;
                break;
            }

            // append #other ?
            if (append) {
                ss.append(c);
            }

        }
        return j;
    }

    /**
     * 忽略#if、#elseIf 内部 #if ... #end
     *
     * @param sql 模版
     * @param i   索引
     * @return 接下来的索引
     */
    private static int ignoreInternalIfEnd(String sql, int i) {
        int countIf = 0;
        int index = i;
        for (; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (HASH == c) {
                if (hasNextKeyword(sql, HASH_IF, i)) {
                    countIf++;
                } else if (0 < countIf && hasNextKeyword(sql, HASH_END, i)) {
                    countIf--;
                    index = i;
                } else if (0 == countIf && (hasNextKeyword(sql, HASH_ELSE_IF, i)
                        || hasNextKeyword(sql, HASH_ELSE, i)
                        || hasNextKeyword(sql, HASH_END, i))) {
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 判断接下来的字符串是否为keyword
     *
     * @param sql     模版
     * @param keyword 关键字
     * @param i       索引
     * @return 结果
     */
    private static boolean hasNextKeyword(String sql, String keyword, int i) {
        if (null == keyword || null == sql) {
            return false;
        }

        int sourceLen = sql.length();
        int nextLen = keyword.length();
        if (sourceLen < i + nextLen) {
            return false;
        }
        for (int j = 0; j < nextLen; j++) {
            char c1 = sql.charAt(i + j);
            c1 = Character.toLowerCase(c1);
            char c2 = keyword.charAt(j);
            c2 = Character.toLowerCase(c2);
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取 #if (Expression)
     *
     * @param sql    模版
     * @param syntax 语法
     * @param i      索引
     * @return Expression
     */
    private static Expression getExpression(String sql, String syntax, int i) {

        StringBuilder el = new StringBuilder();
        int counter = 0;// `(` 计数器
        int j = i;
        for (; j < sql.length(); j++) {
            char c = sql.charAt(j);
            if (0 == counter && c != ' ' && c != '(') {
                throw new EormException(syntax + c + " Syntax fail");
            }
            if ('(' == c) {
                counter++;
                if (1 == counter) {
                    continue;
                }
            }
            if (')' == c) {
                counter--;
                if (0 == counter) {
                    break;
                }
            }

            if (0 < counter) {
                el.append(c);
            }
        }
        return new Expression(j, el.toString());
    }

    /**
     * @param tsql
     * @param clazz
     * @return
     */
    public static EAsteriskColumn parseAsteriskColumn(String tsql, Class<?> clazz) {
        tsql = format(tsql);
        List<String> aliasList = new ArrayList<>();
        Map<String, String> aliasTableMap = new HashMap<>();
        int len = tsql.length();
        StringBuilder alias = new StringBuilder();
        StringBuilder table = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = tsql.charAt(i);
            if ('.' == c && '*' == tsql.charAt(i + 1)) {
                if (alias.length() > 0) {
                    // System.out.println(word);
                    aliasList.add(alias.toString());
                }
                i++;
                continue;
            }
            if (' ' == c || ',' == c) {
                if (alias.length() > 0) {
                    alias = new StringBuilder();
                }
                continue;
            }
            if ('f' == c && hasNextKeyword(tsql, "from", i)) {
                if (aliasList.isEmpty()) {
                    break;
                }
                i += 4;

                alias = new StringBuilder();
                boolean appendTable = true, appendTableAlias = false;
                for (; i < len; i++) {
                    char cc = tsql.charAt(i);
                    if ('a' == cc && hasNextKeyword(tsql, "as ", i)) {
                        i += 3;
                        continue;
                    }
                    if (',' == cc) {// from a ,b b
                        addAlisaTable(aliasTableMap, alias, table);
                        table = new StringBuilder();
                        alias = new StringBuilder();
                        appendTable = true;
                        appendTableAlias = false;
                        continue;
                    } else if ('j' == cc && hasNextKeyword(tsql, "join", i)) {
                        // inner|left|right join table_xx alias_xx
                        addAlisaTable(aliasTableMap, alias, table);
                        table = new StringBuilder();
                        alias = new StringBuilder();
                        appendTable = true;
                        appendTableAlias = false;
                        i += 4;
                        for (; i < len; i++) {
                            char ccc = tsql.charAt(i);
                            if ('a' == ccc && hasNextKeyword(tsql, "as ", i)) {
                                i += 3;
                                continue;
                            }
                            if (' ' == ccc) {
                                if (table.length() > 0) {
                                    appendTable = false;
                                    appendTableAlias = true;
                                }
                                continue;
                            } else if (appendTableAlias) {
                                if ('i' == ccc && hasNextKeyword(tsql, "inner ", i)) {
                                    i += 6;
                                    addAlisaTable(aliasTableMap, alias, table);
                                    alias = new StringBuilder();
                                    table = new StringBuilder();
                                    appendTable = true;
                                    appendTableAlias = false;
                                    break;
                                } else if ('l' == ccc && hasNextKeyword(tsql, "left ", i)) {
                                    i += 5;
                                    addAlisaTable(aliasTableMap, alias, table);
                                    alias = new StringBuilder();
                                    table = new StringBuilder();
                                    appendTable = true;
                                    appendTableAlias = false;
                                    break;
                                } else if ('r' == ccc && hasNextKeyword(tsql, "right ", i)) {
                                    i += 6;
                                    addAlisaTable(aliasTableMap, alias, table);
                                    alias = new StringBuilder();
                                    table = new StringBuilder();
                                    appendTable = true;
                                    appendTableAlias = false;
                                    break;
                                } else if (hasNextKeyword(tsql, "on ", i)) {
                                    i += 2;
                                    addAlisaTable(aliasTableMap, alias, table);
                                    alias = new StringBuilder();
                                    table = new StringBuilder();
                                    appendTable = false;
                                    appendTableAlias = false;
                                    break;
                                }
                            }

                            if (appendTable) {
                                table.append(ccc);
                            }
                            if (appendTableAlias) {
                                alias.append(ccc);
                            }
                        }
                        continue;
                    } else if (' ' == cc) {
                        if (table.length() > 0) {
                            appendTable = false;
                            appendTableAlias = true;
                        }
                        continue;
                    } else if ('w' == cc && hasNextKeyword(tsql, "where", i)) {
                        addAlisaTable(aliasTableMap, alias, table);
                        break;
                    }

                    if (appendTable) {
                        table.append(cc);
                    }

                    if (appendTableAlias) {
                        alias.append(cc);
                    }
                }
                break;// 结束
            }
            alias.append(c);
        }

        Set<Field> fields = EReflectUtils.getFields(clazz);

        EAsteriskColumn ac = new EAsteriskColumn();
        Map<String, Field> fieldNames = new HashMap<>();
        for (Field field : fields) {
            fieldNames.put(field.getName(), field);
            fieldNames.put(EReflectUtils.humpToUnderline(field.getName()), field);
        }
        if (!aliasList.isEmpty()) {
            for (String a : aliasList) {
                Field field = fieldNames.get(a);
                if (null == field) {
                    continue;
                }
                ac.getFields().put(a, field);
                String tableName = aliasTableMap.get(a);
                Table table1 = ETableColumnParser.getTable(tableName);
                List<Column> columns = table1.getAllColumns();

                StringBuilder sss = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    Column column = columns.get(i);
                    sss.append(a);
                    sss.append(".");
                    sss.append(column.getName());
                    sss.append(" ");
                    sss.append(a);
                    sss.append("__");
                    sss.append(column.getName());
                    if (i < columns.size() - 1) {
                        sss.append(",");
                    }
                }
                tsql = tsql.replace(a + ".*", sss);
            }
        }

        ac.setTsql(tsql);
        return ac;
    }


    private static void addAlisaTable(Map<String, String> aliasTableMap, StringBuilder alisa, StringBuilder table) {
        String tableName = table.toString();
        if (table.length() == 0) {
            return;
        }
        if (alisa.length() > 0) {
            aliasTableMap.put(alisa.toString(), tableName);
        } else {
            aliasTableMap.put(tableName, tableName);
        }
    }

}
