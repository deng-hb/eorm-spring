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
        where rr.student_id = s.id and rr.book_id = book.id
        and s.id in (select id from student where gender = 1)
    }*/;

    @Test
    public void test1() {

        List<ReadRecordModel> list = db.select(ReadRecordModel.class, sql, new HashMap<>());

        System.out.println(list);


    }

    public static void main(String[] args) {
        // druidParser();
        ccparser();
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
