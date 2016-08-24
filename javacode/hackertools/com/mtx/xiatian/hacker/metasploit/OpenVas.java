package com.mtx.xiatian.hacker.metasploit;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mtx.xiatian.hacker.InfoLog;
/**
 * <pre>
 * msfd -a 127.0.0.1 -p 2323
 * load openvas
 * openvas_connect admin 123456 192.168.29.29 9390 ok
 * </pre>
 * 
 * @author xiatian
 */
public class OpenVas extends Metasploit
{
	/**
	 * 默认可查msf数据库
	 */
	public OpenVas()
	{
		setConnInfo("jdbc:sqlite:/Volumes/dbdata/sgkzl/MTX私有/收集的db/openvas/tasks.db", "tasks", "");
	}
	/**
	 * 所有ip
	 */
	public void doAllIps()
	{
		getCmdResult(System.out, new IGetInputMSFCmd()
		{
			public String getCmd()
			{
				String[] a =
				{ "load openvas\r\n",
				        // 服务器的ip随时会变
				        "openvas_connect admin xtmt2016 192.168.10.243 9390 ok\r\n", };
				// 创建目标任务：openvas_target_create <name> <hosts> <comment>
				// openvas_target_create 60Server 192.168.10.60 Metasploitable
				//
				// System.out.println("openvas_target_create 公司安全巡检" + (10 > n ?
				// "0" : "") + n + " " + k + " 公司外网安全巡检" + k);
				return null;
			}
			@Override
			public OutputStream getOutputStream()
			{
				return null;
			}
		}, "/usr/bin/nc", "127.0.0.1", "2323");
	}
	public static void sendCmd(String s, OutputStream outCmd)
	{
		try
		{
			outCmd.write((s + "\r\n").getBytes());
			outCmd.flush();
			Thread.sleep(2000);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// OpenVas ov = new OpenVas();
		// ov.doAllIps();
		//
		String s = InfoLog.getFile(new File("/Users/xiatian/safe/tianxiamtx/javacode/hackertools/com/mtx/xiatian/hacker/metasploit/1.txt")).trim();
		String[] a = s.trim().split("\\r|\\n");
		Map<String, String> m = new HashMap<String, String>();
		int n = 1;
		Runtime rt = Runtime.getRuntime();
		Process p = null;
		OutputStream outCmd = null;
		try
		{
			// rt.
			p = rt.exec(new String[]
			{ "/usr/bin/nc", "127.0.0.1", "2323" });
			outCmd = p.getOutputStream();
			InputStream in = p.getInputStream();
			for (String k : a)
			{
				k = k.trim().replaceAll("(^.*?\\/+)|([:\\/].*$)", "").toLowerCase();
				if (3 > k.replaceAll("\\d", "").length())
					continue;
				sendCmd("load openvas", outCmd);
				sendCmd("openvas_disconnect", outCmd);
			    sendCmd("openvas_connect admin 123456 192.168.0.112 9390 ok", outCmd);

			    if (k.startsWith("192."))
					continue;
				// 避免重复
				if (!m.containsKey(k))
				{
					sendCmd("openvas_target_create 巡检_target_" + k + " " + k + " " + k, outCmd);
					sendCmd("openvas_task_create 巡检_task_" + k + " " + k + " " + " 5 " + n, outCmd);
					sendCmd("openvas_task_start " + (n - 1),outCmd);
					new Thread(new MetasploitRest(in, System.out)).start();
					// 错误消息
					new Thread(new MetasploitRest(p.getErrorStream(), System.out)).start();
					System.out.println(k);
					m.put(k, "");
					n++;
				}
				p.waitFor();
				in.close();
				if (null != p)
					p.destroy();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// List <TreeMap<String,Object>>list = new
		// ArrayList<TreeMap<String,Object>>(){
		// public boolean add(TreeMap<String, Object> m)
		// {
		// System.out.println(m);
		// return true;
		// }
		// };
		// // ov.query("ATTACH tasks.db AS my_db", list);
		// //
		// ov.query("SELECT name FROM my_db.sqlite_master WHERE type='table'",
		// list);
		// ov.query("SELECT name FROM sqlite_master WHERE type='table'", list);
	}
}
