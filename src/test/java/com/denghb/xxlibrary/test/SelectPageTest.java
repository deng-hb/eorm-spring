/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.xxlibrary.test;

import com.denghb.xxlibrary.domain.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/11/14 23:51
 */
@Slf4j
public class SelectPageTest extends BaseTest {

    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();

    @Test
    public void test() throws IOException {
        scanClasses("com.denghb", null);
    }

    private Set<Class<?>> scanClasses(String packagePatterns, Class<?> assignableType) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String[] packagePatternArray = tokenizeToStringArray(packagePatterns,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        for (String packagePattern : packagePatternArray) {
            Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class");
            for (Resource resource : resources) {
                try {
                    ClassMetadata classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = Resources.classForName(classMetadata.getClassName());
                    if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

    @Test
    public void test1() {
        int a = 200;
        Integer b = new Integer(200);
        System.out.println(a == b);
        String sql = ""/*{
            select sql_calc_found_rows * from student where name = :name and age in (:ages) limit 0,3
        }*/;
        List<Integer> ages = new ArrayList<>();
        ages.add(12);
        ages.add(13);
        Map<String, Object> parameter = new HashMap<String, Object>() {{
            put("name", "zhangsan");
            put("ages", ages);
        }};
        List<Student> list = db.select(Student.class, sql, parameter);


        Object[] s = NamedParameterUtils.buildValueArray(sql, parameter);
        ParsedSql ps = NamedParameterUtils.parseSqlStatement(sql);
        String ss = NamedParameterUtils.parseSqlStatementIntoString(sql);
        System.out.println(list);
        Integer rows = db.selectOne(Integer.class, "select found_rows()");

        System.out.println(rows);

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        long totalCount = 0L;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            conn.setAutoCommit(true);
            statement = conn.createStatement();
            rs = statement.executeQuery(sql);

            ResultSetMetaData md = rs.getMetaData(); //获得结果集结构信息,元数据
            int columnCount = md.getColumnCount();   //获得列数

            List<Map<String, Object>> data = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<String, Object>();

                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                data.add(rowData);

                // 类型转换
                DefaultConversionService.getSharedInstance();
            }

            String totalCountSQL = "select found_rows() AS total_count";
            rs1 = statement.executeQuery(totalCountSQL);
            while (rs1.next()) {
                totalCount = rs1.getLong("total_count");
            }

            System.out.println(totalCount);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeResultSet(rs1);
            JdbcUtils.closeStatement(statement);
            //释放资源
            DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
        }
    }
}
