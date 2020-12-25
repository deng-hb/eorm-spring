package com.denghb.eorm.utils;

import com.denghb.eorm.EOrmException;
import com.denghb.eorm.support.EKeyHolder;
import com.denghb.eorm.support.domain.ESQLParameter;
import com.denghb.eorm.support.domain.ETableRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 支持使用Annotation标注表结构
 */
public class ESQLTemplateUtils {

    // <class, ETableRef>
    private static final Map<Class<?>, ETableRef> TABLE_REF_CACHE = new ConcurrentHashMap<Class<?>, ETableRef>();
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

    /**
     * 1.美化并去掉注释
     * 2.拼接${name}
     * 3.
     *
     * @param sql
     * @param args
     * @return
     */
    public static ESQLParameter parse(String sql, Object... args) {
        ESQLParameter sp = new ESQLParameter();

        List<Object> list = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof EKeyHolder) {

                continue;
            }
            list.add(arg);
        }

        if (list.size() == 1) {
            Object object = list.get(0);
            if (!EReflectUtils.isSingleClass(object.getClass())) {

            }
        }
        // sql = format(sql, );


        return sp;
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
    public static boolean hasNextKeyword(String sql, String keyword, int i) {
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
                throw new EOrmException(syntax + c + " Syntax fail");
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

    @Data
    @AllArgsConstructor
    public static class Expression {

        private int endIndex;

        private String content;
    }
}
