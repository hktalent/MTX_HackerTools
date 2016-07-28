package com.mtx.xiatian.hacker.metasploit;

import java.io.OutputStream;

/**
 * <pre>
 * msfd -a 127.0.0.1 -p 2323
 * load openvas
 * openvas_connect admin 123456 192.168.29.29 9390 ok
 * </pre>
 * @author xiatian
 */
public class OpenVas extends Metasploit
{
	/**
	 * 默认可查msf数据库
	 */
	public OpenVas()
	{
		super();
	}
	
	public void doAllIps()
	{
		getCmdResult(System.out, new IGetInputMSFCmd()
		{
            public String getCmd()
            {
            		String []a = {"load openvas\r\n",
            				// 服务器的ip随时会变
            				 "openvas_connect admin 123456 192.168.28.10 9390 ok\r\n",
            				};
            		// 创建目标任务：openvas_target_create <name> <hosts> <comment>
            		// openvas_target_create 60Server 192.168.10.60 Metasploitable
            		
	            return null;
            }
			@Override
            public OutputStream getOutputStream()
            {
	            return null;
            }
		},"/usr/bin/nc",
		"127.0.0.1",
		"2323");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}
}
