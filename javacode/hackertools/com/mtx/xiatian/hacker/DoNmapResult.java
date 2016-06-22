package com.mtx.xiatian.hacker;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import weblogic.jdbc.wrapper.Array;

/**
 * 处理Nmap扫描的结果
 * @author xiatian
 */
public class DoNmapResult extends GetNetLanInfo
{
	
	/**
	 * 获取ip地址的端口信息
	 * doGetPort("192.168.0.1/24")
	 * @param ip
	 */
	public  void doGetPort(String ip)
	{
		File f = new File(ip);
		DoNmapResult dnr = this;
		dnr.useMysql();
		String []a = (f.exists()?InfoLog.getFile(f) :  dnr.getCmdResult("/Users/xiatian/safe/myNmap.sh", "-sS", ip)).split("Nmap scan report for |\\[bridge\\]");
		TreeMap <String,Object>m = new TreeMap<String,Object>();
		hvLastScan = false;
		String []port;
		Long lnSvId, lnPtId;
		for(String s:a)
		{
			m.clear();
			s = s.trim();
			ip = getPatternStr("(\\d*\\.\\d*\\.\\d*\\.\\d*)", s);
			if(dnr.isEmpty(ip))continue;
			m.put("ip",  ip);
			m.put("serverid", dnr.getLongId());
			m.put("mac",  getPatternStr("([0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F]:[0-9a-fA-F][0-9a-fA-F])", s));
			m.put("cjsj",  new Date());
			if(1 == dnr.insertTable("SFserver", "ip='{ip}'", m))
				dnr.info(ip, "主机信息插入成功");
			else// 更新mac 
			{
				if(!dnr.isEmpty(String.valueOf(m.get("mac"))) && 1 == dnr.update("update SFserver set mac='" + m.get("mac") + "' where mac='' and ip='" + ip+ "'"))
					dnr.info(ip, "mac信息更新成功");
			}
			port = s.split("PORT\\s*STATE\\s*SERVICE\\s*");
			lnSvId = Long.valueOf(String.valueOf(dnr.querySQL("select * from SFserver where ip='" + m.get("ip")+ "'").get("serverid")));
			if(1 < port.length)
			{
				port = port[1].split("\\n");
				for(String x: port)
				{
					m.clear();
					x = x.trim();
					if(dnr.isEmpty(x))continue;
					m.put("portId",lnPtId = dnr.getLongId());
					ip = getPatternStr("(\\d+)\\/", x);
					if(dnr.isEmpty(ip))continue;
					m.put("port",  Long.valueOf(ip));
					m.put("ptype", getPatternStr("\\d+\\/([^\\s]+)", x));
					m.put("portName",  getPatternStr("\\s([^\\s$]+)$", x));
					m.put("PortDesc", "");
					m.put("PortLeakDesc", "");
					
					if(1 == dnr.insertTable("SFportInfo", "port={port}", m))
						dnr.info(m.get("port"), "端口信息插入成功");
					lnPtId = Long.valueOf(String.valueOf(dnr.querySQL("select * from SFportInfo where port=" + m.get("port")).get("portId")));
					m.clear();
					m.put("serverid", lnSvId);
					m.put("portId", lnPtId);
					if(1 == dnr.insertTable("B", "serverid={serverid} and portId={portId}", m))
						dnr.info(lnSvId, "," , lnPtId, "端口主机映射信息插入成功");
				}
			}
//			else dnr.info(ip, "没有端口");
		}
	}
	public static int nCnt= 7;
	
	public static void doQianyiServerInfo()
	{
		DoNmapResult dnr = new DoNmapResult();
		dnr.useMysql();
		final DoNmapResult dnr1 = new DoNmapResult();
		dnr1.setConnInfo("jdbc:mysql://192.168.10.115:3306/qix1?relaxAutoCommit=true", "qix1", "qix1");
		dnr1.hvLastScan = false;
		dnr.querySQL("select * from SFserver", false, new ArrayList<TreeMap<String,Object>>()
				{
                    private static final long serialVersionUID = -8975014597758971548L;

					@Override
                    public boolean add(TreeMap<String, Object> m)
                    {
						m.put("hostip", m.get("ip"));
						m.put("macaddress", m.get("mac"));
						m.put("opertime", m.get("cjsj"));
						m.put("deptid", "11002");
						m.put("hostid", nCnt++);
						m.remove("ip");
						m.remove("cjsj");
						m.remove("serverid");
						m.remove("mac");
						if(1 == dnr1.insertTable("qihost", "hostip='{hostip}'", m))
							info(m);
	                    return true;
                    }
			
				});
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new DoNmapResult().doGetPort("/Users/xiatian/safe/xx.txt");
//		DoNmapResult dnr = new DoNmapResult();
//		dnr.useMysql();
//		final DoNmapResult dnr1 = new DoNmapResult();
//		dnr1.setConnInfo("jdbc:mysql://192.168.10.115:3306/qix1?relaxAutoCommit=true", "qix1", "qix1");
//		dnr1.hvLastScan = false;
//		dnr.querySQL("select * from SFportInfo a, SFserver b, B c", false, new ArrayList<TreeMap<String,Object>>()
//				{
//                    private static final long serialVersionUID = -8975014597758971548L;
//					@Override
//                    public boolean add(TreeMap<String, Object> m)
//                    {
//						m.put("hostip", m.get("ip"));
//						m.put("macaddress", m.get("mac"));
//						m.put("opertime", m.get("cjsj"));
//						m.put("deptid", "11002");
//						m.put("hostid", nCnt++);
//						m.remove("ip");
//						m.remove("cjsj");
//						m.remove("serverid");
//						m.remove("mac");
//						if(1 == dnr1.insertTable("qiprot", "hostip='{hostip}'", m))
//							info(m);
//	                    return true;
//                    }
//			
//				});
	}

}
