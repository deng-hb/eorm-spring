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
import com.denghb.eorm.template.EAsteriskColumn;
import com.denghb.eorm.template.ESQLTemplate;
import com.denghb.eorm.utils.EReflectUtils;
import com.denghb.xxlibrary.model.ReadRecordModel;
import com.denghb.xxlibrary.model.TestSubSelectModel;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.Test;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        where rr.student_id = s.id and rr.book_id = book.id
        and s.id in (select id from student where gender = 1)
    }*/;

    @Test
    public void test1() {

        List<ReadRecordModel> list = db.select(ReadRecordModel.class, sql, new HashMap<>());

        System.out.println(list);


    }

    @Test
    public void parse() throws JSQLParserException {
        // 解析子查询
        String tsql = ""/*{
            select s.*, s1.* from student s inner join (
                select a.id, a.name from student a where gender in (1)
            ) s1 on s1.id = s.id
        }*/;
        tsql = ESQLTemplate.format(tsql);
        StringBuilder alias = new StringBuilder();
        List<String> aliasList = new ArrayList<>();
        int start = 0;
        int length = tsql.length();
        for (int i = 0; i < length; i++) {
            char c = tsql.charAt(i);
            if ('.' == c && '*' == tsql.charAt(i + 1)) {
                if (alias.length() > 0) {
                    // System.out.println(word);
                    aliasList.add(alias.toString());
                }
                i++;
                continue;
            }
            if (ESQLTemplate.hasNextKeyword(tsql, " from ", i)) {
                start = i + 5;
                break;
            }
            if (' ' == c || ',' == c) {
                if (alias.length() > 0) {
                    alias = new StringBuilder();
                }
                continue;
            }
            alias.append(c);
        }

        Map<String, String> aliasTable = new HashMap<>();
        // 查找对应的列
        for (String a : aliasList) {
            int end = tsql.indexOf(" " + a + " ");
            StringBuilder table = new StringBuilder();
            for (int i = end; i > start; i--) {
                char c = tsql.charAt(i);
                if (' ' == c) {
                    if (table.length() > 0) {
                        aliasTable.put(a, table.reverse().toString());
                        table = new StringBuilder();
                    }
                    continue;
                }
                if (')' == c) {
                    // 表示这个是临时表，需要找到字段
                    int d = 1;
                    i--;
                    int start2 = 0;
                    for (int j = i; j > start; j--) {
                        c = tsql.charAt(j);
                        if (')' == c) {
                            d++;
                        }
                        if ('(' == c) {
                            d--;
                            if (0 == d) {
                                start2 = j + 1;
                            }
                        }
                    }

                    String s = tsql.substring(start2, i);
                    System.out.println(s);
                    StringBuilder column = new StringBuilder();
                    int s2 = 0;
                    for (int k = 0; k < s.length(); k++) {
                        char c1 = s.charAt(k);
                        if (ESQLTemplate.hasNextKeyword(s, " select ", k)) {
                            k += 7;
                        } else if ('(' == c1) {
                            s2++;
                        } else if (')' == c1) {
                            s2--;
                        } else if (',' == c1) {
                            System.out.println(column);
                            column = new StringBuilder();
                        } else if (ESQLTemplate.hasNextKeyword(s, " from ", k)) {
                            System.out.println(column);
                            break;
                        } else if (' ' == c1) {
                        } else {
                            column.append(c1);
                        }
                    }
                }
                table.append(c);
            }
            if (table.length() > 0) {
                aliasTable.put(a, table.reverse().toString());
            }
        }
        System.out.println(aliasTable);
        /*
        List<TestSubSelectModel> list = db.select(TestSubSelectModel.class, sql);

        System.out.println(list);

         */
    }

    String sss = ""/*{
select s.*, s1.* from student s inner join ( select a.id, a.name from student a ) s1 on s1.id = s.id
    }*/;

    public static void main(String[] args) {
        // druidParser();
//        ccparser();
        ESQLTemplate.parseAsteriskColumn(sql3, ReadRecordModel.class);
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


}
