package com.mtx.xiatian.hacker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 在开无线的时候 1、收集附近所有wifi的名字、mac并记录 2、并手工标记位置
 * 
 * @author xiatian
 */
public class GetWifiInfo extends CommonTools
{
	public GetWifiInfo(){}
	/**
	 * 获取ID给定信息的id
	 * 
	 * @param szDz
	 * @param szGPS
	 * @return
	 */
	public  String getGpsDZId(final String szDz, final String szGPS)
	{
		if (null == szDz || null == szGPS)
			return null;
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>();
		query("select id  from " + gpsDZ + " where gps='" + szGPS + "' and dz='" + szDz + "'", list);
		if (null == list || 0 == list.size())
			return null;
		return String.valueOf(list.get(0).get("id"));
	}

	/**
	 * 添加GPS坐标信息
	 * 
	 * @param szDz
	 * @param szGPS
	 * @param id
	 * @param tablename
	 */
	public  int doGPS(final String szDz, final String szGPS, String id)
	{
		if (null == szGPS || null == szDz)
			return 0;
		String szId = getGpsDZId(szDz, szGPS);
		if (null != szId)
			id = szId;
		TreeMap<String, Object> m1 = new TreeMap<String, Object>();
		m1.put("id", id);
		m1.put("gps", szGPS);
		m1.put("dz", szDz);
		return insertTable(gpsDZ, "id='" + m1.get("id") + "' and  gps='" + szGPS + "' and dz='" + szDz + "'", m1);
	}

	private  Map <String,Object>mYj = new HashMap<String,Object>();
	/**
	 * @param list
	 */
	public  int getWiFiMacs(String szDz, String szGPS)
	{
		String szId = getGpsDZId(szDz, szGPS);
		if (null == szId)
			szId = getId();
		doGPS(szDz, szGPS, szId);
		final String szGPSID = szId;

		String s1 = getCmdResult(new String[]
		{ "/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport", "-s" });
		if (null == s1 || 0 == s1.length())
			return 0;
		String[] a = s1.split("\n");
		// 跳过第一行
		a[0] = null;
		final String sP1 = "(^.*?)([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}", sP2 = "((?:[0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2})";
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
		{
			public TreeMap<String, Object> get(int i)
			{
				TreeMap<String, Object> m1 = super.get(i);
				List<TreeMap<String, Object>> lst01 = queryForList(wifi + " where mac='" + m1.get("mac") + "'");

				if (null != lst01 && 0 < lst01.size())
				{
					// 1、更新存活时间: lastScan；
					update("update " + wifi + " set lastScan='" + getTime() + "',RSSI='" + m1.get("RSSI") + "',CHANNEL='" + m1.get("CHANNEL") + "' where mac='" + m1.get("mac")  + "'");
					m1 = lst01.get(0);
					// 2、可能他的gpsid和当前不一样，那么说明这个mac出现在两个地方，则应该设置一个新的关联
					if (!szGPSID.equals(m1.get("id")))
					{
						TreeMap<String, Object> mTmp = new TreeMap<String, Object>();
						mTmp.put("id", szGPSID);
						mTmp.put("mac", m1.get("mac"));
						if (1 == insertTable(gpsDZ_Wifi, " id='" + szGPSID + "' and mac='" + m1.get("mac") + "'", mTmp))
							info("新的gps地址：", szGPSID, " 和mac：", m1.get("mac"), " 关联关系已经搞定了");
						else
							err("新的gps地址：", szGPSID, " 和mac：", m1.get("mac"), " 关联关系没有搞定，可能关联关系已经存在");
					}
					return null;
				}
				if (null == m1.get("lastScan"))
					m1.put("lastScan", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				info(m1.get("mac"));
				return m1;
			}
		};
		TreeMap<String, Object> m1 = null;
		String szRSSI = "^(-\\d{1,})\\s*", szCHANNEL = "^(\\d*(?:,(?:\\+|\\-)\\d*)*)";
		String rssi = "", channel = "", name, mac;
		 // {CHANNEL=TP-LINK_888888, RSSI=e4:d3:32:74:1a:e2, id=11,-1, lastScan=-83, mac=20151221134959, name=2015-12-22 22:07:00}
		for (String s : a)
		{
			if (null == s)
				continue;
			m1 = new TreeMap<String, Object>();
			s = s.trim();
			
			name = getPatternStr(sP1, s).trim();
			mac = getPatternStr(sP2, s).trim();
			s = s.replaceFirst("^.*?" + sP2 + "\\s*", "");
			rssi = getPatternStr(szRSSI, s).trim();
			s = s.replaceFirst("^.*?" + szRSSI + "\\s*", "");
			channel = getPatternStr(szCHANNEL, s).trim();
			
			m1.put("CHANNEL", channel);
			m1.put("RSSI",rssi );
			m1.put("id", szId);
			m1.put("lastScan", getTime());
			m1.put("mac", mac);
			m1.put("name", name);
			
			if(mYj.containsKey(m1.get("mac")))continue;
			list.add(m1);
			mYj.put(String.valueOf(m1.get("mac")), "");
		}
		int n = insert(wifi, list);
		if (0 < n)
			info("成功插入：", n);
		return n;
	}

	public  void doQueryErrDt()
	{
		 
			query("select *  from " + wifi + "  where length(id) < 12", new ArrayList<TreeMap<String, Object>>()
			{
				private  final long	serialVersionUID	= -7061840719725188173L;
				public boolean add(TreeMap<String, Object> m)
				{
					info(m);
					return true;
				}
			});
	}
	
	/**<pre>
	 * 不停的获取wifi信息
	 * 0、中国四川省成都市三色路163号银海芯座26楼北
	 * 1、中国四川省成都市瑞联路66号天合凯旋城2栋3楼
	 * </pre>
	 * @param nPos
	 */
	public  void doGetWiFi(int nPos)
	{
		final String[][] a = aGPS;
		final int n = nPos;
		while(true)
		{
			getWiFiMacs(a[n][0], a[n][1]);
			try
            {
	            Thread.sleep(13);
            } catch (Exception e)
            {
	            e.printStackTrace();
	            break;
            }
		}
	}
	
	public  void display()
	{
		query("select b.id,b.dz,count(1) count  from " + wifi + " a, " + gpsDZ + " b where a.id=b.id group by b.dz order by a.id  desc", new ArrayList<TreeMap<String, Object>>()
		{
			private  final long	serialVersionUID	= -7061840719725188173L;
			public boolean add(TreeMap<String, String> m)
			{
				info(m);
				return true;
			}
		});
	}
	
	
	@Override
    protected void finalize() throws Throwable
    {
	    super.finalize();
	    err("退出来了");
    }

	public static void main(final String[] args)
	{
		// 5广州; 0 公司
		System.setProperty("java.net.useSystemProxies", "true");
		final GetWifiInfo gwi = new GetWifiInfo();
		MyExecutors.getInstance().add(new Runnable(){
			public void run(){
				String[] a = args;
				if(null == a || 0 == a.length)
					a = new String[]{
//						 "5"
//						"8"
//						"9"
//						"0"
						"10"
						};
				gwi.doGetWiFi(Integer.parseInt(a[0]));}
		});
//		final String[][] a = aGPS;
//		final int n = 0;
//		if (null != args && 2 <= args.length)
//		{
//			a[n][0] = args[0];
//			a[n][1] = args[1];
//		}
		
//		update("ALTER TABLE " + wifi + " 'CHANGE' RSSI  AFTER lastScan;");
//		update("ALTER TABLE " + wifi + " add RSSI varchar(2000);");
//		update("ALTER TABLE " + wifi + " add CHANNEL varchar(20);");
//		update("delete from " + wifi + " where length(id) < 8;");
//		for(int i = 0; i < 200; i++)
//		{
////			new Thread(new Runnable()
////			{
////				public void run()
////				{
//					getWiFiMacs(a[n][0], a[n][1]);
////				}
////			}).start();
//		}
////		
////		doQueryErrDt();
//		display();
		gwi.query("select  count(1) as '附近wifi个数'  from " + wifi 
				+ "  where id='20151229223003391'"
				, new ArrayList<TreeMap<String, Object>>()
		{
			private  final long	serialVersionUID	= -7061840719725188173L;
			public boolean add(TreeMap<String, Object> m)
			{
				gwi.info(m);
				return true;
			}
		});
		// getWiFiMacs(a[1][0], a[1][1]);
				// update("update wifi set id='20151221134958'");
				// doGPS(a[1][0], a[1][1], "20151221134959");
	}
}
