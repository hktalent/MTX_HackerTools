package com.mtx.xiatian;

import java.io.File;

import com.mtx.xiatian.hacker.CommonTools;

/**
 * 通讯录处理，解决分组重复问题
 * @author xiatian
 *
 */
public class TxlCl extends CommonTools
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String s = CommonTools.getFile(new File("/Users/xiatian/Desktop/5642位联系人.vcf"));
		s = s.replaceAll("(UID|X-ABUID):[^\\n]+\\n", "");
		CommonTools.writeFile("/Users/xiatian/Desktop/5642位联系人1.vcf", s);
		System.out.println("Ok");
	}

}
