package com.mtx.xiatian.expimp;

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * <pre>
 * 数据导出
 * 1、第一行导出列名称、类型
 * 2、以后的行只导出数据，节约空间
 * </pre>
 * 
 * @author xiatian
 * 
 */
public class ExpDB
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new ExpDB(
				"jdbc:oracle:thin:@129.1.20.104:1521:swwzdb", "sw_xcms", "sw_xcms").query(
		        "select table_name,num_rows from user_tables where  order by num_rows desc", new ArrayList<TreeMap<String, String>>()
		        {
			        public boolean add(TreeMap<String, String> m)
			        {
				        try
				        {
					        // myout.print(m.toString());
				        } catch (Throwable e)
				        {
					        e.printStackTrace();
				        }
				        return true;
			        }
		        });
	}

	// "jdbc:oracle:thin:@10.160.1.28:1521:wssbdb", "scwssb", "scwssb!xxzx1407"
	private String	path	= ".";
	public ExpDB(String jdbcUrl, String user, String pswd)
	{
		setConnInfo(jdbcUrl, user, pswd);
	}

	/**
	 * 执行sql语句
	 * 
	 * @param sqlCbk
	 * @return
	 */
	private int execSql(ISQLCallBack sqlCbk)
	{
		Connection conn = null;
		PreparedStatement prep = null;
		Statement stat = null;
		int[] aRst;
		int nRst = 0;
		String szSql;
		try
		{
			szSql = sqlCbk.getSql();
			if (null == szSql || 0 == (szSql = szSql.trim()).length())
				return 0;
			conn = DriverManager.getConnection(szJdbcUrl, szUser, szPswd);
			conn.setAutoCommit(false);
			if (sqlCbk.isInsert())
			{
				prep = conn.prepareStatement(szSql);
				nRst = sqlCbk.doPreparedStatement(prep, conn);
				aRst = prep.executeBatch();
				for (int x : aRst)
					nRst += x;
			} else
			{
				stat = conn.createStatement();
				nRst = stat.executeUpdate(szSql);
			}

		} catch (SQLException e)
		{
		} catch (Throwable e)
		{
			try
			{
				if (null != conn)
					conn.rollback();
			} catch (Throwable e1)
			{
				e1.printStackTrace();
			}
		} finally
		{
			if (null != conn)
			{
				try
				{
					conn.commit();
				} catch (Throwable e)
				{
				}
			}
			if (null != stat)
			{
				try
				{
					stat.close();
				} catch (Throwable e)
				{
				}
			}
			if (null != prep)
			{
				try
				{
					prep.close();
				} catch (Throwable e)
				{
				}
			}
			if (null != conn)
			{
				try
				{
					conn.close();
				} catch (Throwable e)
				{
				}
			}
		}
		return nRst;
	}

	/**
	 * 设置连接信息，默认是sqlite类型数据库
	 * 
	 * @param jdbcUrl
	 * @param user
	 * @param pswd
	 */
	public void setConnInfo(String jdbcUrl, String user, String pswd)
	{
		szJdbcUrl = jdbcUrl;
		szUser = user;
		szPswd = pswd;
		String[] a =
		{ "oracle", "mysql", "sqlite" };
		for (String s : a)
		{
			if (-1 < jdbcUrl.indexOf("jdbc:" + s + ":"))
				init(s);
		}
	}

	private String	szJdbcUrl	= "jdbc:sqlite:" + path + "mtBigHackInfo.db", szUser = "", szPswd = "";

	private void init(String type)
	{
		try
		{
			String[] a =
			{ "oracle.jdbc.driver.OracleDriver", "com.mysql.jdbc.Driver", "org.sqlite.JDBC" };
			for (String s : a)
			{
				if (-1 < s.indexOf(type))
					Class.forName(s);
			}
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 查询
	 * 
	 * @param sql
	 * @param list
	 */
	public void query(final String sql, final List<TreeMap<String, String>> list)
	{
		execSql(new ISQLCallBack()
		{
			private static final long	serialVersionUID	= 2254897909124184706L;

			public int doPreparedStatement(PreparedStatement p, Connection conn)
			{
				int nRst = 0;
				ResultSet rs = null;
				TreeMap<String, String> m = null;
				ResultSetMetaData rsmd = null;
				try
				{
					rs = p.executeQuery();
					if (null != rs)
					{
						// 获得列信息
						rsmd = rs.getMetaData();
						int nCol = rsmd.getColumnCount();
						String[] szACol = new String[nCol];
						int i = 0, x = 1;
						for (; i < nCol; i++, x++)
						{
							szACol[i] = rsmd.getColumnName(x);
						}

						while (rs.next())
						{
							m = new TreeMap<String, String>();
							for (i = 0; i < nCol; i++)
							{
								m.put(szACol[i], rs.getString(szACol[i]));
							}
							list.add(m);
						}
					}
				} catch (Exception e)
				{
				} catch (Throwable e)
				{
				} finally
				{
					if (null != rs)
						try
						{
							rs.close();
						} catch (Throwable e)
						{
							e.printStackTrace();
						}
				}
				return nRst;
			}

			public boolean isInsert()
			{
				return true;
			}

			public String getSql()
			{
				return sql;
			}
		});
	}

	public interface ISQLCallBack extends Serializable
	{
		public int doPreparedStatement(PreparedStatement p, Connection conn);

		public String getSql();

		public boolean isInsert();
	}
}
