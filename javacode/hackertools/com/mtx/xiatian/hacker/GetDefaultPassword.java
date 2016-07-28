package com.mtx.xiatian.hacker;

import java.io.File;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * 获取默认密码
 * http://www.defaultpassword.com/
 * </pre>
 * 
 * @author xiatian
 * 
 */
public class GetDefaultPassword extends CommonTools
{
	public GetDefaultPassword()
	{
	}
	/**
	 * 获取一些默认密码
	 */
	public void getPswd()
	{
		OutputStream out = null;
		try
		{
			byte[] b = getUrlForByte("http://www.defaultpassword.com/", null);
			String s = new String(b, "UTF-8").trim(), s1 = "<TR VALIGN=\"top\"><TD>Manufactor<";
			if(0 == s.length())
			{
				System.out.println("没有获取到数据");
				return;
			}
			int i = s.indexOf(s1);
			if(-1 < i)
				s = s.substring(i);
			i = s.lastIndexOf("</table><small>");
			if(-1 < i)
				s = s.substring(0, i);
			String []a = s.split("<\\/TR>");
			StringBuffer buf = new StringBuffer(""); 
			Pattern p = Pattern.compile("<TD[^>]*>([^<]*?)<\\/TD>", Pattern.DOTALL|Pattern.MULTILINE);
			Matcher m = null;
			File f1 = new File("./data/defaultpassword.txt");
			
			for(int x = 0, j = a.length; x < j; x++)
			{
				m = p.matcher(a[x]);
				while(m.find())
				{
					buf.append(m.group(1).trim()).append("\t");
				}
				buf.append("\n");
				out = MyDir.writeFile(f1, buf.toString(), out);
				buf.delete(0, buf.length());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(null != out)
			{
				try{
					out.flush();
					out.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new GetDefaultPassword().getPswd();
	}
}
