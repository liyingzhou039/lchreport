package com.lch.report.util;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lch.report.dto.Column;
import com.lch.report.dto.Condition;
import com.lch.report.dto.Equal;
import com.lch.report.dto.Like;
import com.lch.report.dto.Report;
import com.lch.report.dto.Table;
import com.lch.report.power.PowerUtil;

public class ReportUtil {
	public static Connection openConn() {
		Connection conn=null;
		String fileName="db.local.properties";
		String driver=null;
		String dbUrl=null;
		String dbName=null;
		String dbPassword=null;
		try {

			Properties p = new Properties();
			InputStream is = ReportUtil.class.getClassLoader().getResourceAsStream(fileName);
			p.load(is);
			driver = p.getProperty("db.driver");
			dbUrl = p.getProperty("db.url");
			dbName = p.getProperty("db.username");
			dbPassword = p.getProperty("db.password");
			Class.forName(driver);
			conn = DriverManager.getConnection(dbUrl, dbName, dbPassword);
		} catch (ClassNotFoundException e) {
			System.out.println("加载数据库驱动" + driver + "失败！");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	// 查询所有记录
	public  static List<String[]> findList(String sql) {
		System.out.println("-- sql=" + sql);
		List<String[]> list = new ArrayList<String[]>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn =ReportUtil.openConn();
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(sql);
			ResultSetMetaData md = rs.getMetaData();
			int rs_cols = md.getColumnCount();
			String[] data = new String[rs_cols];
			while (rs.next()) {
				data = new String[rs_cols];
				for (int i = 0; i < rs_cols; i++) {
					if (rs.getString(i + 1) != null) {
						data[i] = rs.getString(i + 1).trim();// 替换英文逗号为中文,防止被误用成字段分隔符(1列变多列).replaceAll(",",
																// "，")
					} else {
						data[i] = ""; // 不等于null,减少页面判断null
					}
				}
				list.add(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeAll(conn, stmt, rs);
		}
		return list;
	}
	/**
	 * 获取数据库的所有用户名
	 */
	public static List<String> getDbUserNames() {
		String sql = "select t.username from sys.all_users t order by t.username";
		List<String[]> ls = findList(sql);
		List<String> names=new ArrayList<String>();
		if (null != ls) {
			for (String[] ss : ls) {
				for (String s : ss) {
					names.add(s);
				}
			}
		}
		return names;
	}
	/**
	 * 获取指定用户的所有表和视图
	 */
	public static List<Table> getAllTables(String owner) {
		String sql = "select * from( select t1.table_name,t2.comments from sys.all_tables t1 ,sys.all_tab_comments t2 "
				+ "where t1.owner=t2.owner and t1.table_name=t2.table_name and t1.owner = '"
				+ owner
				+ "' and t2.table_type='TABLE' union  "
				+ "select t1.view_name,t2.comments  from sys.all_views t1 ,sys.all_tab_comments t2 "
				+ "where t1.owner=t2.owner and t1.view_name=t2.table_name and t1.owner = '"
				+ owner + "' and t2.table_type='VIEW' )order by 1";
		List<String[]> ls = findList(sql);
		List<Table> tables=new ArrayList<Table>();
		if (null != ls) {
			for (String[] ss : ls) {
				if(ss!=null&&ss.length==2){
					Table tb=new Table();
					tb.setName(ss[0]);
					tb.setComments(ss[1]);
					tables.add(tb);
				}
			}
		}
		return tables;
	}
	/**
	 * 获取指定表的信息
	 */
	public static Table getTableInfo(String tableName,String owner) {
		tableName = tableName.toUpperCase();
		if (tableName.indexOf(".") > 0) {
			owner = tableName.substring(0, tableName.indexOf("."));
			tableName = tableName.substring(tableName.indexOf(".") + 1,
					tableName.length());
		}
		Table table=new Table();
		table.setName(tableName);
		table.setOwner(owner);
		List<Column> cols = new ArrayList<Column>();
		table.setCols(cols);
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn =openConn();
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			rs = databaseMetaData.getColumns("", owner, tableName, "%"); // 获得指定tableName对应的列
			while (rs.next()) {
				Column col=new Column();
				col.setColName(rs.getString("COLUMN_NAME")/*.toLowerCase()*/);
				int decimal = rs.getInt("DECIMAL_DIGITS");
				String tmp = "";
				if (decimal != 0) {
					tmp = "," + decimal;
				}
				col.setColType(rs.getString("TYPE_NAME").toLowerCase() + "("
						+ rs.getInt("COLUMN_SIZE") + "" + tmp + ")");
				cols.add(col);
			}
			if (cols.size() >= 1) {
				String sql = "select b.comments from all_tab_columns a, all_col_comments b where a.table_name = b.table_name and a.column_name = b.column_name "
						+ "and a.owner = b.owner and a.table_name = '"
						+ tableName
						+ "' and a.owner = '"
						+ owner
						+ "' order by a.column_id";
				List<String[]> list = findList(sql);
				for (int i = 0; i < list.size(); i++) {
					String[] comment = (String[]) list.get(i);
					cols.get(i).setComments(comment[0]);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeAll(conn, stmt, rs);
		}
		return table;
	}

	public static String getSqlByReport(Report report,HttpServletRequest req, HttpServletResponse resp) {
		// 处理表
		String sql = "select ";
		List<Table> tables = report.getBind();
		if (null != tables && tables.size() > 0) {
			Table table = tables.get(0);
			List<Column> cols = table.getCols();
			String s = "";
			if (cols != null) {
				for (Column col : cols) {
					if (s.length() > 0) {
						s += ",";
					}
					s += "t1.\"" + col.getColName() + "\" \""
							+ col.getColName() + "\"";
				}
			}
			sql += s + " from " + table.getName() + " t1";
		}

		String where = " where 1=1 ";
		Condition cd = report.getCondition();
		if (null != cd) {
			// 处理模糊查询条件
			List<Like> likes = cd.getLikes();
			for (Like like : likes) {
				String conParam = req.getParameter(like.getColumn());
				if (conParam != null) {
					where += " and t1.\"" + like.getColumn() + "\" like '%"
							+ conParam + "%' ";
				}
			}

			// 处理下拉选择条件
			List<Equal> equals = cd.getEquals();
			for (Equal equal : equals) {
				String conParam = req.getParameter(equal.getColumn());
				if (conParam != null && !conParam.trim().equals("")) {
					where += " and t1.\"" + equal.getColumn() + "\"='"
							+ conParam + "' ";
				}
			}
			//处理权限
			String power=cd.getPower();
			if(power!=null&&power.equals("unit_org")){
				try {
					where+=PowerUtil.getUnitOrgPowerSql();
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
		sql += where;
		
		return sql;
	}
	private static  void closeAll(Connection conn,Statement stmt,ResultSet rs) {
		if (rs != null) {  
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
 
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}  
}
