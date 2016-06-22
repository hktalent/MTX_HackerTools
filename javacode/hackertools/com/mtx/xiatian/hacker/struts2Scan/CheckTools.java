package com.mtx.xiatian.hacker.struts2Scan;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CheckTools implements IStruts2Scan{
	
	/**
	 * 结果
	 */
	private String szRst = null;
	
	/**
	 * 命令
	 */
	private String szCmd = null;
	/**
	 * 服务器端返回的结果
	 * @return
	 */
	public String getResult()
	{
		return szRst;
	}
	
	/**
	 * 设置要执行的命令
	 * @param s
	 */
	public CheckTools setCmd(String s)
	{
		try
        {
	        szCmd = java.net.URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
	        e.printStackTrace();
        }
		return this;
	}
	
	/**
	 * 替换命令
	 * @param s
	 * @return
	 */
	public String replace_whoami(String s)
	{
		String cmd = getCmd();
		if(null != cmd)
			s = s.replaceAll("whoami", cmd);
		return s;
	}
	
	/**
	 * 执行漏洞检查
	 * @param target
	 * @param poc1
	 * @return
	 */
	public boolean doS2LeakScan(String target, String poc1) {
		boolean flag = false;
		String poc = replace_whoami(poc1);
		String response = HttpRequestTool.doGet(target.indexOf("?") != -1 ? target + "&" + poc : target+"?"+poc);
		setResult(response);
		if(!response.toLowerCase().contains("<html") && null != response && 0 != response.length()){
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 获取要执行的命令
	 * @return
	 */
	public String getCmd()
	{
		return szCmd;
	}
	
	/**
	 * 服务器端返回的结果
	 * Disallowed Key Characters.
	 * @param s
	 */
	public void setResult(String s)
	{
		if(-1 == s.indexOf("<!DOCTYPE ") && -1 == s.indexOf("<HTML><HEAD>"))
			szRst = s;
	}
	
	/**
	 * 获取请求时间
	 */
	public static long getResultMs(String target) {
		long stime = System.currentTimeMillis(); // 开始时间
		HttpRequestTool.doGet(target);// 请求测试
		return System.currentTimeMillis() - stime;
	}
	
	/**
	 * 请求时差判断是否存在漏洞
	 * @param stime
	 * @param ntime
	 * @param result_ms
	 * @return
	 */
	public static boolean getTimeOut(long stime,long ntime,long result_ms){
		if (stime - ntime > (result_ms * (0.75))) {
			return true;
		}
		return false;
	}
	
	/**
	 * 分析哪些参数是整型
	 * @return
	 */
	public static Map<String,Object> getArgsType(String url){
		Map<String,Object> map = new HashMap<String, Object>();
		if(url.indexOf("?")!=-1){
			String s1[] = url.substring(url.indexOf("?")+1,url.length()).split("&");
			for(String s:s1){
				String s2[] = s.trim().split("=");
				try {
					Integer.parseInt(s2[1]);
					map.put(s2[0], true);
				} catch (NumberFormatException e) {
					map.put(s2[0], false);
				}
			}
		}
		return map;
	}
	
	/**
	 * 简单的转换下url的编码
	 * @param url
	 * @return
	 */
	public static String toURL(String url){
		url = url.replace("%3F", "?");
		url = url.replaceAll("%3d", "=");
		url = url.replaceAll("%26", "&");
		return url;
	}
	
	/**
	 * 获取当前时间
	 * @param regex
	 * @return
	 */
	public String getNowTime(String regex){
		return new SimpleDateFormat(regex).format(new Date());
	}
	
	public String getNowTime(){
		return getNowTime("yyyy-MM-dd HH:mm:ss");
	}

    public boolean doS2LeakScan(String url)
    {
	    return false;
    }
	
}
