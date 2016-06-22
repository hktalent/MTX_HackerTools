package com.mtx.safegene.test.face;

import java.util.Map;

public interface  ICallBack {

	/**
	 * 处理map数据
	 * @param m
	 */
	public void doMap(Map<String,String> m);
	
	/**
	 * 获取数据
	 * @return
	 */
	public Map<String,String> getMap();
}
