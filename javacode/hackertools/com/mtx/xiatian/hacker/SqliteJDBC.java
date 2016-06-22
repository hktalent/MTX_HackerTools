package com.mtx.xiatian.hacker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.mtx.xiatian.db.IConst;
import com.mtx.xiatian.db.ISQLCallBack;
import com.mtx.xiatian.db.MySQLCallBack;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

/**
 * 内存数据库操作封装
 * @author xiatian
 */
public class SqliteJDBC extends InfoLog
{

	public static String path = "javacode/com/mtx/safegene/test/xiatian/db/";
	public SqliteJDBC()
	{
		init("sqlite");
	}
	
	/**
	 * 删除，并返回影响记录数
	 * @param szSQL
	 * @return
	 */
	public int delete(String szSQL)
	{
		return update(szSQL);
	}
	/**
	 * 更新，并返回影响记录数
	 * @param szSQL
	 * @return
	 */
	public int update(final String szSQL)
	{
		return execSql(new MySQLCallBack(){
            private static final long serialVersionUID = -529313623450935624L;
			public String getSql()
			{
				return szSQL;
			}
		});
	}

	
	/**
	 * 统一执行SQL
	 * 
	 * @param sqlCbk
	 */
	private int execSql(ISQLCallBack sqlCbk)
	{
//		info(sqlCbk.getSql());
		Connection conn = null;
		PreparedStatement prep = null;
		Statement stat = null;
		int[] aRst;
		int nRst = 0;
		String  szSql;
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

		}catch(SQLException e){
			e.printStackTrace();
			if(-1 < e.getMessage().indexOf("missing database "));
			else
			{
				info(sqlCbk.getSql());
				info(e);
			}
//			if(-1 < e.getMessage().indexOf("no such table"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			info(sqlCbk.getSql());
			info(e);
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
	 * @param jdbcUrl
	 * @param user
	 * @param pswd
	 */
	public void setConnInfo(String jdbcUrl, String user, String pswd)
	{
		szJdbcUrl = jdbcUrl;
		szUser = user;
		szPswd = pswd;
		String []a = {"oracle", "mysql", "sqlite"};
		for(String s:a)
		{
			if(-1 < jdbcUrl.indexOf("jdbc:" + s + ":"))
				init(s);
		}
	}
	private String szJdbcUrl = "jdbc:sqlite:" + path + "xtBigHackInfo.db", szUser = "", szPswd = "";
	private void init(String type)
	{
		try
		{
			String []a = {"oracle.jdbc.driver.OracleDriver", "com.mysql.jdbc.Driver", "org.sqlite.JDBC"};
			for(String s:a)
			{
				if(-1 < s.indexOf(type))
					Class.forName(s);
			}
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 建表
	 * 
	 * @param sql
	 * @return
	 */
	public int createTable(final String sql)
	{
		return execSql(new MySQLCallBack()
		{
			private static final long	serialVersionUID	= 9022004729294190002L;

			public String getSql()
			{
				return sql;
			}
		});
	}
	
	/**
	 * 根据表名查询
	 * @param szTableName
	 * @return
	 */
	public List<TreeMap<String, Object>> queryForList(String szTableName)
	{
		List <TreeMap<String, Object>>lst = new ArrayList<TreeMap<String, Object>>();
		query("select *  from " + szTableName, lst);
		return lst;
	}
	/**
	 * 查询
	 * 
	 * @param sql
	 * @param list
	 */
	public void query(final String sql, final List<TreeMap<String, Object>> list)
	{
		execSql(new ISQLCallBack()
		{
			private static final long	serialVersionUID	= 2254897909124184706L;

			public int doPreparedStatement(PreparedStatement p, Connection conn)
			{
				int nRst = 0;
				ResultSet rs = null;
				TreeMap<String, Object> m = null;
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

						Object oTmp = null; 
						while (rs.next())
						{
							m = new TreeMap<String, Object>();
							for (i = 0; i < nCol; i++)
							{
//								oTmp = typeHandlerFactory.getTypeHandler(arg0).getResult(rs, szACol[i]);
								oTmp = rs.getObject(szACol[i]);
								
								m.put(szACol[i], oTmp);
							}
							list.add(m);
						}
					}
				}catch(MySQLSyntaxErrorException e)
				{
					info(sql);
//					if(-1 == e.getMessage().indexOf("doesn't exist"))
						info(e);
				}
				catch (Throwable e)
				{
					info(sql);
					info(e);
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

	/**
	 * 插入单表
	 * 
	 * @param szTableName
	 * @param m
	 * @return
	 */
	public void insertStream(final String szTableName, final IGetOneMap one)
	{
		insert(szTableName, new ArrayList<TreeMap<String, Object>>(){

            public TreeMap<String, Object> get(int arg0)
            {
	            return one.getOneMap();
            }

            public int size()
            {
	            return Integer.MAX_VALUE;
            }
			
		});
	}
	
	/**
	 * 获取一行数据
	 * @param list
	 * @return
	 */
	public TreeMap<String, Object>  getOneData(List<TreeMap<String, Object>> list)
	{
		TreeMap<String, Object> one = null;
		synchronized(list)
		{
			while(0 == list.size())
		    {
	            try
	            {
	            	   list.wait();
	            } catch (InterruptedException e1)
	            {
	                e1.printStackTrace();
	            }
		    }
		}
			if(0 < list.size())
			{
				try{one = list.get(0);
				list.remove(0);
				}catch(Exception e){
					e.printStackTrace();
					}
			}
			synchronized(list)
			{
				list.notifyAll();
			}
		return one;
	}
	
//	public final TypeHandlerFactory typeHandlerFactory = new TypeHandlerFactory();
	/**
	 * 插入单表
	 * 
	 * @param szTableName
	 * @param m
	 * @return
	 */
	public int insert(final String szTableName, final List<TreeMap<String, Object>> list)
	{
		int nRst = 0;
//		if (null == list || 0 == list.size())
//			return nRst;
		nRst = execSql(new ISQLCallBack()
		{
			private static final long	serialVersionUID	= -7658614266198330240L;
			public boolean isInsert()
			{
				return true;
			}
			public int doPreparedStatement(PreparedStatement p, Connection conn)
			{
				TreeMap<String, Object> m = null;
				Iterator<String> it = null;
				int x = 0, nCnt = 1; // 500 批量提交
				int[] aRst;
				int nRst = 0;
				ResultSetMetaData rsm = null;
				try
                {
	                rsm = p.getMetaData();
                } catch (Exception e1)
                {
                	info(e1);
                }
				int i = 0;
				boolean bFirst = true;
				while(true)
				{
					if(bFirst)
					{
						m = m1;
						bFirst = false;
					}
					else
					{
						m = getOneData(list);
					}
					if(null == m)
					{
						break;
					}
					it = m1.keySet().iterator();
					x = 1;
					try
					{
						int iPCnt = 0;
						while (it.hasNext())
						{
							
//							if(null == rsm)
							try{p.setObject(x++, m.get(it.next()));
								++iPCnt;
								if(iPCnt >= nParmSize)break;}catch(Exception e){e.printStackTrace();break;}
//							else
//							{
//								oTmp = m.get(it.next());
//								typeHandlerFactory.getTypeHandler(oTmp.getClass()).setParameter(p, x++, oTmp, null);
//							}
						}
						// 补充缺的参数
						iPCnt = nParmSize - iPCnt - 1;
						while(0 < iPCnt)
						{
							try{p.setObject(x++, "");}catch(Exception e){e.printStackTrace();break;}
						}
						p.addBatch();
						if (nCnt < i && 0 == i % nCnt)
						{
							aRst = p.executeBatch();
							conn.commit();
							for (int y : aRst)
							{
								nRst += y;
							}
//							info("提交数据了: ",   nRst + "/"+ i);
						}
					} catch (Throwable e)
					{
						if(null != e && -1 == e.getMessage().indexOf("Duplicate entry"))
							e.printStackTrace();
						try
                        {
	                        conn.rollback();
                        } catch (SQLException e1)
                        {
	                        e1.printStackTrace();
                        }
//						break;
					}
					i++;
				}
				try{
						aRst = p.executeBatch();
						conn.commit();
						for (int y : aRst)
						{
							nRst += y;
						}
//						info("提交数据了: ",   nRst + "/"+ i);
				} catch (Throwable e)
				{
					e.printStackTrace();
					try
                    {
                        conn.rollback();
                    } catch (SQLException e1)
                    {
                        e1.printStackTrace();
                    }
				}
				return nRst;
			}

			private TreeMap<String, Object> m1 = null;
			int nParmSize = 0;
			public String getSql()
			{
				StringBuffer create = new StringBuffer("create table IF NOT EXISTS  ");
				create.append(szTableName).append("(");

				StringBuffer buf = new StringBuffer("insert into "), names = new StringBuffer("("), vals  = new StringBuffer();
				buf.append(szTableName);
				vals.append(" values (");
				
				m1 = getOneData(list);
				if(null == m1)return null;
				nParmSize = m1.size();
				String sN = "";
				Iterator<String> it = m1.keySet().iterator();
				int nSize = 2000;
				if(-1 < szJdbcUrl.indexOf("mysql"))
					nSize = 60000 / 3 / (0 == m1.size() ? 1 : m1.size());
				for (int i = 0, j = m1.size(); it.hasNext() && i < j; i++)
				{
					vals.append("?");
					sN = it.next();
					names.append(sN);
					create.append(sN).append(" varchar(" + nSize + ")");
					if (i != j - 1)
					{
						vals.append(",");
						names.append(",");
						create.append(",");
					}
				}
				names.append(") ");
				buf.append(names.toString()).append(vals.toString());
				buf.append(");");
				create.append(");");
				createTable(create.toString());
				return buf.toString();
			}
		});
		return nRst;
	}


	/**
	 * IP、"Nmap scan report for "
	 * 端口、协议类类型(tcp/udp)、服务、版本： “PORT      STATE SERVICE     VERSION”
	 * 设备类型：Device: switch 有则是交换机，否则为PC
	 * 操作系统：OS:\\s*([^;]*));   或者OS:\\s*([^\\n]*)
	 * Host script results: 
	 * 主机名：Computer name:\\s*([^\\n]*)   NetBIOS computer name: \\s*([^\\n]*)
	 * Mac: NetBIOS MAC:\\s*([^\\s\\(\\n]*)   MAC Address:\\s*([^\\s\\(\\n]*)
	 * 相同的ip不代表mac相同
	 */
	public  void doGetAllInfo(String szFileName)
	{
		String s = IConst.readFile(szFileName);
		if(null == s || 0 == (s = s.trim()).length())
		{
			IConst.debug("没有获取到文件数据");
			return;
		}
		
		s = s.replaceAll("(\\n|\\r| |\\t)(\\| Key|SF:|\\| Not valid |Status:|Host is up |Service detection performed)[^\\n\\r]+(\\n|\\r)", "\n");
		s = s.replaceAll("(\\n|\\r)(All|Nmap done:|Connect Scan Timing: About)\\s*\\d*[^\\n]+\\n", "\n");
		s = s.replaceAll("(\\s*SF:).*?(\\n|\\r)", "\n");
		s = s.replaceAll("Not shown: \\d+ closed ports\\s*", "");
		s = s.replaceAll("(\\n|\\r)[\\|_\\s\\d\\.]+?[^a-z]*(\\r|\\n)", "\n");
		final SqliteJDBC sj = this;
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>(){
            private static final long serialVersionUID = -7751181274295061596L;
			public TreeMap <String,Object> get(int i)
			{
				TreeMap <String,Object> m1 = super.get(i);
				// 插入前先查询 ip='" + m1.get("ip") + "' and
				List<TreeMap<String, Object>> lst01 = sj.queryForList("server where  mac='" + m1.get("mac") + "'");
				if(null != lst01 && 0 < lst01.size())return null;
				if(null == m1.get("lastScan"))
					m1.put("lastScan", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				return m1;
			}
		};
		String []a = s.split("Nmap scan report for ");
		TreeMap<String, Object> m = null;
		
		TreeMap<String, TreeMap<String, Object>> m1Mac = new TreeMap<String, TreeMap<String, Object>>();
		for(String x: a)
		{
			m = new TreeMap<String, Object>();
			s = getPatternStr("(\\d*\\.\\d*\\.\\d*\\.\\d*)", x);
			if(0 == s.length())continue;
			m.put("ip", s);
			// NetBIOS MAC:\\s*([^\\s\\(\\n]*)   MAC Address:\\s*([^\\s\\(\\n]*)
			s = getPatternStr("\\bMAC\\s*[^:]*:.*?([0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F])", x);
			m.put("mac", s);
			if(0 < s.length())
			{
				if(m1Mac.containsKey(s))
					m = m1Mac.get(s);
			}
//			System.out.println("IP: " + m.get("ip") + " MAC: " + s);
			
			s = getPatternStr("Device:\\s*([^\\n]+)\n", x);
			m.put("device", s);
			// OS:\\s*([^;]*));   或者OS:\\s*([^\\n]*)
			s = getPatternStr("OS:\\s*([^;]*);", x);
			if(0 == s.length())
				s = getPatternStr("OS:\\s*([^\\n]*)\n", x);
			m.put("os", s);
			
			// Computer name:\\s*([^\\n]*)   NetBIOS computer name: \\s*([^\\n]*)
			s = getPatternStr("computer name:\\s*([^;\\n]*)\n", x);
			m.put("cptnm", s);
			
			m.put("allinfo", x.trim());
			
			// 通过mac判断是否重复
			if(0 == String.valueOf(m.get("mac")).length() || !m1Mac.containsKey(m.get("mac")))
				list.add(m);
			else 
				System.out.println("重复：" + m.get("mac"));
			
			if(0 < String.valueOf(m.get("mac")).length())
				m1Mac.put(String.valueOf(m.get("mac")), m);
		}
		System.out.println(list.size());
	}
	
	public static void main(String[] args)
	{
		String szFileName = "/Users/xiatian/all10A.txt";
		new SqliteJDBC().doGetAllInfo(szFileName);
		
//		String s = IConst.readFile(szFileName);
//		s = s.replaceAll("(\\n|\\r| |\\t)(\\| Key|SF:|\\| Not valid |Status:|Host is up |Service detection performed)[^\\n\\r]+(\\n|\\r)", "\n");
//		s = s.replaceAll("(\\n|\\r)(All|Nmap done:|Connect Scan Timing: About)\\s*\\d*[^\\n]+\\n", "\n");
//		s = s.replaceAll("(\\s*SF:).*?(\\n|\\r)", "\n");
//		s = s.replaceAll("Not shown: \\d+ closed ports\\s*", "");
//		s = s.replaceAll("(\\n|\\r)[\\|_\\s\\d\\.]+?[^a-z]*(\\r|\\n)", "\n");
//		System.out.println(s);
	}
	public static void main1(String[] args)
	{
		
		final SqliteJDBC sj = new SqliteJDBC();
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>(){
			public TreeMap <String,Object> get(int i)
			{
				TreeMap <String,Object> m1 = super.get(i);
				List<TreeMap<String, Object>> lst01 = sj.queryForList("server where ip='" + m1.get("ip") + "' and mac='" + m1.get("mac") + "'");
				if(null != lst01 && 0 < lst01.size())return null;
				if(null == m1.get("lastScan"))
					m1.put("lastScan", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				return m1;
			}
		};
		
		TreeMap<String, Object> m = new TreeMap<String, Object>();
		m.put("mac", "00:00:00:00:00");
		m.put("ip", "127.0.0.1");
		m.put("biaozhu", "");
		m.put("sysinfo","");
		m.put("otherInfo", "");
		list.add(m);
		
		// 获取mac等信息
		int i = 8;
//		for(i = 1; i < 255; i++)
//			MyNMapTest.getMacs("192.168." + i + ".0/24", list);
		
		int n = sj.insert("server", list);
		System.out.println("[" + n + "]");

		// delete  from server where ip='ip'
		sj.query("select *  from server", new ArrayList<TreeMap<String, Object>>()
		{
			private static final long	serialVersionUID	= -7061840719725188173L;

			public boolean add(TreeMap<String, Object> m)
			{
				System.out.println(m);
				return true;
			}

		});
	}
}
