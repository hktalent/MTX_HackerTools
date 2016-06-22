package com.mtx.xiatian.hacker.struts2Scan;

public interface IStruts2Scan {
	
	//延迟时间
	long result_ms = 6000;
	
	public boolean doS2LeakScan(String url);

	/**
	 * 获取命令、漏洞执行返回的结果
	 * @return
	 */
	public String getResult();
	
	/**
	 * 设置要执行的命令
	 * @param s
	 */
	public CheckTools setCmd(String s);
	
	/**
	 * 获取要执行的命令
	 * @return
	 */
	public String getCmd();
	/**
	 * 服务器端返回的结果
	 * @param s
	 */
	public void setResult(String s);
}
