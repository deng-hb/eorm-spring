package com.denghb.xxlibrary.test;


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.denghb.eorm.support.ETableColumnParser;
import com.denghb.eorm.support.domain.Column;
import com.denghb.eorm.support.domain.Table;
import com.denghb.eorm.template.EQueryTemplate;
import com.denghb.eorm.utils.EReflectUtils;
import com.denghb.xxlibrary.model.ReadRecordModel;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.Test;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author denghb
 * @Date: 2019-05-26 00:49
 */
public class SelectMoreTest extends BaseTest {

    static String sql0 = ""/*{
        select readRecord.*, student.*, book.* from read_record
        left join student on read_record.student_id = student.id
        left join book on read_record.book_id = book.id
    }*/;

    static String sql1 = ""/*{
        select c.name commodityName, c.gtin, c.id commodityId, ifnull(t.sales,0) - ifnull(cs.stocknum,0) - ifnull(csi.size,0) prenum, wc.unit_size size from tb_warehouse_commodity wc
        left join tb_commodity c on wc.commodity_id = c.id
        left join (select sum(od.num) sales, od.commodity_id from tb_order_detail od left join tb_pay p
        on od.order_id = p.order_id left join tb_order o on o.id = od.order_id left join tb_counter c
        on o.counter_code = c.code where p.status = 11 and od.inserttime >= DATE_SUB(CURDATE(), INTERVAL 14 DAY) and c.warehouse_id = ? group by commodity_id) t on t.commodity_id = wc.commodity_id
        left join (select sum(num - sales) stocknum,commodity_id from tb_commodity_stock where warehouse_id = ? group by commodity_id) cs on cs.commodity_id = wc.commodity_id
        left join (select commodity_id,sum(size*piece) size from tb_commodity_stock_in where warehouse_id = ? and status = 1 and isactive = 1 group by commodity_id) csi
        on csi.commodity_id = wc.commodity_id
        where wc.warehouse_id = ? and wc.isactive = 1 and ifnull(t.sales,0) - ifnull(cs.stocknum,0) - ifnull(csi.size,0) > 0
    }*/;

    static String sql = ""/*{
        select readRecord.*, student.*, book.* from read_record readRecord, student, book
        where readRecord.student_id = student.id and readRecord.book_id = book.id
    }*/;

    static String sql2 = ""/*{
        select read_record.*, student.*, book.* from read_record, student, book
        where read_record.student_id = student.id and read_record.book_id = book.id
        and student.id in (select id from student where gender = 1)
    }*/;
    static String sql3 = ""/*{
        select rr.*, s.*, book.* from read_record rr, student s, book
        where rr.s = s.id and rr.book_id = book.id
        and s.id in (select id from student where gender = 1)
    }*/;

    @Test
    public void test1() {

        List<ReadRecordModel> list = db.select(ReadRecordModel.class, "select * from student ");

        System.out.println(list);

        String ss = parseAsteriskColumn(sql2, ReadRecordModel.class);

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(ss);

        Set<Field> fields = EReflectUtils.getFields(ReadRecordModel.class);
        Map<String, Field> fieldNames = new HashMap<>();
        for (Field field : fields) {
            fieldNames.put(field.getName(), field);
            fieldNames.put(humpToUnderline(field.getName()), field);
        }
        List<ReadRecordModel> modelList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            ReadRecordModel e = new ReadRecordModel();
            for (String key : map.keySet()) {
                for (String fieldName : fieldNames.keySet()) {
                    String fd = fieldName + "__";
                    if (key.contains(fd)) {
                        String realFieldName = key.substring(fd.length());
                        realFieldName = underlineToHump(realFieldName, false);

                        Field field = fieldNames.get(fieldName);
                        Object fieldObj = EReflectUtils.getFieldValue(field, e);
                        if (null == fieldObj) {
                            fieldObj = EReflectUtils.constructorInstance(field.getType());
                            EReflectUtils.setFieldValue(field, e, fieldObj);
                        }
                        Object v = map.get(key);
                        Class<?> subFieldType = EReflectUtils.getField(field.getType(), realFieldName).getType();
                        if (subFieldType.getSuperclass() == Number.class) {
                            // 数字
                            v = EReflectUtils.constructorInstance(subFieldType, String.class, String.valueOf(v));
                        }
                        EReflectUtils.setValue(fieldObj, realFieldName, v);
                    }
                }
            }
            modelList.add(e);
        }
        System.out.println(modelList);

    }

    private static final char[] ALL_SYMBOL = "=+-*/><(),.%|&?!".toCharArray();

    private static boolean contains(char c) {

        for (int i = 0; i < ALL_SYMBOL.length; i++) {
            if (ALL_SYMBOL[i] == c) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // druidParser();
        ccparser();
        parseAsteriskColumn(sql3, ReadRecordModel.class);
    }

    private static String parseAsteriskColumn(String tsql, Class<?> modelClass) {
        tsql = EQueryTemplate.format(tsql);
        System.out.println(tsql);
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
            if (' ' == c || contains(c)) {
                if (alias.length() > 0) {
                    alias = new StringBuilder();
                }
                continue;
            }
            if ('f' == c && hasNextWord(tsql, i, "from")) {
                if (aliasList.isEmpty()) {
                    break;
                }
                i += 4;

                alias = new StringBuilder();
                boolean appendTable = true, appendTableAlias = false;
                for (; i < len; i++) {
                    char cc = tsql.charAt(i);
                    if ('a' == cc && hasNextWord(tsql, i, "as ")) {
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
                    } else if ('j' == cc && hasNextWord(tsql, i, "join")) {
                        // inner|left|right join table_xx alias_xx
                        addAlisaTable(aliasTableMap, alias, table);
                        table = new StringBuilder();
                        alias = new StringBuilder();
                        appendTable = true;
                        appendTableAlias = false;
                        i += 4;
                        for (; i < len; i++) {
                            char ccc = tsql.charAt(i);
                            if ('a' == ccc && hasNextWord(tsql, i, "as ")) {
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
                                if ('i' == ccc && hasNextWord(tsql, i, "inner ")) {
                                    i += 6;
                                    addAlisaTable(aliasTableMap, alias, table);
                                    alias = new StringBuilder();
                                    table = new StringBuilder();
                                    appendTable = true;
                                    appendTableAlias = false;
                                    break;
                                } else if ('l' == ccc && hasNextWord(tsql, i, "left ")) {
                                    i += 5;
                                    addAlisaTable(aliasTableMap, alias, table);
                                    alias = new StringBuilder();
                                    table = new StringBuilder();
                                    appendTable = true;
                                    appendTableAlias = false;
                                    break;
                                } else if ('r' == ccc && hasNextWord(tsql, i, "right ")) {
                                    i += 6;
                                    addAlisaTable(aliasTableMap, alias, table);
                                    alias = new StringBuilder();
                                    table = new StringBuilder();
                                    appendTable = true;
                                    appendTableAlias = false;
                                    break;
                                } else if (hasNextWord(tsql, i, "on ")) {
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
                    } else if ('w' == cc && hasNextWord(tsql, i, "where")) {
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

        Set<Field> fields = EReflectUtils.getFields(modelClass);
        List<String> fieldNames = new ArrayList<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            fieldNames.add(humpToUnderline(field.getName()));
        }
        if (!aliasList.isEmpty()) {
            for (String a : aliasList) {
                if (!fieldNames.contains(a)) {
                    continue;
                }
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
                System.out.println(table1);
            }
        }


        System.out.println(aliasList);
        System.out.println(aliasTableMap);
        System.out.println(tsql);
        return tsql;
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

    private static boolean hasNextWord(String source, int i, String word) {
        int sourceLen = source.length();
        int nextLen = word.length();
        if (sourceLen < i + nextLen) {
            return false;
        }
        for (int j = 0; j < nextLen; j++) {
            char c1 = source.charAt(i + j);
            c1 = Character.toLowerCase(c1);
            char c2 = word.charAt(j);
            c2 = Character.toLowerCase(c2);
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }

    private static void druidParser() {

        // 1、将from前的xxx.*
        String result = SQLUtils.format(sql1, JdbcConstants.MYSQL);
        MySqlStatementParser mySqlStatementParser = new MySqlStatementParser(sql0);
        SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) mySqlStatementParser.parseSelect();

        SQLSelect sqlSelect = sqlSelectStatement.getSelect();
        SQLSelectQuery sqlSelectQuery = sqlSelect.getQuery();
        if (sqlSelectQuery instanceof MySqlSelectQueryBlock) {
            MySqlSelectQueryBlock mySqlSelectQueryBlock = (MySqlSelectQueryBlock) sqlSelectQuery;
            MySqlOutputVisitor where = new MySqlOutputVisitor(new StringBuilder());
            // 获取where 条件
            mySqlSelectQueryBlock.getWhere().accept(where);
            System.out.println("##########where###############");
            System.out.println(where.getAppender());
            // 获取表名
            System.out.println("############table_name##############");
            MySqlOutputVisitor tableName = new MySqlOutputVisitor(new StringBuilder());
            mySqlSelectQueryBlock.getFrom().accept(tableName);
            System.out.println(tableName.getAppender());

            //   获取查询字段
            System.out.println("############查询字段##############");
            System.out.println(mySqlSelectQueryBlock.getSelectList());
        }
    }

    private static void ccparser() {

        CCJSqlParserManager pm = new CCJSqlParserManager();
        Statement statement = null;
        try {
            statement = pm.parse(new StringReader(sql1));
            Select select = (Select) statement;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            for (SelectItem selectItem : selectItems) {
                SelectExpressionItem sei = (SelectExpressionItem) selectItem;
                System.out.println(sei.getAlias());
            }
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(select);
            for (String tableName : tableList) {
                System.out.println(tableName);
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

    }

    /**
     * 驼峰格式转换为下划线格式
     */
    public static String humpToUnderline(String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i > 0 && Character.isUpperCase(ch)) {// 首字母是大写不需要添加下划线
                builder.append('_');
            }
            builder.append(ch);
        }

        int startIndex = 0;
        if (builder.charAt(0) == '_') {//如果以下划线开头则忽略第一个下划线
            startIndex = 1;
        }
        return builder.substring(startIndex).toLowerCase();
    }


    /**
     * 下划线格式转换为驼峰格式
     */
    public static String underlineToHump(String name, boolean firstCharToUpper) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i == 0 && firstCharToUpper) {
                builder.append(Character.toUpperCase(ch));
            } else {
                if (i > 0 && ch == '_') {// 首字母是大写不需要添加下划线
                    i++;
                    ch = name.charAt(i);
                    builder.append(Character.toUpperCase(ch));
                } else {
                    builder.append(ch);
                }
            }
        }
        return builder.toString();
    }

}
