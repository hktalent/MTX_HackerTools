package com.mtx.xiatian.expimp;

import java.sql.Connection;
import java.sql.DriverManager;

import com.gene.common.jdbc.ScriptRunner;
import com.gene.common.resources.Resources;

/**
 * 导入、批量运行sql测试
 * @author xiatian
 */
public class MyTest
{

    public static void main(String []arg)
    {
        String driverClassName = "oracle.jdbc.driver.OracleDriver";
        String driverURL = "jdbc:oracle:thin:@192.168.10.213:1521:testdb";
        String catalogName = "yn01700_p";
        Connection dbConn = null;
        try {
            Class.forName(driverClassName);
            dbConn = DriverManager.getConnection(driverURL, catalogName, catalogName);
            ScriptRunner runner = new ScriptRunner(dbConn, true, true);
            runner.setErrorLogWriter(null);
            runner.setLogWriter(null);
            runner.runScript(Resources.getResourceAsReader("com/mtx/safegene/test/xiatian/expimp/jy14mc002cf_yhomsmp.sql"));
            
        } catch (Exception e) {
//            System.err.println("连接数据库失败: " + e);
            e.printStackTrace();
        }
        finally
        {
        	try{
        		if(null != dbConn)dbConn.close();
        	} catch (Exception e) {
                System.err.println("连接数据库失败: " + e);
            }
        }
        // 连接数据库失败: java.sql.SQLException: ORA-00933: SQL 命令未正确结束

    }
}
