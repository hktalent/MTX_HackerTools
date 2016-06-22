package com.mtx.xiatian.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 默认实现
 * @author xiatian
 */
public class MySQLCallBack implements ISQLCallBack
{
    private static final long serialVersionUID = -8258971472992089663L;
	public int doPreparedStatement(PreparedStatement p, Connection conn)
	{
		return 0;
	}
	public String getSql()
	{
		return null;
	}
	public boolean isInsert(){return false;}

}
