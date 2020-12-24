package com.denghb.xxlibrary.test;


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.denghb.eorm.support.EAsteriskColumn;
import com.denghb.eorm.support.ESelectParser;
import com.denghb.eorm.support.domain.SelectSQL;
import com.denghb.eorm.utils.ESQLTemplateUtils;
import com.denghb.xxlibrary.model.ReadRecordModel;
import com.denghb.xxlibrary.model.TestSubSelectModel;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.Test;

import java.io.StringReader;
import java.util.*;

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
        String tsql1 = ""/*{
            select s.*, s1.* from student s inner join (
                select a.id, a.name from student a where gender in (1)
            ) s1 on s1.id = s.id
        }*/;
        String tsql = ""/*{
            select s.*, s1.* from student s inner join (
                select * from student
            ) s1 on s1.id = s.id
        }*/;
        tsql = ESQLTemplateUtils.format(tsql);
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
            if (ESQLTemplateUtils.hasNextKeyword(tsql, " from ", i)) {
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
                    List<String> cc = new ArrayList<>();
                    StringBuilder column = new StringBuilder();
                    int s2 = 0;
                    for (int k = 0; k < s.length(); k++) {
                        char c1 = s.charAt(k);
                        if (ESQLTemplateUtils.hasNextKeyword(s, " select ", k)) {
                            k += 7;
                        } else if ('(' == c1) {
                            s2++;
                        } else if (')' == c1) {
                            s2--;
                        } else if (s2 == 0 && ',' == c1) {
                            System.out.println(column);
                            cc.add(column.toString());
                            column = new StringBuilder();
                        } else if (ESQLTemplateUtils.hasNextKeyword(s, " from ", k)) {
                            System.out.println(column);
                            cc.add(column.toString());
                            break;
                        } else if (' ' == c1) {
                        } else {
                            column.append(c1);
                        }
                    }
                    System.out.println(cc);
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

    @Test
    public void test2() {

        String tsql1 = ""/*{
            select s.*, s1.* from student s inner join (
                select a.* from student a
            ) s1 on s1.id = s.id
        }*/;
        String tsql = ""/*{
            select s.*, s1.* from student s inner join (
                select a.id, a.name from student a
            ) s1 on s1.id = s.id
        }*/;
        EAsteriskColumn ac = ESelectParser.parse(tsql, TestSubSelectModel.class);
        System.out.println(ac);
        List<TestSubSelectModel> list = db.select(TestSubSelectModel.class, tsql);

        System.out.println(list);
    }


    static String sss = ""/*{
select s.*, s1.* from student s inner join ( select a.id, a.name from student2 a ) s1 on s1.id = s.id
    }*/;
    static String sss1 = ""/*{
select a.id, a.name from student a
    }*/;
    static String sss2 = ""/*{
select * from student
    }*/;
    static String sss3 = ""/*{
select s.*, s1.* from student s , ( select a.id, a.name from student2 a ) s1 where s1.id = s.id
    }*/;
    static String sss4 = ""/*{
select a.a, a.b, a.c, b.c, b.d, b.f from a LEFT OUT JOIN b ON a.a = b.c
    }*/;
    static String sss5 = ""/*{
select * from a left inner join b on a.a=b.b right inner join c on a.a=c.c inner join d on a.a=d.d
    }*/;

    static String sss6 = ""/*{
select top 10 b.* from (select top 20 主键字段,排序字段 from 表名 order by 排序字段 desc) a,表名 b where b.主键字段 = a.主键字段 order by a.排序字段
    }*/;
    static String sss7 = ""/*{
select type,sum(case vender when 'A' then pcs else 0 end),sum(case vender when 'C' then pcs else 0 end),sum(case vender when 'B' then pcs else 0 end) FROM tablename group by type
    }*/;
    static String sss8 = ""/*{
select top 5 * from (select top 15 * from table order by id asc) table_别名 order by id desc
    }*/;
    static String sss9 = ""/*{
select name from syscolumns where id in (select id from sysobjects where type = 'u' and name = '表名')
    }*/;
    static String sss10 = ""/*{
select a.* from sysobjects a, syscomments b where a.id = b.id and b.text like '%表名%'
    }*/;
    static String sss11 = ""/*{
select column_name,data_type from information_schema.columns
    where table_name = '表名'
    }*/;

    static String sss12 = ""/*{
        select m.* , n.平均分 , n.总分 from
(select * from (select * from tb) a pivot (max(分数) for 课程 in (语文,数学,物理)) b) m,
(select 姓名 , cast(avg(分数*1.0) as decimal(18,2)) 平均分 , sum(分数) 总分 from tb group by 姓名) n
where m.姓名 = n.姓名
    }*/;

    static String sss13 = ""/*{
SELECT stuid FROM (SELECT tb_student.stuid, couid FROM tb_score
            RIGHT JOIN tb_student on tb_score.stuid = tb_student.stuid) as t1 WHERE couid is NULL;
    }*/;
    static String sss14 = ""/*{
SELECT stuname, birth FROM tb_student WHERE birth = (SELECT max(birth) FROM tb_student);

    }*/;
    static String sss15 = ""/*{
SELECT stuname, avgmark FROM (SELECT stuid, avg(mark) as avgmark FROM tb_score GROUP BY(stuid)) as temp
INNER JOIN tb_student on temp.stuid=tb_student.stuid;
    }*/;
    static String sss16 = ""/*{
SELECT stuid,avg(mark) as avgmark FROM tb_score GROUP BY(stuid) HAVING avg(mark)>90;

    }*/;
    static String sss17 = ""/*{
SELECT if(gender,'男','女') as '性别',COUNT(stuid) as '人数' FROM tb_student GROUP BY(gender);

    }*/;

    static String sss18 = ""/*{
select * from (select * from tb) a pivot (max(分数) for 课程 in (语文,数学,物理)) b
    }*/;


    public static void main(String[] args) {

        SelectSQL ss = ESelectParser.parse(sss15);

        System.out.println(ss);
        System.out.println(ESelectParser.parse(sss));
        System.out.println(ESelectParser.parse(sss1));
        System.out.println(ESelectParser.parse(sss2));
        System.out.println(ESelectParser.parse(sss3));

        System.out.println(ESelectParser.parse(sss4));
        System.out.println(ESelectParser.parse(sss5));
        System.out.println(ESelectParser.parse(sss6));

        System.out.println(ESelectParser.parse(sss7));
        System.out.println(ESelectParser.parse(sss8));
        System.out.println(ESelectParser.parse(sss9));

        System.out.println(ESelectParser.parse(sss10));
        System.out.println(ESelectParser.parse(sss11));
        System.out.println(ESelectParser.parse(sss12));

        System.out.println(ESelectParser.parse(sss13));
        System.out.println(ESelectParser.parse(sss14));
        System.out.println(ESelectParser.parse(sss15));

        System.out.println(ESelectParser.parse(sss16));
        System.out.println(ESelectParser.parse(sss17));
        System.out.println(ESelectParser.parse(sss18));
        // druidParser();
//        ccparser();
//        ESQLTemplate.parseAsteriskColumn(sql3, ReadRecordModel.class);
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
