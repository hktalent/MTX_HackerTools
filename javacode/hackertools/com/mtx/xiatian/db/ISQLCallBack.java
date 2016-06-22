package com.mtx.xiatian.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;

public interface ISQLCallBack extends Serializable
{

	/**
	 * 回调，返回false则停止执行
	 * p.addBatch();
	 * p.executeBatch();
	 * @param p
	 * @param conn
	 * @return
	 */
	public int doPreparedStatement(PreparedStatement p, Connection conn);
	
	/**
	 * 返回当前需要执行的SQL语句
	 * @return
	 */
	public String getSql();
	
	public boolean isInsert();
	
}
