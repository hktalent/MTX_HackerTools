package com.mtx.xiatian.hacker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import com.mtx.safegene.test.common.UrlTestTool;

public  class CommonTools extends  SqliteJDBC
{
	protected  UrlTestTool utt = new UrlTestTool();// 创建UrlTestTool对象
	protected  Map<String, HttpUriRequest> posts = new HashMap<String, HttpUriRequest>();// 请求
	protected  StringBuffer sbContent = new StringBuffer();
	protected  Map<String, Object> mParams = new HashMap<String, Object>();// 请求参数
	protected  Map<String, String>	headers = new HashMap<String, String>();// 请求头
	
	Long n = System.nanoTime();
	public Long getLongId()
	{
		return ++n;
	}
	/**
	 * 获取域名
	 * @param szUrl
	 * @return
	 */
	public String getIp(String szUrl)
	{
		String url = "http://www.msxindl.com/tools/ip/ipnum.asp";
		headers.clear();
		sbContent.delete(0, sbContent.length());
		mParams.put("keys", "url");
		mParams.put("ip", szUrl);
		headers.put("Origin", "http://www.msxindl.com");
		headers.put("Cookie", "ASPSESSIONIDSQBAQSDA=JEPHGCGCHDPCMBDIPJJKHGPI; HasUrl=HasChecked; u%5Fip=118%2E122%2E91%2E163; u%5Fcity=%CB%C4%B4%A8%CA%A1%B3%C9%B6%BC%CA%D0%B5%E7%D0%C5; CNZZDATA1108890=cnzz_eid%3D1901595094-1452068602-http%253A%252F%252Fwww.msxindl.com%252F%26ntime%3D1452068602");
		headers.put("Referer","http://www.msxindl.com/tools/ip/ip_num.asp");
		headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36");

		try
        {
	        utt.doPost(url, null, null, mParams, headers, sbContent, false, null, null);
        } catch (Throwable e)
        {
	        e.printStackTrace();
        }
		url = sbContent.toString();
		return url;
	}
	
	/**使用我本机的mysql数据库
	 */
	public  void useMysql()
	{
		setConnInfo("jdbc:mysql://127.0.0.1:3306/mydb?relaxAutoCommit=true&useUnicode=true&characterEncoding=utf8", "root", "root");
	}
	
	/**
	 * 查询表
	 * 
	 * @param queryTableName
	 */
	public  TreeMap<String, Object> query(final String queryTableName)
	{
		return querySQL("select *  from " + queryTableName);
	}

	/**
	 * 获取url的数据
	 * 
	 * @param url
	 * @return
	 */
	public  String getUrl(String url)
	{
		byte[] a = getUrlForByte(url, null);
		if (null == a || 0 == a.length)
			return null;
		try
		{
			return new String(a, "UTF-8");
		} catch (Throwable e)
		{
			err(e);
		}
		return null;
	}

	/**
	 * 获取url的数据
	 * 
	 * @param url
	 * @param szLocalFileName
	 * @return
	 */
	public  byte[] getUrlForByte(String url, String szLocalFileName)
	{
		UrlTestTool utt = new UrlTestTool();
//		Map<String, Object> mParams = new HashMap<String, Object>();// 请求参数
//		Map<String, String> headers = new HashMap<String, String>();// 请求头
		StringBuffer sbContent = new StringBuffer();
		OutputStream out = null;
		try
		{
			if (null != szLocalFileName)
			{
				out = new FileOutputStream(new File(szLocalFileName), false);
			} else
				out = new ByteArrayOutputStream();
		
			// new HttpPost(url)
			utt.doPost(url, null, null, mParams, headers, sbContent, false, null, null, out);
		} catch (Throwable e)
		{
			err(e);
		} finally
		{
			try
			{
				if (null != out)
					out.close();
			} catch (Throwable e)
			{
				err(e);
			}
		}
		if(null == out)
			return sbContent.toString().getBytes();
		if (null != szLocalFileName)return null;
		return ((ByteArrayOutputStream) out).toByteArray();
	}

	/**
	 * 判空
	 * 
	 * @param s
	 * @return
	 */
	public  boolean isEmpty(String s)
	{
		return null == s || 0 == s.trim().length();
	}

	public  TreeMap<String, Object> querySQL(final String sql)
	{
		return querySQL(sql, false, null);
	}
	/**
	 * 执行查询语句的sql
	 * 
	 * @param sql
	 */
	public  TreeMap<String, Object> querySQL(final String sql, final boolean bShow, final List<TreeMap<String, Object>> list)
	{
		final TreeMap<String, Object>m1  = new TreeMap<String, Object>();
		query(sql, null != list ? list : new ArrayList<TreeMap<String, Object>>()
		{
			private static final long	serialVersionUID	= -7061840719725188173L;

			public boolean add(TreeMap<String, Object> m)
			{
				if(bShow){
					info(m.toString() + "\n");
				}
				if(null != list)
					list.add(m);
				else m1.putAll(m);
				return true;
			}
		});
		return m1;
	}
	
//	/**
//	 * 查询sql语句
//	 * @param sql
//	 * @param lst
//	 */
//	public static void query(String sql, final List <Map<String, Object>>lst)
//	{
//		sj.queryForList(sql, new IMyResultHandler()
//		{
//            public void doResult(Map<String, Object> m)
//            {
//	            	Map<String, Object> m1 = new HashMap<String, Object>();
//	            	// 去除null值
//	            	for(String s : m.keySet())
//	            	{
//	            		if(null != m.get(s))
//	            			m1.put(s, m.get(s));
//	            	}
//            		lst.add(m1);
//            }
//		});
//	}
	
	/**
	 * 身份证查询经纬度
	 * http://www.gpsspg.com/sfz/?q=510922199207052***
	 * @param szSfz
	 * @return
	 */
	public String getSfzGPS(String szSfz)
	{
		return null;
	}

	/**
	 * 获取经纬度
	 * http://www.gpsspg.com/ajax/maps_get.aspx?lat=30.594994&lng=104.090569&type=2
	 * GET /ajax/maps_get.aspx?lat=30.594994&lng=104.090569&type=2 HTTP/1.1
Host: www.gpsspg.com
Connection: keep-alive
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36
Content-Type: application/x-www-form-urlencoded
DNT: 1
Referer: http://www.gpsspg.com/apps/maps/baidu_140802.htm
Accept-Encoding: gzip,deflate,sdch
Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.6
Cookie: ARRAffinity=19f333b2d215b6baa787066c0d18c3af94489c9a048b82529f5af563487a6052; Hm_lvt_15b1a40a8d25f43208adae1c1e12a514=1451280468; Hm_lpvt_15b1a40a8d25f43208adae1c1e12a514=1451280694; AJSTAT_ok_pages=2; AJSTAT_ok_times=1
	 * {"status":"ok", "match":1, 
	 * "gps":{"lat":30.59161411597, "lng":104.08149371866}, 
	 * "google":{"lat":30.58918971097, "lng":104.08402035956},
	 *  "baidu":{"lat":30.594994, "lng":104.090569}, 
	 *  "qq":{"lat":30.58917756597, "lng":104.08401634866}, 
	 *  "mapbar":{"lat":30.58425411597, "lng":104.08409371866}, 
	 *  "rid":"510104036005", "address":"四川省成都市锦江区柳江街道包江桥社区西北方向约0.94公里"}
	 * @param s
	 * @return
	 */
	public String getGPS(String s)
	{
		;
		return null;
	}

	/**
	 * 数据库操作实例
	 * 
	 * 靠近：四川省成都市锦江区三色路163
参考：四川省成都市锦江区柳江街道包江桥社区西北方向
谷歌地图：30.5889217110,104.0848553596
百度地图：30.5947260000,104.0914040000
腾讯高德：30.5889095660,104.0848513487
图吧地图：30.5839861160,104.0849287187
谷歌地球：30.5913461160,104.0823287187
北纬N30°35′28.85″ 东经E104°04′56.38″
	 */
	public static String[][]	aGPS	=
	                                 {
	                                 { "中国四川省成都市三色路163号银海芯座26楼北", "30.5889217110,104.0848553596" },
	                                 { "中国四川省成都市瑞联路66号天合凯旋城2栋3楼", "104.007571,30.672306" },
	                                 { "中国四川省成都市新业路（百叶路1号）电子科技大学成都学院（校内）红咖啡西餐厅（霞光店）", "103.96871,30.736255" },
	                                 { "中国四川省成都市二环路营门立交桥成都劳动保障大厦2507", "104.047474,30.69693" },
	                                 { "中国四川省成都市2.5环青羊大道、光华大道交界肯德基", "104.013985,30.670706" },
	                                 { "中国广州市教育路88号广东省人力资源和社会保障厅 712会议室", "23.1238261671,113.2659937819" },
	                                 { "中国四川省成都市双流县西航港大道中二段（东升街道办迎春桥社区东南方向约0.93公里）", "30.5828446120,103.9546481691" },
	                                 { "中国广州白云国际机场(1楼南出发)", "23.3853587783,113.3040208151" },
	                                 { "中国广州教育路7天酒店", "123" },
	                                 { "豪生大酒店", "1236" },
	                                 { "东苑小区", "234242424" },
	                                 
	                                 };

	/**
	 * 获取曾经获取时出错的url
	 * @param type   leak、等表名
	 * @param urls
	 */
	public  void getErrUrls(String type, final List<String>urls)
	{
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
		{
			private static final long	serialVersionUID	= -7061840719725188173L;
			public boolean add(TreeMap<String, Object> m)
			{
				urls.add(String.valueOf(m.get("url")));
				return true;
			}
		};
		querySQL("select url from " + errUrls + " where type='" + type + "'", false, list);
	}
	
	/**
	 * 记录错误的url
	 * @param type
	 * @param url
	 */
	public  void insertErrUrl(String type, String url)
	{
		TreeMap<String, Object> m = new TreeMap<String, Object>();
		m.put("type", type);
		m.put("url", url);
		insertTable(errUrls, "type='" + type + "' and url='" + url + "'", m);
	}
	
	
	/**
	 * 获取上次的位置
	 * @param type
	 * @param nDeafult 未获取到返回的默认值
	 * @return
	 */
	public  int getLastPos(String type, int nDeafult)
	{
		TreeMap<String, Object> m = querySQL("select pos from " + lastPos + " where type='" + type + "'", false, null);
		if(null != m && 0 < m.size())
			return Integer.valueOf(String.valueOf(m.get("pos")));
		return nDeafult;
	}
	
	/**
	 * 记录错误的url
	 * @param type
	 * @param url
	 */
	public  void insertLastPosl(String type, int n)
	{
		TreeMap<String, Object> m = new TreeMap<String, Object>();
		m.put("type", type);
		m.put("pos", n + "");
		insertTable(lastPos, "type='" + type + "' and pos='" + n + "'", m);
	}
	/**
	 * 执行命令、返回结果
	 * 
	 * @return
	 */
	public  String getCmdResult(String... arg)
	{
		Process p = null;
		ByteArrayOutputStream out = null;
		InputStream in = null;
		OutputStream outCmd = null;
		String s = null;
		try
		{
			out = new ByteArrayOutputStream();
			p = Runtime.getRuntime().exec(arg);
			p.waitFor();
			in = p.getInputStream();
			byte[] b = new byte[1024];
			int i = 0;
			while (-1 < (i = in.read(b, 0, b.length)))
			{
				out.write(b, 0, i);
			}
			b = out.toByteArray();
			s = new String(b, "UTF-8");
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (null != outCmd)
					outCmd.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				if (null != in)
					in.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				if (null != out)
				{
					out.close();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			if (null != p)
				p.destroy();
		}

		return s;
	}

	/**
	 * 获取一个ID
	 * 
	 * @return
	 */
	public  String getId()
	{
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
	}

	/**
	 * 更新数据
	 * 
	 * @param tableName
	 * @param where
	 * @param m1
	 * @param aFild
	 * @return
	 */
	public  int update(final String tableName, final String where, TreeMap<String, Object> m1, String[] aFild)
	{
		List<TreeMap<String, Object>> lst01 = queryForList(tableName + " where " + where);
		if (null != lst01 && 0 < lst01.size())
		{
			// 更新存活时间: lastScan
			StringBuffer bf = new StringBuffer("update " + tableName + " set  ");
			int i = 0;
			for (String s : aFild)
			{
				if (0 < String.valueOf(m1.get(s)).length())
				{
					if (0 < i)
						bf.append(",");
					bf.append(s).append("='").append(m1.get(s)).append("'");
					i++;
				}
			}
			if (0 == i)
				return 0;
			return update(bf.toString() + " where " + where);
		}
		return 0;
	}

	/**
	 * gps信息库 gpsDZ : 编号、地址、gps、最后一次更新时间 gpsDZ_Wifi:
	 * 同一个mac出现在不同的地址上的关联，id（gps编号）、mac wifi： gps编号、名字、mac、最后一次更新时间、RSSI（信号强度）、
	 * mac: gps编号(id)、macId编号、mac、最后一次更新时间；和ip关联时，允许和多个ip关联
	 * server：mac编号、ip、主机编号（svId）、主机名、操作系统、设备类型、其他信息、最后一次更新时间 端口信息:
	 * svId、端口、协议类型、服务名、其他信息、最后一次更新时间
	 * 
	 * lastPos: 上次执行的位置， type、pos
	 * RSSI 因为是负数，那么数字越大说明损耗也越大。当损耗达到一定程度时， 你就连不上无线了，损耗再大点就直接搜不到信号了，因为信号都损耗完了。
	 * 所以那个数字还是越小越好，越小说明信号越强、质量越好。
	 */
	public static String	gpsDZ	= "gpsDZ", wifi = "wifi", mac = "mac", server = "server", portInfo = "portInfo", gpsDZ_Wifi = "gpsDZ_Wifi",
	        GHDB = "GHDB", Exloits = "Exloits", errUrls = "errUrls", lastPos = "lastPos", leak = "leak";

	/**
	 * 处理参数
	 * 
	 * @param szParm
	 * @param m1
	 * @return
	 */
	private  String parseParameter(String szParm, TreeMap<String, Object> m1)
	{
		if(null == m1 || 0 == m1.size())return szParm;
		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile("\\{([^\\}]*)\\}", Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(szParm);
		while (m.find())
		{
			if(null == m.group(1) || null == m1.get(m.group(1)))
			{
				info("请检查参数：", szParm, " 中 ", m.group(1), "不存在了");
			}
			m.appendReplacement(sb, String.valueOf(m1.get(m.group(1))));// String index out of range: 10
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public boolean hvLastScan = true;
	/**
	 * 插入一张表
	 * 
	 * @param tablename
	 *            表名
	 * @param where
	 *            where后的查询条件避免多次插入
	 * @param lst
	 *            数据
	 * @return
	 */
	public  int insertTable(final String tablename, final String where, List<TreeMap<String, Object>> lst)
	{
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>()
		{
			private static final long	serialVersionUID	= -7385462347598647262L;

			public TreeMap<String, Object> get(int i)
			{
				TreeMap<String, Object> m1 = super.get(i);
				if(null !=where)
				{
					List<TreeMap<String, Object>> lst01 = queryForList(tablename + " where " + parseParameter(where, m1));
					if (null != lst01 && 0 < lst01.size())
					{
						// 更新存活时间: lastScan
						;
						return null;
					}
				}
				if(hvLastScan && null != m1)
					m1.put("lastScan", getTime());
				return m1;
			}
		};
		for (TreeMap<String, Object> m : lst)
		{
			list.add(m);
			synchronized(list)
			{
				list.notifyAll();
			}
		}
		list.add(null);
		return insert(tablename, list);
	}

	public static String getTime()
	{
		return getTime("yyyy-MM-dd HH:mm:ss");
	}
	
	public static String getTime(String szFormat)
	{
		return new SimpleDateFormat(szFormat).format(new Date());
	}

	/**
	 * 插入单条数据
	 * 
	 * @param tablename
	 * @param where
	 * @param m
	 * @return
	 */
	public  int insertTable(final String tablename, final String where, TreeMap<String, Object> m)
	{
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>();
		list.add(m);
//		synchronized(list)
//		{
//			list.notifyAll();
//		}
		list.add(null);
//		synchronized(list)
//		{
//			list.notifyAll();
//		}
		return insertTable(tablename, where, list);
	}


	static
	{
		System.setProperty("java.net.useSystemProxies", "true");
		// 正常允许配置
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
//				err("退出来了哈");
			}
		});
	}
	public static String	urlWY	= "http://www.wooyun.org", myWYtype = leak + "WY";
	public static void main(String []args)
	{
//		useMysql();
//		sj.update("ALTER TABLE " + leak + " add lastScan varchar(25);");
//		sj.delete("delete from lastPos where type='" + myWYtype + "'");
//		sj.delete("delete from " + errUrls + " where type='" + leak + "'");
		
//		sj.update("create table IF NOT EXISTS  GHDB(desc varchar(2000),gds varchar(2000),ghdbId varchar(40),lastScan varchar(25),srcUrl varchar(400),submited varchar(300),url varchar(500));");
//		GetAllLeakData.queryForyWord(new ArrayList<TreeMap<String, String>>()
//		{
//			private static final long	serialVersionUID	= -7061840719725188173L;
//			public boolean add(TreeMap<String, String> m)
//			{
//					info(m.toString());
//				return true;
//			}
//		}, new String[]{"贵州","贵阳","四川", "成都", "重庆", "都江堰", "温江", "绵阳", "吉林", "太原","西安","陕西","黄冈","咸宁","荆州","德阳","天津","武汉","云南","昆明","内江"}, new String[]{"人社", "社保", "医疗", "劳动", "医保","单位", "银海"});
//		querySQL("select * from leak where url like 'http://www.wooyun.org/bugs/wooyun-%'", true, null);
//		info(getLastPos(myWYtype, 1));
//		info(GetAllLeakData.getLeakCnt());
	}
}
