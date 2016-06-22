package com.mtx.xiatian.hacker.test;

import java.util.Properties;

import com.mtx.xiatian.hacker.CommonTools;

public class MyTest extends CommonTools
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		MyTest mt = new MyTest();
//		mt.querySQL("select count(1) from server", true, null);
//		mt.querySQL("select count(1) from portInfo ", true, null);
		//  java.version;java.class.version;os.name;os.version
		Properties p = System.getProperties();
		p.list(System.out);
//		System.out.println(p);

	}

}
