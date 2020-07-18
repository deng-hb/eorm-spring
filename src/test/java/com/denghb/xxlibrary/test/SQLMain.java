package com.denghb.xxlibrary.test;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLMain {
 
	private static final String FROM_LOWER = "from";
	private static final String FROM_UPPER = "FROM";
	private static final String[] SPLIT_KEY = {"select", "SELECT", " where ", " WHERE "};
	private static final String[] JOIN_KEY = {",", " join ", " JOIN "};
 
	public static void main(String[] args) {
//		String sql = "select a, b, c from user u,org o where u.id = o.uid and u.id in (select id from user_org uo)";
//		String sql = "select * FROM     u   order    by  b";
//		String sql = "select count(a) from `user`  u  ,   org  o  where u.a = o.b group by d";
		String sql = "select a, b, c from user u,org o where u.id = o.uid and u.id in (select id from user_org uo LEFT JOIN table_o to " +
				"where uo.id not exist (select id from org_user  ))";
//		String sql = "select a, b, c from user u INNER   JOIN  org o on u.id = o.id where user.a = 'aaa'";
		sql = formatSQL(sql);
		List<String> list = new ArrayList<>();
		if (getSplitKey(sql) != null) {
			splitSQL(sql, list);
		}
		Set<String> tables = new HashSet<>();
		for (String s : list) {
			getTables(s, tables);
		}
		for (String table : tables) {
			System.out.println(table);
		}
	}
 
	private static void getTables(String str, Set<String> tables) {
		if (str.contains(FROM_LOWER)) {
			str = str.substring(str.indexOf(FROM_LOWER) + 5, str.length());
		} else if (str.contains(FROM_UPPER)) {
			str = str.substring(str.indexOf(FROM_UPPER) + 5, str.length());
		} else {
			return;
		}
		for (String key : JOIN_KEY) {
			for (String s : str.split(key)) {
				tables.add(s.split(" ")[0]);
			}
		}
	}
 
	private static void splitSQL(String sql, List<String> temp) {
		// 根据from关键字切割SQL
		sql = sql.contains("from") ? sql.substring(sql.indexOf("from"), sql.length()) : sql.substring(sql.indexOf("FROM"), sql.length());
		// 是否需要二次切割
		if (getSplitKey(sql) != null) {
			for (String s : sql.split(getSplitKey(sql))) {
				if (getSplitKey(s) != null) {
					splitSQL(s, temp);
				} else {
					temp.add(s);
				}
			}
		} else {
			temp.add(sql);
		}
	}
 
	private static String getSplitKey(String str) {
		if (StringUtils.isNotBlank(str)) {
			for (String split : SPLIT_KEY) {
				if (str.contains(split)) {
					return split;
				}
			}
		}
		return null;
	}
 
	private static String formatSQL(String sql) {
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.replaceAll("\\s+,\\s+", ",");
		return sql;
	}
 
}