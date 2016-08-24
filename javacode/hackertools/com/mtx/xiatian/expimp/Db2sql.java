package com.mtx.xiatian.expimp;

import java.io.File;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import com.mtx.xiatian.evolvedata.MyThreadPoolExecutor;
import com.mtx.xiatian.hacker.InfoLog;

/**
 * 将数据导出为建库脚本
 * csisca.AC01 记录数：55474890
 * @author xiatian
 */
public class Db2sql
{

	public static boolean useText = false;
	
	public static String path = "./data/";// "/Volumes/dbdata/sgkzl/MTX私有/QQ/";

	/**
	 * 去除后面的数字
	 * @param s
	 * @return
	 */
	public static String noNum(String s)
	{
		return s.replaceAll("\\d*$", "");
	}
	
//	static boolean bStart = false;
	public static String dumpDB(Properties props)
	{
		String driverClassName = props.getProperty("driver.class");
		String driverURL = props.getProperty("driver.url");
		String catalogName = props.getProperty("user");
		String outCatalogName = null;
		// 启用高速，则数据写入固态硬盘，完事后移动到移动硬盘上
		boolean bUseGs = false;
		File f1 = new File(outCatalogName = path + catalogName + ".sql");
		if (f1.exists()) {
			f1.delete();
			// return "";
			// f1.renameTo(new File(path + catalogName +
			// "_" + System.nanoTime() + ".sql"));
		}
		outCatalogName = path + catalogName + ".sql";
		OutputStream bos = null;
		if (bUseGs) {
			outCatalogName = "data/" + catalogName + ".sql";
			f1 = new File(outCatalogName);
		}
		// Default to not having a quote character
		String columnNameQuote = props.getProperty("columnName.quoteChar", "");
		DatabaseMetaData dbMetaData = null;
		Connection dbConn = null;
		try {
			Class.forName(driverClassName);
			props.setProperty("remarksReporting","true");  
			dbConn = DriverManager.getConnection(driverURL, props);
			dbMetaData = dbConn.getMetaData();
		} catch (Exception e) {
			InfoLog.info("无法连接：", driverURL, " ", catalogName);
			System.err.println("Unable to connect to database: " + e);
			return null;
		}
		ResultSet rs = null;
		ResultSet primaryKeys = null;
		ResultSet tableMetaData = null;
		StringBuffer result = new StringBuffer();
		StringBuffer resultTb = new StringBuffer();
		try {
			//获取查询导出数据库对象的条件
			String catalog = null;
			String schema = null;
			String tables = null;
			String types[] = null;
			if(props.getProperty("catalog") != ""){
				catalog = props.getProperty("catalog");
			}
			if(props.getProperty("schemaPattern") != ""){
				schema = props.getProperty("schemaPattern");
			}
			if(dbConn.getClass().getName().toUpperCase().indexOf("SQLSERVER") > -1){
				schema = "dbo";
			}
			if(props.getProperty("tableName") != ""){
				tables = props.getProperty("tableName");
			}
			if(props.getProperty("type") != "" && null != props.getProperty("type") ){
				types = props.getProperty("type").split(",");
			}
			//查询需要导出时间的数据库对象
			rs = dbMetaData.getTables(catalog, schema, tables, types);
			if (!rs.next()) {
				System.err.println("根据参数: catalog=" + catalog + " schema=" + schema + " tables="
								+ tables + "没有找到任何符合要求的对象");
				rs.close();
			} else {
				outCatalogName = path + catalogName + ".sql";
				f1 = new File(outCatalogName);
				if (f1.exists())
					return "";
				InfoLog.info("开始处理", outCatalogName);
				StringBuffer exptables = new StringBuffer("");
				do {
					String tableName = rs.getString("TABLE_NAME");//当前查询出的table名称
					String tableDesc = rs.getString("REMARKS");//当前查询出的table注释
					
					String tablesname = "";
					if(null != props.getProperty("tablesname"))
						tablesname = props.getProperty("tablesname").toUpperCase();//获取需要导出数据的对象名称
					if(0 < tablesname.length())
					if (tablesname.indexOf(tableName.toUpperCase()) < 0 || exptables.indexOf(tableName) > -1) {
						continue;
					}
//					if("nvt_cves".equalsIgnoreCase(tableName))
//					{
//						bStart = true;
//						continue;
//					}
//					if(!bStart)continue;
					
					String tableType = rs.getString("TABLE_TYPE");
					if ("TABLE".equalsIgnoreCase(tableType)) {
						
						{
							//如果是oracle
							if(dbConn.getClass().getName().toUpperCase().indexOf("ORACLE") > -1){
								result.append("\ndeclare \n num number; \nbegin "
										+ "\n SELECT count(1) INTO num from ALL_TABLES WHERE TABLE_NAME = '" + tableName.toUpperCase() + "' and "
										+ "OWNER = '" + catalogName.toUpperCase() + "';\n  IF num = 1 THEN \n "
										+ "  EXECUTE IMMEDIATE 'DROP TABLE " + tableName.toUpperCase() + "';\n END IF;\n"
										+ "END;\n/");
							}else if(dbConn.getClass().getName().toUpperCase().indexOf("SQLSERVER") > -1){//如果是sqlserver
								result.append("\nIF OBJECT_ID('" + tableName.toUpperCase() + "') IS NOT NULL DROP TABLE "+ tableName.toUpperCase() + "\nGO");
							}else if(dbConn.getClass().getName().toUpperCase().indexOf("MYSQL") > -1){
								result.append("\nDROP TABLE IF EXISTS "+tableName+";");
							}
							result.append("\n-- " + tableName);
							result.append("\nCREATE TABLE " + tableName+ " (\n");
						}
						try {
							// java.sql.SQLException: ORA-01424: 转义符之后字符缺失或非法
							// http://www.iteye.com/problems/55630
							if (tableName.contains("/")
									|| tableName.contains("$")
									|| tableName.contains("=")
									|| tableName.contains("BIN")) {
								continue;
								// tableName = "\"" + tableName + "\"";
							}
							tableMetaData = dbMetaData.getColumns(null, null,
									tableName, "%");
							boolean firstLine = true;
							StringBuffer columnNmaes = new StringBuffer("");
							while (tableMetaData.next()) {
								String columnName = tableMetaData.getString("COLUMN_NAME");
								String columnType = tableMetaData.getString("TYPE_NAME");
								String columnDesc = tableMetaData.getString("REMARKS");
								if(columnNmaes.indexOf(columnName) > -1 )
									continue;

								if (firstLine) {
									firstLine = false;
								} else {
									result.append(",\n");
								}
								// WARNING: this may give daft answers for some
								// types on some databases (eg JDBC-ODBC link)
								int columnSize = tableMetaData.getInt("COLUMN_SIZE");
								String nullable = tableMetaData.getString("IS_NULLABLE");
								String nullString = "";
								if ("NO".equalsIgnoreCase(nullable)) {
									nullString = "NOT NULL";
								}
								if(columnType.equals("DATE") || columnType.equals("NUMBER") ){
									result.append("    " + columnNameQuote
											+ columnName + columnNameQuote + " "
											+ columnType);
								}else{
									result.append("    " + columnNameQuote
											+ columnName + columnNameQuote + " "
											+ columnType + " (" + columnSize + ")"
											+ " " + nullString);
								}
								if(dbConn.getClass().getName().toUpperCase().indexOf("MYSQL") > -1 && columnDesc != null && columnDesc != ""){
									result.append(" COMMENT'"+columnDesc + "'");
								}
								columnNmaes.append(columnName);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								if (null != tableMetaData)
									tableMetaData.close();
								tableMetaData = null;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						// Now we need to put the primary key constraint
						try {
							primaryKeys = dbMetaData.getPrimaryKeys(catalog,schema, tableName);
							// What we might get:
							// TABLE_CAT String => table catalog (may be null)
							// TABLE_SCHEM String => table schema (may be null)
							// TABLE_NAME String => table name
							// COLUMN_NAME String => column name
							// KEY_SEQ short => sequence number within primary
							// key
							// PK_NAME String => primary key name (may be null)
							String primaryKeyName = null;
							StringBuffer primaryKeyColumns = new StringBuffer();
							while (primaryKeys.next()) {
								String thisKeyName = primaryKeys.getString("PK_NAME");
								if ((thisKeyName != null && primaryKeyName == null)
										|| (thisKeyName == null && primaryKeyName != null)
										|| (thisKeyName != null && !thisKeyName
												.equals(primaryKeyName))
										|| (primaryKeyName != null && !primaryKeyName
												.equals(thisKeyName))) {
									// the keynames aren't the same, so output
									// all that we have so far (if anything)
									// and start a new primary key entry
									if (primaryKeyColumns.length() > 0) {
										// There's something to output
										result.append(",\n    PRIMARY KEY ");
										if (primaryKeyName != null) {
											result.append(primaryKeyName);
										}
										result.append("("
												+ primaryKeyColumns.toString()
												+ ")");
									}
									// Start again with the new name
									primaryKeyColumns = new StringBuffer();
									primaryKeyName = thisKeyName;
								}
								// Now append the column
								if (primaryKeyColumns.length() > 0) {
									primaryKeyColumns.append(", ");
								}
								primaryKeyColumns.append(primaryKeys
										.getString("COLUMN_NAME"));
							}
							if (primaryKeyColumns.length() > 0) {
								// There's something to output
								result.append(",\n    PRIMARY KEY ");
//								if (primaryKeyName != null) {
//									result.append(primaryKeyName);
//								}
								result.append("("
										+ primaryKeyColumns.toString() + ")");
							}
						} catch (SQLException e) {
							// NB you will get this exception with the JDBC-ODBC
							// link because it says
							// [Microsoft][ODBC Driver Manager] Driver does not
							// support this function
							System.err
									.println("Unable to get primary keys for table "
											+ tableName + " because " + e);
						} finally {
							try {
								if (null != primaryKeys)
									primaryKeys.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						result.append("\n);\n");
						//如果是oracle，则加上oracle建表注释
						if(dbConn.getClass().getName().toUpperCase().indexOf("ORACLE") > -1){
							//表注释
							result.append("comment on table " + tableName + "\nis '" + tableDesc + "';\n");
							//字段注释
							try {
								if (tableName.contains("/") || tableName.contains("$") || tableName.contains("=") || tableName.contains("BIN")) {
									continue;
									// tableName = "\"" + tableName + "\"";
								}
								tableMetaData = dbMetaData.getColumns(null, null,tableName, "%");
								boolean firstLine = true;
								StringBuffer columnNmaes = new StringBuffer("");
								while (tableMetaData.next()) {
									String columnName = tableMetaData.getString("COLUMN_NAME");
									String columndesc = tableMetaData.getString("REMARKS");
									if(columnNmaes.indexOf(columnName) > -1 )
										continue;
									
									if (firstLine) {
										firstLine = false;
									} else {
										result.append(";\n");
									}
									if(columndesc != "" && columndesc != null)
										result.append("comment on column " + tableName+ "." + columnName + "\nis '" + columndesc + "'");
									else
										result.append("comment on column " + tableName+ "." + columnName + "\nis null");
									columnNmaes.append(columnName);
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								try {
									if (null != tableMetaData)
										tableMetaData.close();
									tableMetaData = null;
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							result.append(";\n");
						}else if(dbConn.getClass().getName().toUpperCase().indexOf("SQLSERVER") > -1){
							//如果是sqlserver，加sqlserver的注释
							result.append("GO\n");
							String descfirst = "exec sp_addextendedproperty N'MS_Description', N'";
							String descsecond = "', N'user', N'dbo', N'table', N'";
							String descthird = "', N'column',N'";
							//字段注释
							try {
								if (tableName.contains("/") || tableName.contains("$") || tableName.contains("=") || tableName.contains("BIN")) {
									continue;
								}
								tableMetaData = dbMetaData.getColumns(null, null,tableName, "%");
								boolean firstLine = true;
								StringBuffer columnNmaes = new StringBuffer("");
								while (tableMetaData.next()) {
									String columnName = tableMetaData.getString("COLUMN_NAME");
									if(columnNmaes.indexOf(columnName) > -1 )
										continue;
									
									if (firstLine) {
										firstLine = false;
									} else {
										result.append("\n");
									}
									PreparedStatement stmt1 = null;
									ResultSet rsDesc = null;
									//查询当前表的当前列的说明
									stmt1 = dbConn.prepareStatement("SELECT cast(p.value as NCHAR(100)) FROM sys.extended_properties p,"
											+ "sys.columns c where p.major_id=OBJECT_ID('" + tableName.toUpperCase() + "') and c.name= '"
											+ columnName.toUpperCase() + "' and p.major_id=c.object_id and p.minor_id=c.column_id");

									rsDesc = stmt1.executeQuery();
									if(rsDesc.next()){
										String columndesc = rsDesc.getString(1);
										if(columndesc != "" && columndesc != null)
											result.append(descfirst + columndesc + descsecond + tableName + descthird + columnName + "'");
										columnNmaes.append(columnName);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								try {
									if (null != tableMetaData)
										tableMetaData.close();
									tableMetaData = null;
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							result.append("GO\n");
						}
						// Right, we have a table, so we can go and dump it,
						// 如果文件为空，则先创建文件
						if (null == bos) {
							bos = InfoLog.writeFileForBuf(outCatalogName, "",bos);
						}
						// 没有数据的表结构就不写了
						dumpTable(catalogName, dbConn, resultTb, tableName, outCatalogName, result, bos);
						bos = InfoLog.writeFileForBuf(outCatalogName, resultTb.toString(), bos);
						resultTb.delete(0, resultTb.length());
						result.delete(0, result.length());
						
						//记录已经导出过的sql
						exptables.append(tableName);
					}
				} while (rs.next());
			}
			return result.toString();
		} catch (SQLException e) {
			// To change body of catch statement use
			// Options | File Templates.
			e.printStackTrace();
		} finally {
			try {
				bos = InfoLog.writeFileForBuf(outCatalogName,
						resultTb.toString(), bos);
				if (null != bos) {
					try {
						bos.flush();
						bos.close();
					} catch (Exception e) {
					}
				}
				resultTb.delete(0, resultTb.length());
				if (null != rs)
					rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (null != dbConn)
					dbConn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// if(bUseGs)
			// f1.renameTo(new File(path + catalogName +
			// "_Ok.sql"));
			InfoLog.info(catalogName, ".", catalogName, "处理完毕");
		}
		return null;

	}

	/** dump this particular table to the string buffer */
	private static void dumpTable(String dbName,Connection dbConn, StringBuffer result, String tableName, String outCatalogName, StringBuffer resultTb, OutputStream bos)
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;
		long lnCnt = 0;
		try {
			bos.flush();
			// First we output the create table stuff
			if(dbConn.getClass().getName().toUpperCase().indexOf("SQLSERVER") > -1){
				stmt = dbConn.prepareStatement("SELECT * FROM " + tableName);
			}else{
				stmt = dbConn.prepareStatement("SELECT * FROM " + tableName
						// TYPE_SCROLL_INSENSITIVE sqllit不支持
						// ,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE
						);
			}
			rs = stmt.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			
			// Now we can output the actual data
			result.append("\n\n-- Data for " + tableName + "\n");
			
			//获取记录条数
			stmt = dbConn.prepareStatement("SELECT count(*) FROM " + tableName);
			ResultSet rs1 =  stmt.executeQuery();
			rs1.next();
			int rowCount = rs1.getInt(1);
			// 0条
			if(0 == rowCount)result.delete(0, result.length());
			while (rs.next()) 
			{
				// 有数据就加上表头信息
				if (0L == lnCnt && 0 < resultTb.length()) 
				{
					result.append(resultTb.toString());
					resultTb.delete(0, resultTb.length());
				}
				result.append("INSERT INTO " + tableName + " VALUES (");
				for (int i = 0; i < columnCount; i++) {
					bos = InfoLog.writeFileForBuf(outCatalogName, result.toString(), bos);
					result.delete(0, result.length());
					if (i > 0) {
						result.append(", ");
					}
					Object value = rs.getObject(i + 1);
					if (value == null) {
						result.append("NULL");
					} else {
						ResultSet tableMetaData = null;
						String columnNmae =null;
						try{
							DatabaseMetaData dbMetaData = dbConn.getMetaData();
							columnNmae = metaData.getColumnName(i+1);
							tableMetaData = dbMetaData.getColumns(null, null,tableName, columnNmae);
							tableMetaData.next();
							String columntype = tableMetaData.getString("TYPE_NAME");
							//判断如果字段为oracle的date类型，则对value加上to_date处理
							if(columntype.equals("DATE") && dbConn.getClass().getName().toUpperCase().indexOf("ORACLE") > -1){
								String outputValue = value.toString();
								outputValue = outputValue.replaceAll("'", "\\'");
								if(outputValue.length() == 21){
									outputValue = outputValue.substring(0, outputValue.length()-2);
								}
								result.append("to_date('" + outputValue + "','YYYY-MM-DD HH24:MI:SS')");
							}else{
								String outputValue = value.toString();
								outputValue = outputValue.replaceAll("'", "\\'");
								result.append("'" + outputValue + "'");
							}
							bos = InfoLog.writeFileForBuf(outCatalogName, result.toString(), bos);
							result.delete(0, result.length());
						}catch (Throwable e) {
							System.out.println("tableMetaData = dbMetaData.getColumns(null, null,tableName, columnNmae);");
							System.out.println(tableName + ": " +columnNmae);
							e.printStackTrace();
						} finally {
							try {
								if (null != tableMetaData)
									tableMetaData.close();
								tableMetaData = null;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				result.append(");\n");
				lnCnt++;
				//每一千条执行提交
				if(dbConn.getClass().getName().toUpperCase().indexOf("SQLSERVER") < 0){
					if(lnCnt % 1000 == 0 && 0 < lnCnt){
						result.append("commit;\n");
					}else if(lnCnt == rowCount){//剩下的不满一千条的，再进行提交
						result.append("commit;\n");
					}
				}
				bos = InfoLog.writeFileForBuf(outCatalogName, result.toString(), bos);
				result.delete(0, result.length());
				if (0 == lnCnt % 10000 && 0 < lnCnt)
					InfoLog.info(dbName, ".", tableName, ": ", lnCnt / 10000, "万");
			}
		} catch (Exception e) {
			 System.err.println("导出表 " + tableName + " 失败，原因: " + e);
		} finally {
			try {
				if (null != rs)
					rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (null != stmt)
					stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (0 < lnCnt)
				InfoLog.info(dbName, ".", tableName, " 记录数：", lnCnt);
		}
	}

	public static void main(String[] args)
	{
		System.setProperty("sun.zip.encoding", System.getProperty("sun.jnu.encoding"));
		
		if(true)
		{
//			 main1("oracle.jdbc.driver.OracleDriver",
//					 "jdbc:oracle:thin:@192.168.24.18:1521:orcl", "yhomsmp", "yhomsmp");
//			 
//			 main1("org.postgresql.Driver",
//					 "jdbc:postgresql://127.0.0.1:5433/msf", "msf",
//                     "miracle***");
			 
			 main1("org.sqlite.JDBC",
					 "jdbc:sqlite:/Volumes/dbdata/sgkzl/MTX私有/收集的db/openvas/tasks.db", "tasks", "");
//			int i = 1;
//			for( i = 1; i < 12; i++)
//			{
//				main1("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://10.211.55.6:1433; DatabaseName=GroupData" + i, "sa", "Miracle+_)=-0");
//			}
//			for( i = 1; i < 12; i++)
//			{
//				main1("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://10.211.55.6:1433; DatabaseName=QunInfo" + i, "sa", "Miracle+_)=-0");
//			}
			
//			main1("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://10.211.55.6:1433; DatabaseName=new", "sa", "Miracle+_)=-0");
			return;
		}
		
		// jdbc\:oracle\:thin\:@192.168.10.220\:1521\:yhdb
		// main1("oracle.jdbc.driver.OracleDriver",
		// "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "rswt", "rswt");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.26.21:1521:basedb", "yn01700_P", "yn01700_P");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.26.21:1521:testdb", "yn01700_P", "yn01700_P");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "tyqw", "tyqw2014");
		/*
test21 =
  (DESCRIPTION =
    (ADDRESS = (PROTOCOL = TCP)(HOST = 192.168.26.21)(PORT = 1521))
    (CONNECT_DATA =
      (SERVER = DEDICATED)
      (SERVICE_NAME = basedb)
  )
 )
yn01700_p/yn01700_p*/
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_p", "yn01700_p");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "csisca", "csisca");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY15GC99900_1", "jy15gc99900");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY14GD02100_01", "JY14GD02100_01");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY14GD02100_03", "JY14GD02100_03");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY14GD02100_02", "JY14GD02100_02");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "JY12CD008_tkbx", "JY12CD008");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.60:1521:yhdb", "yhjypt", "yhjypt");
//
//		
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.106:1521:orcl", "scjm2", "scjm2");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "yhjy", "yhjy");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "jysys", "jysys");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.220:1521:yhdb", "jysite", "jysite");
//		
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.190:1521:orcl", "yhportal", "yhportal");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.207:1521:orcl", "ta3test", "ta3test");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.184:1521:testdb", "ta3", "ta3");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_frame", "yn01700_frame");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_com", "yn01700_com");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_i", "yn01700_i");
////		
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.213:1521:testdb", "yn01700_param", "yn01700_param");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "tywscx", "tywscx");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "tywscx", "tywscx2014");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "gzjkk", "gzjkk");
//		main1("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.10.153:1521:testdb", "gzcms", "gzcms");
	}

	/** Main method takes arguments for connection to JDBC etc. */
	public static void main1(final String... args)
	{
		MyThreadPoolExecutor.getInstance().addRunnable(new Runnable()
		{
			public void run()
			{
				try{
					Thread.sleep(333);
					Properties props = new Properties();
					try
					{
						// props.load(new FileInputStream(args[0]));
						if (4 <= args.length)
						{
							int i = 0;
							props.put("driver.class", args[i++]);
							props.put("driver.url", args[i++]);
							props.put("user", args[i++]);
							props.put("password", args[i++]);
						}
						// props.put("driver.class","com.mysql.jdbc.Driver");
						// props.put("driver.url",
						// "jdbc:mysql://127.0.0.1:3306/mydb?zeroDateTimeBehavior=convertToNull&relaxAutoCommit=true&useUnicode=true&characterEncoding=utf8");
						// props.put("user","root");
						// props.put("password","root");
						// System.out.println(
						if(0 < props.size())
							dumpDB(props);
						props.clear();
					} catch (Exception e)
					{
						System.err.println("Unable to open property file: " + args[0] + " exception: " + e);
						e.printStackTrace();
					}
				 }catch(Throwable e)
				 {e.printStackTrace();}
			}
		});
		

	}
}