package com.mtx.xiatian.evolvedata;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mtx.face.IMyResultHandler;

public class AC01Info {

	/**
	 * 电话信息
	 * @param szTel
	 * @param irh
	 */
	public static void getTelInfo(Long nTel, IMyResultHandler irh)
	{
		// 短信
		MyQDataInfo.sdb.queryForList("SELECT fssj FROM mydb.duanxin where zjh = '" + nTel + "' or bjh = '" + nTel + "'", irh);
	}
	
	/** aac003,aac002,aac007,yae098,aae006
	 * new IMyResultHandler()
		{
			public void doResult(Map<String, Object> m) 
			{
				if(null == m.get("aac002") || 15 > String.valueOf(m.get("aac002")).length())return;
				// aac003,aac002,aac007,yae098,aae006
				System.out.println("电话：" + m.get("yae098") + "; 身份证：" + m.get("aac002")+ "; 生日：" + String.valueOf(m.get("aac006")).substring(0, 10));
			}}
	 * @param szXm
	 * @param irh
	 */
	public static  void getAc01Info(String szXm, IMyResultHandler irh, int nAgeMin, int nAgeMax)
	{
		// queryForListByTab
		// 姓名、年龄、身份证、电话
		MyQDataInfo.sdb.queryForList("select * from (" +
"select aac001,yae098,aac006,aac002, (year(curdate()) - substring(aac002,7,4))  as nl from ac01 where aac004='1' and aac003 = '" + szXm + "' and aac002  is not null "+ 
 " group by aac001,yae098,aac006,aac002,nl)  k where k.nl  BETWEEN " + nAgeMin + " and " + nAgeMax + " order by k.nl desc", irh);
		
		// 家庭住址
		MyQDataInfo.sdb.queryForList("select yhdz from cdgrzfxx where yhxm = '" + szXm + "'", irh);
	}
	
	/**
	 * 获取20~40岁之间的人
	 * @param szXm
	 * @param irh
	 */
	public static  void getAc01Info(String szXm, IMyResultHandler irh)
	{
		getAc01Info(szXm, irh, 20, 40);
	}
	
	
	/**
	 * map数据转换到字符串
	 * @param m
	 * @param a
	 * @return
	 */
	public static String getMapToString(Map m, String ...a)
	{
		StringBuffer sb = new StringBuffer();
		Object o;
		String szK = null;
		for(int i = 0, j = a.length; i < j; i += 2)
		{
			o = m.get(szK = a[i + 1]);
			if(null == o)continue;
			if("nl".equals(szK))
				o = String.valueOf(o).replaceAll("\\.0*", "");
			else if("aac006".equals(szK))
				o = String.valueOf(o).substring(0, 10);
			if(null != a[i])
				sb.append(a[i]).append(": ");
			sb.append(o).append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * 已经下载处理过的qq
	 */
	public static Map mYjclQQ = null;
	static{
		Map m = null;// (Map)MyQDataInfo.readObject(MyHashMap.cacheFileName);
		if(null != m)
			mYjclQQ = m;
		else mYjclQQ = new MyHashMap();
	}
	
	/**
	 * 获取url内容
	 */
	public static synchronized  String getUrlStr(String url1)
	{
		url1 = url1.trim();
		
//		String qq = url1.substring(url1.lastIndexOf("/") + 1, url1.lastIndexOf("."));
//		if(null != mYjclQQ.get(qq))
//			return null;
		
//		boolean bHvUrl = false;
//		final List <Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
//		// 判断存在否？
//		MyQDataInfo.sdb.queryForList("SELECT url FROM url where url='" + url1 + "'", new IMyResultHandler(){
//					public void doResult(Map<String, Object> m) {
//						lst.add(m);
//					}});
//		
//		MyQDataInfo.sdb.queryForList("SELECT qq FROM qq where qq='" + qq + "'", new IMyResultHandler(){
//			public void doResult(Map<String, Object> m) {
//				lst.add(m);
//			}});
//		bHvUrl = 0 < lst.size();
//		if(1 < lst.size())
//		{
//			lst.clear();
////			MyQDataInfo.log("已经处理过, 退出：", url1);
//			return null;
//		}
//		lst.clear();
//	    System.out.println(url1);
		URL url = null;
		HttpURLConnection conn = null;
		InputStreamReader isr = null;
		try
		{
			StringBuffer sb  = new StringBuffer(); 
			url = new URL(url1);
			conn = (HttpURLConnection)url.openConnection();
//			conn.setReadTimeout(1333);
			conn.setRequestProperty("Cookie", "__cfduid=d1a28b3474a3c8120543a189eeda38fb21424764205; ASPSESSIONIDSCCTRTBB=BLPALBLBBPFOLCKEJDANCBBG; ASPSESSIONIDQABTRQCB=LJCDEMGCPCNOLPBAGENGBILI; ASPSESSIONIDSACRTTBA=LKPEBIDDNICOLPPNNHJLLKNE; ASPSESSIONIDQACTRTBA=AMKFMPHANCDOMOMALLMPDGAI; ASPSESSIONIDSAATTSBB=KDOFKLEBMDCANMBHNHBLBKIF; ASPSESSIONIDQAATRRCA=EHIABFKBLHJCFMAJBEKAOMDA; ASPSESSIONIDQCATSRAA=KCKFJBPBEGANOCHHCHNDNPNI; ASPSESSIONIDSAAQRRCB=BFNMOGACJBOEDBJBCAMPOGPM; ASPSESSIONIDSACRQQCB=PJINPFJCGFLKLOMLJAGAGCON; ASPSESSIONIDSCCQSTAA=FNIHBCNCIEJMJPEEDPKCBJAA; ASPSESSIONIDAAACSATC=EHLDDOIANENPMJKPKCDGHFMM; ASPSESSIONIDCCCCSATD=FDJNGANAJEFDENDDLPMLFFFA; ASPSESSIONIDCQQADBBS=EJMLENMCFJECAECPGFLMBLGK; ASPSESSIONIDCSQDBABT=JBFIHBDDJLGMBMNJGNGEGCKL; ASPSESSIONIDCQSDADAS=BPCGFNPDNHEAFOKGMAMKDCBB; ASPSESSIONIDSACSBRCB=FCANCEKBKCDEPFPCFJKILDHM; ASPSESSIONIDQCBTCRDA=FNDOELLDIEIECGBMFPLCMAFK; ASPSESSIONIDQCBRBRCB=HJMAMDABPBHCAOPLIPNCNNBP; ASPSESSIONIDSABQARCB=IMDBDKLBJFAFKIMBFFKAMIIB; CNZZDATA1000125360=1746150813-1424763085-http%253A%252F%252Fwww.gfsoso.com%252F%7C1426853398");
			conn.setRequestProperty("DNT","1");
			conn.setRequestProperty("Cache-Control","max-age=0");
			conn.setRequestProperty("Referer",url1);
			conn.setRequestProperty("Host", "qun.594sgk.com");
			conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36");
//			conn.setInstanceFollowRedirects(true);
	        conn.setDoInput(true);
	        conn.setDoOutput(false);
	        String line;
	        InputStream isN = null;
	        isN = conn.getInputStream();
	        isr = new InputStreamReader(isN,"UTF-8");
	        BufferedReader reader = new BufferedReader(isr);
	        while ((line = reader.readLine()) != null) 
	        {
	          sb.append(line);
	        }
	        reader.close();
	        isN.close();
	        
	        // 避免重复获取
//	        mYjclQQ.put(qq, "1");
	        MyQDataInfo.writeObject("MyHashMap.bin", mYjclQQ);
	        Map <String, Object> parm = new HashMap<String, Object>();
	        parm.put("url", url1);
//			MyQDataInfo.sdb.insert("url", parm);
//			MyQDataInfo.processUrlFile(MyQDataInfo.cacheQFile, url1);
			
//			MyQDataInfo.log("下载ok，开始处理：", url1);
	        return sb.toString();
		}catch(Exception e)
		{
//			MyQDataInfo.pool.addRunnable(url1);
			MyQDataInfo.processErrUrlFile(url1);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 分析页面得到QQ信息列表
	 * 群、qq连接
	 * http://qun.594sgk.com/qq/3135773.html
	 * 
	 * @param s
	 * @return
	 */
	public static List<Map<String,String>> getQqList(String s, IDoOneQQ one)
	{
		List<Map<String,String>> lst = null;
		Pattern p = Pattern.compile("<td><a href=\"[^\"]*?\">(\\d+)<\\/a><\\/td>\\s*<td>([^<]*?)<\\/td>\\s*<td>([^<]*?)<\\/td>\\s*<td>([^<]*?)<\\/td>\\s*<td><a href=\"[^\"]*?\">(\\d+)<\\/a><\\/td>\\s*<td>[^<]*?<\\/td>",Pattern.MULTILINE);
		
		Matcher m1 = p.matcher(s);
		Map <String,String>m = new HashMap <String,String>();
		
		if(null == one)lst = new ArrayList<Map<String,String>>();
		
		int i = 0;
		while(m1.find())
		{
			if(null == one)m = new HashMap <String,String>();
			i = 1;
			m.put("qq", m1.group(i++));
			m.put("xm", m1.group(i++));
			m.put("xb", m1.group(i++));
			m.put("nl", m1.group(i++));
			m.put("qun", m1.group(i++));
			if(null != one)
			{
				one.doOneQQ(m);
				m.clear();
			}
			else lst.add(m);
		}
	    m1 = null;
		return lst;
	}
	
	/**
	 * 单页QQ信息分析处理
	 * @param sb
	 * @param szStr
	 * @param szXm
	 * @param szQq
	 * @param mUrlPage 避免分页数据重复处理
	 */
	public static void doPageQQInfo(MyStringBuffer sb, String szStr, String szXm, String szQq, Map<String,String> mUrlPage)
	{
		if(null == mUrlPage)mUrlPage = new HashMap<String,String>();
		String line = szStr, szXmTm = null;
		// 处理google搜索结果:http:\/\/qun.594sgk.com\/qq\/871393.html
        Pattern p = Pattern.compile("(http:\\\\/\\\\/qun\\.594sgk\\.com\\\\/qq\\\\/(\\d+)\\.html)",Pattern.MULTILINE);
        Matcher m = p.matcher(line);
        
        p = Pattern.compile("<td><a href=\"\\/qq\\/\\d+\\.html\">(\\d+)<\\/a><\\/td>\\s*<td>([^<]+)<\\/td>\\s*<td>([^<]+)<\\/td>\\s*<td>([^<]+)<\\/td>\\s*<td><a href=\"\\/qq\\/\\d+\\.html\">(\\d+)<\\/a><\\/td>\\s*<td>594sgk.com<\\/td>",Pattern.MULTILINE);
        
        while(m.find())
        {
        	    // szQQ = m.group(2) 当前搜索的QQ
        		line = m.group(1).replaceAll("\\\\", "");
        		line = getUrlStr(line);
        		
        		List<Map<String,String>> lst = getQqList(line, null);
        		for(Map<String,String> m1:lst)
        		{
        			szXmTm = m1.get("xm");
	        		if((null != szXm && ( 
	        				szXm.equals(szXmTm) || 0 < szXm.indexOf(szXmTm) || 0 < szXmTm.indexOf(szXm)
	        				)) || (null != szQq && szQq.endsWith(m1.get("qq"))))
	        		{
	        			sb.append("用名：").append(szXmTm)
	        			.append(", QQ：").append(m1.get("qq"))
	        			.append(", 性别：").append(m1.get("xb"))
	        			.append(", 年龄：").append(m1.get("nl"))
	        			.append(", QQ群：").append(m1.get("qun")).append("\n");
	        		}
        		}
        }
        
        // 分页分析
		p = Pattern.compile("(\\/\\?q=%22" + szXm + ".*?site%3Aqun.594sgk.com&pn=\\d*)",Pattern.MULTILINE);
		int iI = szStr.indexOf("<div id=\\\"pagi\\\"");
		if(-1 < iI)
		{
			szStr = szStr.substring(iI); 
		    m = p.matcher(szStr);
		    List <String>list = new ArrayList<String>();
		    while(m.find())
		    {
		    		list.add(m.group(1));
		    }
		    p = null;
		    m = null;
		    for (String k : list) 
		    {
			    	if(null != mUrlPage.get(k))continue;
		    		mUrlPage.put(k, "1");
		    		doPageQQInfo(sb,  getUrlStr("http://www.gfsoso.com" + k),  szXm,  szQq,  mUrlPage);
			}
		}
	   
	}
	
	/**
	 * 获取可能的QQ信息
	 * @param szXm
	 * @param szQq
	 */
	public static String getQQInfo(String szXm, String szQq)
	{
		String url = null;
		String line = null, szXmTm = null;
		
		MyStringBuffer sb = new MyStringBuffer();
		try
		{
			szXmTm = java.net.URLEncoder.encode(szXm ,"UTF-8");
			url = "http://www.gfsoso.com/?t=1&q=%22" + szXmTm+ "%22+%22" + szQq + "%22+site%3Aqun.594sgk.com";
			line = getUrlStr(url);
			doPageQQInfo(sb,line,szXmTm,szQq, null);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/**
	 * 家庭住址，短信，通话记录
	 */
	public static Boolean bJtdz = true, bDuanxin = true, bThjl = true;
	/**
	 * 获取所有信息
	 * @param szXm
	 * @param szTel
	 */
	public static void getAllInfo(String szXm, String szTel)
	{
		szTel = szTel.replaceAll("\\-", "");
		final Map m1 = new HashMap();
		final StringBuffer sb = new StringBuffer();
		bThjl = bDuanxin = bJtdz = true;
		IMyResultHandler im = null;
		AC01Info.getAc01Info(szXm, im = new IMyResultHandler()
		{
			public void doResult(Map<String, Object> m) 
			{
				// 个人基本信息
				if(null != m.get("aac002") )
				{
					if(null == m.get("aac002") || 15 > String.valueOf(m.get("aac002")).length() || null != m1.get(m.get("aac002")))return;
					String szT = null;
					// 防止重复处理
					m1.put(m.get("aac002"), "1");
//					System.out.println(m.get("aac001"));
					sb.append(szT = getMapToString(m, "年龄", "nl", "身份证", "aac002", "生日", "aac006", "电话", "yae098"));
					// aac003,aac002,aac007,yae098,aae006
					if(null != m.get("ae098") && null == m1.get(m.get("ae098")))
					{
						Long lnTel = Long.valueOf(String.valueOf(m.get("ae098")));
						// 防止重复处理
						m1.put(lnTel, "1");
						AC01Info.getTelInfo(lnTel, this);
					}
				}
				// 家庭住址信息
				else if(null != m.get("yhdz"))
				{
					if(bJtdz)
					{
						bJtdz = false;
						sb.append("家庭住址：\n");
					}
					sb.append(getMapToString(m, null, "yhdz"));
				}
				// 短信信息
				else if(null != m.get("fssj"))
				{
					if(bDuanxin)
					{
						bDuanxin = false;
						sb.append("短信记录：\n");
					}
					sb.append(getMapToString(m, null, "fssj"));
				}
				// 通话记录
				else if(null != m.get("dfhm") && null == m.get("mysc") )
				{
					if(bThjl)
					{
						bThjl = false;
						sb.append("通话记录：\n");
					}
					sb.append(getMapToString(m, "姓名", "xm", "电话号码", "dfhm", "通话时间", "thqssj", "通话时长","sc"));
				}
				// 通话次数、总时间
				else if(null != m.get("mysc") && null != m.get("thcs"))
				{
					sb.append(getMapToString(m, "电话","dfhm", "通话次数", "thcs", "通话总秒数", "mysc"));
				}
			}});
		
		// http://qun.594sgk.com/qq/82603954.html
		// http://www.gfsoso.com/?q=%22单词%22+site%3Aqun.594sgk.com
		// 可能的性能和QQ号码
		
		// 通话记录
		MyQDataInfo.sdb.queryForList("SELECT thqssj,sc,dfhm,xm FROM mydb.thjl where dfhm='" + szTel + "' or xm like '" + szXm + "%'", im);
		// 通话时长
		MyQDataInfo.sdb.queryForList("SELECT  "+ 
     "sum(left(sc,LOCATE('分',sc) - 1)*60 + substring(sc,LOCATE('分',sc) + 1, LOCATE('秒',sc))) as mysc,count(1) as thcs,"+
  "dfhm FROM mydb.thjl where  dfhm = '" + szTel + "' group by dfhm order by mysc desc", im);
		
		getTelInfo(Long.valueOf(szTel), im);
		// 网上QQ信息
		sb.append(getQQInfo(szXm, ""));
		System.out.println(sb.toString());
	}
}
