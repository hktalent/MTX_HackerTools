package com.mtx.xiatian.hacker;

/**
 * http渗透测试工具：http隧道测试，如果已经被入侵，则可以连接
 * @author xiatian
 *
 */
public class HttpClientHacker
{

	/**
	 * com.sensepost.reDuh.reDuhClient
	 * @param args
	 */
	public static void main(String[] args)
	{
		String host = "";
		String []a = {"reDuh.jsp", "811.jsp", "a.jsp", "data.jsp", "data.jspx", "data1.jsp", "data2.jsp", "data111.jsp","datac.jsp", "dd.jsp", "jmxroot.jsp", "shuaige.jsp"};
		String []arg = {""};
		for(String s : a)
		{
			arg[0] =host + "" + s;
			com.sensepost.reDuh.reDuhClient.main(arg);
		}
	}

}
