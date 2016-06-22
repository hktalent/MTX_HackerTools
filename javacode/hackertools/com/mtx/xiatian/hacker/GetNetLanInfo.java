package com.mtx.xiatian.hacker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mtx.xiatian.db.IConst;

/**
 * 获取局域网信息：
 * 
 * <pre>
 * 1、记录各主机信息，打开的端口 
 * 2、并和gpsDZ关联
 * </pre>
 * 
 * @author xiatian
 */
public class GetNetLanInfo extends CommonTools
{

	public GetNetLanInfo(){}
	/**
	 * 获取mac信息的macId、gps编号(id) 如果没有就插入一条
	 * 
	 * @param szMAC
	 * @param nGps
	 *            默认使用0坐标
	 * @return
	 */
	public  TreeMap<String, Object> getMacId(final String szMAC, int nGps)
	{
		if (null == szMAC || 0 == szMAC.trim().length())
			return null;
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>();
		query("select id,macId from " + mac + " where mac='" + szMAC + "'", list);
		if (null == list || 0 == list.size())
		{
			TreeMap<String, Object> m = new TreeMap<String, Object>();
			m.put("id", new GetWifiInfo().getGpsDZId(aGPS[nGps][0], aGPS[nGps][1]));
			m.put("macId", getId());
			m.put("mac", szMAC);
			if (1 == insertTable("mac", "mac='" + szMAC + "'", m))
				return m;
			return null;
		}
		return list.get(0);
	}

	private  Map <String,Object> macToSvrId = new HashMap <String,Object>(); 
	/*
	 * 获取主机ip、mac等信息 sudo nmap -sP -PI -PT 192.168.1.0/24 
	 * 获取主机名等信息： sudo nmap -A --system-dns -T4 192.168.8.0/24
	 */
	public  int getMacs(String szFileName, String szRst)
	{
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>(){
			public TreeMap <String,Object> get(int i)
			{
				TreeMap <String,Object> m1 = super.get(i);
				 m1.put("lastScan", getTime());
				if(0 == update(server, "ip='" + m1.get("ip") + "'", m1, new String[]{"dt", "sysInfo", "cn", "lastScan"}))
					return m1;
				return null;
			}			
		};
		String s = null == szRst ? IConst.readFile(szFileName) : szRst;
		String s2 = "Nmap scan report for ", s3 = "PORT      STATE SERVICE", svId;
		TreeMap<String, Object> m = null, mMacId;
		if (null == s || 0 == s.trim().length())
		{
			return 0;
		}
		s = s.replaceAll("(\\n|\\r| |\\t)(\\| Key|SF:|\\| Not valid |Status:|Warning:|Too many |TRACEROUTE|===+|WARNING:|Not shown:|All 1000 scanned|Network Distance:|Host is up |Service detection performed)[^\\n\\r]+(\\n|\\r)", "\n");
		s = s.replaceAll("(\\n|\\r)(All|Nmap done:|Connect Scan Timing: About)\\s*\\d*[^\\n]+\\n", "\n");
		s = s.replaceAll("(\\s*SF:).*?(\\n|\\r)", "\n");
		s = s.replaceAll("Not shown: \\d+ closed ports\\s*", "");
		s = s.replaceAll("(\\n|\\r)[\\|_\\s\\d\\.]+?[^a-z]*(\\r|\\n)", "\n");
		String[] a = s.split(s2);
		int n = 0;
		for (String x : a)
		{
			m = new TreeMap<String, Object>();
			s = getPatternStr("(\\d*\\.\\d*\\.\\d*\\.\\d*)", x);
			if (0 == s.length())
				continue;

			// 主机server id
			m.put("svId", svId = getId());
			// ip地址
			m.put("ip", s);
			// NetBIOS MAC:\\s*([^\\s\\(\\n]*) MAC Address:\\s*([^\\s\\(\\n]*)
			s = getPatternStr(
			        "\\bMAC\\s*[^:]*:.*?([0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F])",
			        x);
			if(0 == s.length())continue;
			// 获得mac信息
			mMacId = getMacId(s, 0);
			if(null == mMacId)
			{
				info("不应该为null的MAC：", s);
				continue;
			}
			if(macToSvrId.containsKey(s))
				m.put("svId", macToSvrId.get(s));
			else
			{
				m.put("svId", getServId(s, svId));
				macToSvrId.put(s, m.get("svId"));
			}
			
			m.put("macId", mMacId.get("macId"));

			n = x.indexOf(s3);
			if (-1 < n)
			{
				x = doExecPort(x.substring(n + s3.length()), svId);
			}
			// Device type: general purpose
			m.put("dt", getPatternStr("Device type: ([^\\r\\n]+)", x));
			// Running: Microsoft Windows 2008|7
			s = getPatternStr("Running: ([^\\r\\n]+)", x);
			// OS: Windows;
			if(null == s || 0 == s.length())
				s = getPatternStr("OS: ([^;]+)", x);
			m.put("sysInfo", s);
			
			// Computer name: yh-143
			s = getPatternStr("Computer name: ([^\\r\\n]+)", x);
			// OS: Windows;
			if(null == s || 0 == s.length())
				s = getPatternStr("NetBIOS computer name: ([^;]+)", x);
			m.put("cn", s);
			
			x = x.replaceAll("\\d*/(tcp|udp)\\s*[^\\n]*?\n", "");
			m.put("other", x);
			list.add(m);
		}
		 n = insert(server, list);
		if (0 < n)
			System.out.println("成功插入：" + n);
		return n;
	}

	/**
	 * 处理端口
	 * 
	 * @param s
	 * @param svId
	 *            主机id
	 * @return
	 */
	private  String doExecPort(String s, String svId)
	{
		StringBuffer bf = new StringBuffer();
		Pattern p = Pattern.compile("(\\d*)\\/([a-z]+)\\s*[a-zA-Z]+\\s*([^\\r\\n]+)", Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(s);

		TreeMap<String, Object> m1 = null;
		m1 = new TreeMap<String, Object>();
		while (m.find())
		{
			m1.clear();
			m.appendReplacement(bf, "");
			m1.put("svId", svId);
			m1.put("port", m.group(1));
			m1.put("type", m.group(2));
			m1.put("svcname", m.group(3));
			if(null == m1.get("port") || 0 == String.valueOf(m1.get("port")).length())continue;
			
			insertTable(portInfo, "svId='" + svId + "' and port='" + m1.get("port") + "'", m1);
		}
		m.appendTail(bf);
		return bf.toString().trim().replaceAll("(\\r|\\n)+", "\n").replaceAll(p.toString(), "");
	}
	
	
	/**
	 * 获取getServId
	 * @param macId
	 * @param svId
	 * @return
	 */
	public  String getServId(String mac, String svId)
	{
		final TreeMap<String, Object> m1 = new TreeMap<String, Object>();
		query("select a.svId from server a, mac b  where a.macId=b.macId and b.mac='" + mac + "'", new ArrayList<TreeMap<String, Object>>()
				{
					private  final long	serialVersionUID	= -7061840719725188173L;
					public boolean add(TreeMap<String, Object> m)
					{
						m1.putAll(m);
						return true;
					}
				});
		return String.valueOf(null == m1.get("svId") ?  svId : m1.get("svId"));
	}
	
	/**
	 * 简单查询
	 * @param ip
	 */
	public  void doQueryPort(String ip)
	{
			query("select a.ip,c.mac,b.port,b.type,b.svId, b.svcname from server a, portInfo b, mac c where a.svId = b.svId and a.macId=c.macId and a.ip='" + ip + "'", new ArrayList<TreeMap<String, Object>>()
			{
				private  final long	serialVersionUID	= -7061840719725188173L;
				public boolean add(TreeMap<String, Object> m)
				{
					info(m);
					return true;
				}
			});
	}
	
	public  void doQueryPort2(String svid)
	{
			query("select * from  portInfo   where svId ='" + svid + "'", new ArrayList<TreeMap<String, Object>>()
			{
				private  final long	serialVersionUID	= -7061840719725188173L;
				public boolean add(TreeMap<String, Object> m)
				{
					info(m);
					return true;
				}
			});
	}

	public  void doQuery(String tb)
	{
			query("select *  from " + (null ==tb ? server : tb) + " ", new ArrayList<TreeMap<String, Object>>()
			{
				private  final long	serialVersionUID	= -7061840719725188173L;
				public boolean add(TreeMap<String, Object> m)
				{
					info(m);
					return true;
				}
			});
	}
	
	public  void doGetAllIpInfo(String ip)
	{
		String s = getCmdResult("/Users/xiatian/safe/myNmap.sh", "-sS", ip);
		info(s);
		getMacs(null, s);
	}
	
	/**
	 * 输出全网段命令
	 */
	public  void doAllNmap()
	{
		String []a = {
//				"sP", 
//				"sS"
//				, "sU", "A", 
				"O"
				};
		for(String s:a)
		{
			for(int i = 0; i < 256; i++)
				System.out.println("nmap -sS -Pn -T5 --system-dns  192.168." + i + ".0/24 >> nmap.txt");
		}
	}
	public static  void main(String[] args)
	{
		GetNetLanInfo gnli = new GetNetLanInfo();
//		delete("drop table " + mac);
//		delete("drop table " + server);
//		delete("delete from " + mac + " where mac=''");
//		delete("delete from " + mac);
//		delete("delete from " + server);
//		delete("delete from " + portInfo);
//		getMacs("/Users/xiatian/safe/nmap.txt", null);
//		getMacs("/Users/xiatian/safe/all10A.txt", null);
//		doQuerySI(mac);
//		doQuerySI(server);
//		querySQL("select  * from gpsDZ ", true);
		gnli.querySQL("select d.dz,b.ip,a.port from portInfo a, server b,mac c, gpsDZ d  where a.port = '1521' or a.port='3306' and c.macId = b.macId and d.id=c.id and  d.id='20151221134958' and c.id='20151221134958'", true, null);
//		doGetAllIpInfo("192.168.0.104");
//		doQueryPort("192.168.0.1");
//		doQueryPort2("20151223185218098");
		
//		doAllNmap();
	}
	
}
