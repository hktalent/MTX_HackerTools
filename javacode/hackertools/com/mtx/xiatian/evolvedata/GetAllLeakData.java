package com.mtx.xiatian.evolvedata;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mtx.core.common.IConst;
import com.mtx.xiatian.hacker.CommonTools;
import com.mtx.xiatian.hacker.MyExecutors;

/**
 * 获取补天漏洞数据 CREATE TABLE `mydb`.`leak` ( `id` VARCHAR(30) NOT NULL, `leakdes`
 * VARCHAR(500) NULL, `url` VARCHAR(200) NULL , `changshang` VARCHAR(400) NULL,
 * `tjsj` VARCHAR(30) NULL, `leaklevel` VARCHAR(20) NULL, `department`
 * VARCHAR(80) NULL, `confirm` VARCHAR(50) NULL, PRIMARY KEY (`id`), UNIQUE
 * INDEX `id_UNIQUE` (`id` ASC));
 * 
 * 起点：http://loudong.360.cn/vul/list
 * 
 * @author xiatian
 * 
 */
public class GetAllLeakData extends CommonTools
{

	/**
	 * 判断是否已经添加
	 * 
	 * @param url
	 * @return
	 */
	public  boolean haveLeak(String url)
	{
		List lstq = null;
		lstq = MyQDataInfo.sdb.queryListForPageMySql("leak", 0L, 3L, " and url = '" + url + "' ");
		if (null != lstq && 0 < lstq.size())
			return true;
		return false;
	}

	/**
	 * 解析当前html中页的数据
	 * 
	 * @param s
	 */
	public  boolean parsePageHtml(String s)
	{
		if (null == s)
			return false;
		int n = -1;
		// 未找到数据起点标志
		if (-1 == (n = s.indexOf("<div class=\"ld-con-box\">")))
			return false;
		s = s.substring(n);
		n = s.indexOf("<!-- begin content -->");
		if (-1 < n)
			s = s.substring(0, n);

		Pattern p = Pattern.compile("<a.*?href=\"([^\"]+)\".*?>([^<]+)<\\/a>", Pattern.MULTILINE);
		Matcher m = p.matcher(s), m1;
		String szStr = "", id, url, tjsj;

		// 提取提交时间、官方评级
		Pattern p1 = Pattern.compile("<li><dl>提交时间：<\\/dl><dt>([^<]+)<\\/dt><\\/li>\\s*<li><dl>官方评级：</dl><dt>([^<]+)<\\/dt><\\/li>",
		        Pattern.MULTILINE);
		Pattern p2 = Pattern.compile("<dl>漏洞厂商：<\\/dl>\\s*<dt>\\s*<a href=\"[^\"]\">([^<]+)<\\/a>", Pattern.MULTILINE);
		Map<String, Object> mParm = new HashMap<String, Object>();

		while (m.find())
		{
			url = "http://loudong.360.cn" + m.group(1);
			if (haveLeak(url))
				continue;
			mParm.put("leakdes", m.group(2));
			id = m.group(1);
			if (-1 < id.indexOf("list/page") || -1 == id.indexOf("/QTVA-"))
				continue;
			if (-1 < (n = id.lastIndexOf('/')))
				id = id.substring(n + 1);
			if (-1 < (n = id.lastIndexOf('.')))
				id = id.substring(0, n);
			mParm.put("id", id);
			szStr = AC01Info.getUrlStr(url);
			if (null == szStr)
				return false;
			mParm.put("url", url);
			if (-1 < (n = szStr.lastIndexOf("<div class=\"ld-vul-level-tips\">")))
				szStr = szStr.substring(n);
			if (-1 < (n = szStr.lastIndexOf("<div style=\"clear:both;\"></div>")))
				szStr = szStr.substring(0, n);
			m1 = p1.matcher(szStr);
			if (m1.find())
			{
				tjsj = m1.group(1);
				mParm.put("tjsj", tjsj);
				mParm.put("leaklevel", m1.group(2));
			}
			m1 = p2.matcher(szStr);
			if (m1.find())
			{
				mParm.put("changshang", m1.group(1));
			}
			try
			{
				MyQDataInfo.sdb.insert("leak", mParm);
			} catch (Exception e)
			{
				if (-1 < e.toString().indexOf("Duplicate entry"))
					;
				e.printStackTrace();
			}
		}
		return true;
	}

	public  DateFormat	format	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public  String	 cacheQFile	= "/Users/xiatian/Downloads/cacheLeakUrlFile.txt";

	/**
	 * 处理url文件
	 */
	public  void doAllURLFile()
	{
		String s = "http://loudong.360.cn/vul/list/page/1";
		String s1 = MyQDataInfo.getFile(new File(cacheQFile));
		if (null != s1)
			s = s1;
		// if(
		parsePageHtml(AC01Info.getUrlStr(s));
		// )
		{
			s = s.substring(s.lastIndexOf('/') + 1);
			s = s.replaceAll("[^\\d]", "");
			Integer i = Integer.valueOf(s) + 1;

			MyQDataInfo.processUrlFile(cacheQFile, "http://loudong.360.cn/vul/list/page/" + i);
			doAllURLFile();
		}
	}

	/**
	 * id、leakdes、url、changshang、tjsj、leaklevel、department、confirm
	 * 
	 * @param szUrl
	 * @return
	 */
	private  boolean doOneWooyunUrl(String szUrl)
	{
		boolean bRst = false;
		String s = AC01Info.getUrlStr(szUrl);
		if (null == s || 0 == (s = s.trim()).length())
		{
			insertErrUrl(myWYtype, szUrl);
			return bRst;
		}
		String sK = "<table class=\"listTable\">";
		int n = s.indexOf(sK);
		if (-1 == n)
			return bRst;
		s = s.substring(n + sK.length());
		sK = "<p class=\"page\">";
		n = s.indexOf(sK);
		if (-1 == n)
			return bRst;
		s = s.substring(0, n);

		TreeMap<String, Object> m = new TreeMap<String, Object>();
		String[] a = s.split("<\\/tr>");
		for (String x : a)
		{
			m.clear();
			s = getPatternStr("<td>\\s*<a\\s*href=\"\\/bugs\\/([^\"]+)\">", x);
			if (isEmpty(s))
				continue;
			m.put("id", s);

			s = getPatternStr("<td>\\s*<a\\s*href=\"\\/bugs\\/[^\"]+\">\\s*([^<]+)\\s*<", x);
			if (isEmpty(s))
				continue;
			// 可以加上其他附加信息
			m.put("leakdes", s);

			s = getPatternStr("<td>\\s*<a\\s*href=\"(\\/bugs\\/[^\"]+)\">", x);
			if (isEmpty(s))
				continue;
			szUrl = urlWY + s;
			m.put("url", szUrl);
			// 下载并分析 ：szUrl
			s = AC01Info.getUrlStr(szUrl);
			x = getPatternStr("<h3 class='wybug_date'>提交时间：\\s*([^<]+)<\\/h3>", s);
			if (isEmpty(x))
				continue;
			m.put("tjsj", x);

			x = getPatternStr("<h3\\s*class='wybug_corp'>\\s*相关厂商：\\s*<a\\s*href=\"http:\\/\\/www\\.wooyun\\.org\\/corps\\/([^\"]+\"\\s*>\\s*[^<]+)", s);
			x = x.replaceFirst("\">\\s*", "");
			if (isEmpty(x))
				continue;
			m.put("changshang", x);
			x = getPatternStr("<h3 class='wybug_level'>危害等级：\\s*([^<]+)<\\/h3>", s);
			if (isEmpty(x))
				continue;
			m.put("leaklevel", x);

			x = getPatternStr("<h3 class='wybug_status'>漏洞状态：\\s*([^<]+)<\\/h3>", s);
			if (isEmpty(x))
				continue;
			m.put("department", x);

			if (1 != insertTable(leak, "id='{id}' and url='{url}'", m))
				continue;
		}
		return true;
	}

	/**
	 * 总共：81903条
	 * http://www.wooyun.org/bugs/page/2 存储乌云漏洞信息
	 * id、leakdes、url、changshang、tjsj、leaklevel、department、confirm
	 */
	public  void getWooyunData()
	{
		String url = urlWY, p = url + "/bugs/page/", szUrl;
		int n = 81549, j = n / 20 + (0 == n % 20 ? 0 : 1) + 200;
		// getLastPos(myWYtype, 1)
		for (int i = 1; i < j; i++)
		{
			if (doOneWooyunUrl(szUrl = p + i))
			{
				insertLastPosl(myWYtype, i);
				// 删除err表
				delete("delete from " + errUrls + " where url='" + szUrl + "' and type='" + myWYtype + "'");
				info("Ok: ", szUrl);
			} else
			{
				insertErrUrl(myWYtype, szUrl);
				continue;
			}

		}
	}
	
	/**
	 * 获取漏洞数据总数
	 * info(GetAllLeakData.getLeakCnt());
	 * @return
	 */
	public  int getLeakCnt()
	{
		Map <String,Object> m = querySQL("select count(1) as cnt from leak", false, null);
		return Integer.parseInt(String.valueOf(m.get("cnt")));
	}
	
	public void queryWord()
	{
		queryForyWord(new ArrayList<TreeMap<String, Object>>()
				{
					private static final long	serialVersionUID	= -7061840719725188173L;
					public boolean add(TreeMap<String, Object> m)
					{
							info(m.toString() + "\n");
						return true;
					}
				}, new String[]{"贵州","贵阳","四川", "成都", "重庆", "都江堰", "温江", "绵阳", "吉林", "太原","西安","陕西","黄冈","咸宁","荆州","德阳","天津","武汉","云南","昆明","内江"}, new String[]{"人社", "社保", "医疗", "劳动", "医保","单位", "银海"});
	}
	/**<pre>
	 * 1、地名、区域 关键字
	 * 2、支持多个关键字，例如{"成都", "雅安"}， {"社保", "公积金"}，{"公民"}
	 * 
	 * </pre>
	 * @param list
	 * @param aD  
	 */
	public  void queryForyWord(final List<TreeMap<String, Object>> list, String []...aD)
	{
		StringBuffer buf = new StringBuffer("SELECT tjsj,leaklevel,leakdes,url FROM mydb.leak where ");
		int n = 0, j = 0;;
		for(String []a:aD)
		{
			n = 0;
			if(0 < j)buf.append(" and ");
			buf.append("(");
			for(String s:a)
			{
				if(0 < n)buf.append(" or ");
				buf.append(" leakdes LIKE '%" + s + "%' ");
				n++;
			}
			j++;
			buf.append(")");
		}
		querySQL(buf.toString(), false, list);
	}

	/**
	 * <pre>
	 * 1、两个线程
	 * 2、一个负责处理曾经为成功的url
	 * 3、一个负责从上次端点位置开始处理
	 * </pre>
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.setProperty("java.net.useSystemProxies", "true");
		IConst.bMyDebug = IConst.g_bDebug = true;
		
		final GetAllLeakData gald = new GetAllLeakData();
		gald.useMysql();
//		gald.delete("delete from " + lastPos + " where type='" + myWYtype + "'");
//		gald.querySQL("select count(1) from " + leak + " where url like '%wooyun%'", true, null);
//		gald.querySQL("select count(1) from " + leak + "", true, null);
//		gald.getWooyunData();
//		if(true)return;
//		gald.queryWord();
//		if(true)return;
		MyExecutors.getInstance().add(
				new Runnable()
		{
			public void run()
			{
				gald.getWooyunData();
			}
		}, 
//		new Runnable()
//		{
//			public void run()
//			{
//				gald.getErrUrls(myWYtype, new ArrayList<String>()
//				{
//					private static final long	serialVersionUID	= -6281929388484391648L;
//					public boolean add(String s)
//					{
//						try
//						{
//							gald.doOneWooyunUrl(s);
//							Thread.sleep(133);// 3000
//						} catch (Exception e)
//						{
//							e.printStackTrace();
//						}
//						return true;
//					}
//				});
//			}
//		},
//		new Runnable()
//		{
//			public void run()
//			{
//				gald.getErrUrls(leak, new ArrayList<String>()
//				{
//					private static final long	serialVersionUID	= -6281929388484391648L;
//
//					public boolean add(String s)
//					{
//						try
//						{
//							gald.info("处理lastErr：", s);
//							gald.parsePageHtml(AC01Info.getUrlStr(s));
//							Thread.sleep(1000);// 3000
//						} catch (Exception e)
//						{
//							e.printStackTrace();
//						}
//						return true;
//					}
//				});
//			}
//		}, 
				new Runnable()
		{
			public void run()
			{
				String s;
				// gald.getLastPos(leak, 1)
				for (int i = 1; i < 10000; i++)
				{
					s = "http://loudong.360.cn/vul/list/page/" + i;
					gald.info("处理：", s);
					if (!gald.parsePageHtml(AC01Info.getUrlStr(s)))
					{
						gald.insertErrUrl(leak, s);
					} else
					{
						gald.insertLastPosl(leak, i);
						// 删除err表
						gald.delete("delete from " + errUrls + " where url='" + s + "' and type='" + leak + "'");
					}
					try
					{
						Thread.sleep(3000);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}

}
