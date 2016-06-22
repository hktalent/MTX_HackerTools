package com.mtx.xiatian.hacker;

import java.util.Map;

public interface  ICallBack {

	/**
	 * 处理map数据，例如相应的response的头信息
	 * @param m
	 */
	public void doMap(Map<String,String> m);
	
	/**
	 * 获取数据，例如相应的response的头信息
	 * @return
	 */
	public Map<String,String> getMap();
}
