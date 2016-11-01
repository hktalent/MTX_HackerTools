package com.mtx.xiatian.hacker;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mtx.safegene.test.common.UrlTestTool;

/**
 * <pre>
 * 爬取政府网站地址 1、获取首页中的所有第三方连接 2、获取第二层连接中的所有第三方连接 http://www.gov.cn/
 * http://www.chengdu.gov.cn/ http://www.chengdu.gov.cn/organization.shtml
 * CREATE TABLE `zfwebserver` ( `url` VARCHAR(160) NOT NULL, `cjrq` DATETIME
 * NULL, `getnext` INT NULL COMMENT '是否分析过第二层连接', `ip` VARCHAR(15) NULL, `mac`
 * VARCHAR(50) NULL, `osname` VARCHAR(245) NULL, PRIMARY KEY (`url`), UNIQUE
 * INDEX `url_UNIQUE` (`url` ASC));
 * ALTER TABLE `zfwebserver` 
CHANGE COLUMN `cjrq` `cjrq` DATETIME NULL DEFAULT NULL COMMENT '采集日期' ,
CHANGE COLUMN `mac` `mac` VARCHAR(50) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NULL DEFAULT NULL COMMENT 'mac物理地址' ,
CHANGE COLUMN `osname` `osname` VARCHAR(245) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NULL DEFAULT NULL COMMENT '操作系统名称' ,
ADD COLUMN `servername` VARCHAR(150) NULL COMMENT '服务器名' AFTER `osname`,
ADD COLUMN `devlg` VARCHAR(80) NULL COMMENT '开发的语言信息' AFTER `servername`;
ALTER TABLE `mydb`.`zfwebserver` 
ADD COLUMN `title` VARCHAR(400) NULL COMMENT '网站标题' AFTER `devlg`;
ALTER TABLE `mydb`.`zfwebserver` 
ADD COLUMN `lport` INT NULL COMMENT '漏洞的port' AFTER `title`;
</pre>
 * @author xiatian
 */
public class GetZFWebServer extends CommonTools
{

	/**
	 * 获取url的内容
	 * 
	 * @param url
	 * @return
	 * @throws Throwable 
	 */
	public static String getUrlText(String url, Map<String, String> headers) throws Throwable
	{
		UrlTestTool utt = new UrlTestTool();// 创建UrlTestTool对象
		StringBuffer sbContent = new StringBuffer();
		Map<String, Object> mParams = new HashMap<String, Object>();// 请求参数
		headers.clear();
		utt.doPost(url, null, null, mParams, headers, sbContent, false, null, null);
		return sbContent.toString();
	}
	
	public static String szUrlEd = ".edu.cn";// ".gov.cn";
	/**
	 * 需要处理的政府网站
	 * @param szUrl
	 * @return
	 */
	public static boolean isZfUrl(String szUrl)
	{
//		return true;
		return (szUrl.endsWith(".cn") || szUrl.endsWith("gov.cn") || szUrl.endsWith("org"));
		// return (szUrl.endsWith(".gov.cn") );
//		return (szUrl.endsWith(szUrlEd) );
	}
	
	/**
	 * http://www.youdao.com/search?q=site%3Agov.cn&ue=utf8&keyfrom=web.index
	 */
	public  void doYouDao()
	{
		for(int i = 0; i < 10000; i++)
		{
			System.out.println("开始：" + i);
			doOneUrl("http://www.youdao.com/search?q=site%3A" + szUrlEd + "&start=" + i + "&ue=utf8&keyfrom=web.page2&lq=site%3A" + szUrlEd + "&timesort=0", false);
		}
	}
	
	

	/**
	 * 获取连接信息
	 * 
	 * @param s
	 * @param pattern
	 * @param list
	 */
	public static void doPattern(String s, String pattern, List list)
	{
		Pattern p = Pattern.compile(pattern, Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(s);
		Map<String, String> m1 = new HashMap<String, String>();
		String szUrl;
		int i = 0;// http://
		while (m.find())
		{
			szUrl = m.group(1);
			i = szUrl.indexOf("/", 7);
			if (-1 < i)
				szUrl = szUrl.substring(0, i);
			if (!m1.containsKey(szUrl))
			{
				if (isZfUrl(szUrl))
				{
					list.add(szUrl);
					m1.put(szUrl, "1");
				}
			}
		}
	}

	/**
	 * 插入一条数据，如果已经存在就不插入了
	 * 
	 * @param mP1
	 */
	public void doOneInsert(Map<String, Object> mP1)
	{
		TreeMap<String, Object> m = super.querySQL("select * from zfwebserver where url='" + mP1.get("url") + "'");
		if (null != m && 0 < m.size())
			return;

		TreeMap<String, Object> mP = new TreeMap<String, Object>();
		mP.put("cjrq", super.getTime());
		// 0 表示未提取下一层
		mP.put("getnext", new Integer(0));

		doGetIpMac(mP1);
		mP.putAll(mP1);
		System.out.println("start: " + mP.get("url"));
		if (1 == insertTable("zfwebserver", "url='{url}'", mP))
			System.out.println("Ok: " + mP.get("url"));
		else
		{
			System.err.println("err 插入失败: " + mP.get("url"));
//			update("update zfwebserver set servername='" + mP.get("servername") + "',title='" + 
//				mP.get("title")+ "',devlg='" + mP.get("devlg") + "' where url='" +mP.get("url")  + "'");
		}
	}

	/**
	 * 获取ip、mac、osname
	 * 
	 * @param mP1
	 */
	private void doGetIpMac(Map<String, Object> mP1)
	{
		;/*
		 * `getnext` INT NULL COMMENT '是否分析过第二层连接', `ip` VARCHAR(15) NULL, `mac`
		 * VARCHAR(50) NULL, `osname` VARCHAR(245) NULL,
		 */
	}
	
	/**
	 * SELECT * FROM mydb.zfwebserver where substr(cjrq, 1, 10) = '2016-08-25' and title is null;
select * from mydb.zfwebserver where title is null and url like '%>%';
delete from mydb.zfwebserver where title is null and url like '%満鏋勭紪鍒%';
	 * 处理单个url
	 * @param url
	 */
	public void doOneUrl(String url, boolean bFstC)
	{
		if (bFstC && !(isZfUrl(url)))
			return ;
		try
		{
			final Map<String, String> headers = new HashMap<String, String>();// 请求头
			final String sTxt = GetZFWebServer.getUrlText(url, headers);
			final TreeMap<String, Object> mP = new TreeMap<String, Object>();
            doGetTitle(sTxt, mP, headers, url);
            mP.put("cjrq", super.getTime());
            mP.put("getnext", new Integer(3));
            String []a = new String[mP.size()];
            super.update("zfwebserver", "url='" + url + "'", mP, a = mP.keySet().toArray(a));
            System.out.println("Ok: " + mP.get("title") +  " " +  url);
			GetZFWebServer.doPattern(sTxt, "(http:\\/\\/[^\"'\\?\\s#><\\n\\r]+)", new ArrayList()
			{
				public boolean add(Object s)
				{
					String k = String.valueOf(s);
					if (
							-1 < k.indexOf(".w3.org") || 
							k.endsWith(".js") || k.endsWith(".jpg") || k.endsWith(".css")
					        || k.endsWith(".cab") || k.endsWith(".png") || k.endsWith(".swf") || k.endsWith(".zip"))
					{
						return true;
					}
					mP.clear();
					mP.put("url", s);
					doOneInsert(mP);
					return true;
				}
			});
		} catch (Throwable e)
		{
			if(e instanceof UnknownHostException)
			{
				super.delete("delete from zfwebserver where url='" + url + "'");
				System.out.println("删除： " + url);
			}
			else 
			{
				super.update("update  zfwebserver set getnext=4 where url='" + url + "'");
			}
		}
//		update("update zfwebserver set getnext=1 where getnext=0 and url='" + url + "'");
	}
	
	/**<pre>
	 * 获取
	 * 1、标题
	 * 2、server
	 * 3、
	 * </pre>
	 */
	public void doGetTitle(String sTxt, Map<String, Object> mP, Map<String, String> headers1, String url)
	{
		if(null != sTxt)
		{
			// <title>12316农业综合信息服务平台</title>
			String t = getPatternStr("<title|TITLE>\\s*([^<]*?)\\s*<\\/title|TITLE>", sTxt);
			if(null != t)
			{
				t = t.trim();
//				System.out.println(t);
				if(0 < t.length() && -1 == t.indexOf("升级维护") && -1 == t.indexOf("建设中"))
				{
					mP.put("title", t);
					if(200 < t.length())
						System.out.println(t);
				}
			}
			else
			{
				System.out.println(sTxt);
			}
		}
		
		String szK = "Server";
		if(headers1.containsKey(szK))
			mP.put("servername", String.valueOf(headers1.get(szK)).trim());
		if(headers1.containsKey(szK = "X-Powered-By"))
		{
			String szBy = headers1.get(szK);
			if(headers1.containsKey(szK = "X-AspNet-Version"))
				szBy += " " + headers1.get(szK);
			mP.put("devlg", szBy);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final GetZFWebServer gws = new GetZFWebServer();
		gws.useMysql();
		gws.hvLastScan = false;
//		gws.doOneUrl("http://www.chenghua.gov.cn/", false);
//		if(true)
//		{
//			gws.query("select url from zfwebserver ", new ArrayList<TreeMap<String, Object>>()
//					{
//						public boolean add(TreeMap<String, Object> data)
//						{
//							String url1 = String.valueOf(data.get("url"));
//							url1 = url1.replaceAll("^.*?\\/\\/|\\/.*$", "");
//							IConst.writeAppendFile("/Users/xiatian/safe/govCn.txt", url1 + "\n");
//							System.out.println(url1);
//							return true;
//						}
//					});
//			return;
//		}
		
		// "http://www.gov.cn/"
		// "http://www.chengdu.gov.cn/organization.shtml"
		final MyExecutors mec = new MyExecutors();
		mec.add(
//				new Runnable()
//				{
//					public void run()
//					{
//						gws.doYouDao();
//					}}
//				,
				new Runnable()
		{
			public void run()
			{
//				while (true)
				{
					gws.query("select url,title from zfwebserver where title is null and  getnext >= 2" 
//							+ " where title is null and getnext != 2" 
//							+ " and substr(cjrq, 1, 10) != '2016-08-25'" 
//							+ " where getnext=0 and url like '%.edu.cn'"
							, new ArrayList<TreeMap<String, Object>>()
					{
						public boolean add(TreeMap<String, Object> data)
						{
							final String url1 = String.valueOf(data.get("url"));
//							mec.add(
//									new Runnable()
//									{
//										public void run()
//										{
							System.out.println(url1);
											gws.doOneUrl(url1, true);
//										}}
//									);
//							
							return true;
						}
					});
					try
					{
						Thread.sleep(222);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
//				,
//				new Runnable()
//		{
//			public void run()
//			{
//				while (true)
//				{
//					// getnext = 0 and
//					gws.query("select url,title from zfwebserver where getnext = 3 and title is null and  substr(cjrq, 1, 10) = '2016-08-26'" 
//							, new ArrayList<TreeMap<String, Object>>()
//					{
//						public boolean add(TreeMap<String, Object> data)
//						{
//							String url1 = String.valueOf(data.get("url"));
//							gws.doOneUrl(url1, true);
//							return true;
//						}
//					});
//					try
//					{
//						Thread.sleep(222);
//					} catch (InterruptedException e)
//					{
//						e.printStackTrace();
//					}
//				}
//			}
//		}
				);
	}

}
