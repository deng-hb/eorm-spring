package com.denghb.eorm.template;


import com.denghb.eorm.EormException;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.utils.EReflectUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public abstract class EQueryTemplate {
    // 性能？
    private static final ExpressionParser SpEL = new SpelExpressionParser();

    /**
     * 美化SQL
     * 去掉多余的换行和空格
     *
     * @param sql 原SQL
     * @return 美化后的SQL
     */
    public static String format(String sql, Object... args) {
        if (null == sql) {
            return null;
        }
        boolean isBlank = false;
        boolean isQuote = false;
        StringBuilder ss = new StringBuilder();
        int sqlLength = sql.length();
        for (int i = 0; i < sqlLength; i++) {

            char c = sql.charAt(i);
            if ('\'' == c) {
                isQuote = !isQuote;
            }
            if (' ' == c && !isQuote) {
                if (!isBlank) {
                    isBlank = true;
                    ss.append(c);
                }
                continue;
            }
            if (('\n' == c || '\r' == c || '\t' == c) && !isQuote) {
                int newStrLength = ss.length();
                // 新SQL前面不是空格，老SQL后面不是空格给补一个空格
                int nextI = i + 1;
                if (newStrLength > 0 && ' ' != ss.charAt(newStrLength - 1)
                        && sqlLength > nextI && ' ' != sql.charAt(nextI)) {
                    ss.append(' ');
                }
                continue;
            }

            ss.append(c);
            isBlank = false;
        }

        return ss.toString().trim();
    }

    private static final char $ = '$';
    private static final char HASH = '#';
    private static final String HASH_IF = "#if";
    private static final String HASH_ELSE_IF = "#elseIf";
    private static final String HASH_ELSE = "#else";
    private static final String HASH_END = "#end";

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

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            // a${b}c > abc
            if ($ == c && '{' == sql.charAt(i + 1)) {
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
            } else {
                ss.append(c);
            }
        }
        sql = ss.toString();
        ss = new StringBuilder();
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

        int end = i + keyword.length();
        if (sql.length() < end) {
            return false;
        }

        return keyword.equalsIgnoreCase(sql.substring(i, end));
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

}
