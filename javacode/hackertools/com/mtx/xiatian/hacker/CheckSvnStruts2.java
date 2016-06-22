package com.mtx.xiatian.hacker;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mtx.xiatian.evolvedata.MyQDataInfo;

/**
 * svn遍历:
 * 1、strtuts2升级情况，url
 * 2、检查commons-collections-*.jar升级情况
 * 3、jdbc.properties 路径：url
 * 4、字段：部门名称（）、项目编号(JY15MD002SK)、url（不能重复）、ver、标志（升级完成标志true、fasle）、类型（strus2、commonsCollections、jdbc）、lastScan(日期)
 * 	css.update("ALTER TABLE svnInfo add ProjectName varchar(400);");
 * @author xiatian
 *
 */
public class CheckSvnStruts2 extends CommonTools
{
	
	/**
	 * 写文件
	 * @param a
	 */
	public static  void writeFile(String ...a)
	{
		StringBuffer buf = new StringBuffer();
		for(String s:a)
		{
			if(null == s)buf.append("\t");
			else buf.append(s);
		}
		TestHashCodeParm.writeFile("/Volumes/MyWork/MyWork/sfTester/noStruts2.txt", buf.toString());
	}
	

//	ProjectInfo pi = new ProjectInfo();
	int nCnt = 0;
	
	/**<pre>
	 * jdbc信息获取：
	 * a、保留注释的，注释的只有ip地址，或者都注释了
	 * 1、下载、分析、记录jdbc连接信息
	 * 2、url：urlid、url、date（最后的更新时间，有变化就更新）
	 * 3、jdbc：urlid、driver、jdbcurl、user、pswd
	 * 按行读取，以第一个等号分割，去除首尾#和空格
	 * 
	 * jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://192.168.10.115:3306/qix1?relaxAutoCommit=true
#jdbc.url=jdbc:mysql://192.168.10.216:3306/qix1?relaxAutoCommit=true
jdbc.username=qix1
jdbc.password=qix1
	 * </pre>
	 * @param url
	 */
	private void doJdbcInfo(String url)
	{
		headers.clear();
		initHead(url);
		sbContent.delete(0, sbContent.length());
		utt.doPost(url, null, null, mParams, headers, sbContent, false, null, null);
		String []a = sbContent.toString().split("\\n");
		TreeMap<String, Object> m1 = new TreeMap<String, Object>();
		// 第一波只处理未注释行
		for(String s:a)
		{
			s = s.trim();
			if(s.startsWith("#"))continue;
			if(null != m1.get("password"))
			{
				insertTable(jdbcInfo, "url='{url}' and jdbcurl='{jdbcurl}'", m1);
				m1.clear();
			}
			m1.put("url", url);
			if(1 < s.indexOf("driver"))
				m1.put("driver", s.substring(s.indexOf('=') + 1).trim());
			else if(-1 < s.indexOf("jdbc.url"))
				m1.put("jdbcurl", s.substring(s.indexOf('=') + 1).trim());
			else if(-1 < s.indexOf("user"))
				m1.put("user", s.substring(s.indexOf('=') + 1).trim());
			else if(-1 < s.indexOf("password"))
				m1.put("password", s.substring(s.indexOf('=') + 1).trim());
		}
		
		if(null != m1.get("jdbcurl"))
		{
			insertTable(jdbcInfo, "url='{url}' and jdbcurl='{jdbcurl}'", m1);
		}
	}
	
	private String tableType = "svnInfo", jdbcInfo = "jdbcInfo";
	/**
	 * struts2:
	 * url
	 * 处理一个文件或者目录
	 * @param szUrl
	 * @param szA
	 */
	public  boolean doOneFile(String szUrl, String szA)
	{
		if(szA.endsWith(".java"))return true;
		// 目录深度遍历
		if(szA.endsWith("/"))
		{
			doGetUrl(szUrl + szA);
			return true;
		}
		// 文件名判断
		String s = szA.toLowerCase();
		
		Matcher m = null;
		String szId = "";
		
		// struts2-core-2.3.4.1.jar
		TreeMap<String, Object> mD = new TreeMap<String, Object>();
		 // 部门
		mD.put("depart", "");
		// 项目编号
		szId = szUrl.substring(szUrl.indexOf("/svn/") + 5);
		szId = szId.substring(0, szId.indexOf("/"));
		mD.put("projectId", szId);
		// url
		mD.put("url", decode(szUrl) + szA);
		// jdbc.properties；
		if(s.endsWith("jdbc.properties") || s.endsWith("sqlMapconfig.properties"))
		{
			mD.put("ver", "");
			mD.put("type", "jdbc");
//			doJdbcInfo(szUrl + szA);
			if(0 == insertTable(tableType, "url='{url}'", mD))
				;
		}
		else if(s.endsWith(".jar"))
		{
			if(-1 < s.indexOf("struts2"))
			{
				m = struts2.matcher(s);
				if(m.find())
				{
					mD.put("ver", m.group(1));
					mD.put("type", "strus2");
				}
			}
			else if(-1 < s.indexOf("commons-collections"))
			{
				String ver = s.substring(s.lastIndexOf('-') + 1);
				ver = ver.substring(0, ver.lastIndexOf('.'));
				mD.put("ver", ver);
				mD.put("type", "commonsCollections");
			}
			if(null != mD.get("type"))
			{
				if(0 == insertTable(tableType, "url='{url}'", mD))
				{
					info("找到，新增失败：", mD.get("url"));
					if(1 == update("update " + tableType + " set ver='" + mD.get("ver") + "',lastScan='" + getTime()+ "' where ver<>'" + mD.get("ver") + "' and url='" + mD.get("url") + "'"))
						info(mD.get("ver"), "成功更新: ", mD.get("url"));
				}
				else info("找到，已经新增加：", mD.get("url"));
			}
			return true;
		}
		return false;
	}
	
	
	public Map<String, String> getHeaders()
    {
    		return headers;
    }

	public StringBuffer getSbContent()
    {
		return sbContent;
    }


	
	/**
	 * 获取一个url的内容
	 * @param szUrl
	 */
	public void  doGetUrl(String szUrl)
	{
		sbContent.delete(0, sbContent.length());
		doPost(szUrl);
		doAList(sbContent, szUrl);
	}
	
	/**
	 * url解码
	 * @param s
	 * @return
	 */
	public String decode(String s)
	{
		try
        {
	        return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e)
        {
        		info(e);
        }
		return s;
	}
	
	/**
	 * url数据获取，数据存入：sbContent
	 * @param url
	 */
	public void doPost(String url)
	{
		String szL = url.toLowerCase();
		if(-1 < szL.indexOf("/web-inf/")
//				 || -1 < szL.indexOf("/javacode/")
				|| -1 < szL.indexOf("/webapp/")
				|| -1 < szL.indexOf("/classes/")
				|| -1 < szL.indexOf("/.")
				|| -1 < szL.indexOf("/5.效果图/")
				|| -1 < szL.indexOf("/4.原型设计/")
				|| -1 < szL.indexOf("/src/org/")
				
				|| -1 < szL.indexOf("/fckeditor/")
//				|| -1 < szL.indexOf("/com/yinhai/")
				
				|| -1 < szL.indexOf(".document/")
				|| -1 < szL.indexOf(".test/")
				|| -1 < szL.indexOf(".design/")
				|| -1 < szL.indexOf(".plan/")
				|| -1 < szL.indexOf(".requirement/")
				|| -1 < szL.indexOf(".control/")
				|| -1 < szL.indexOf(".reference/")
				
				)return;
//		info(decode(url));
		headers.clear();
		initHead(url);
		sbContent.delete(0, sbContent.length());
		utt.doPost(url, null, null, mParams, headers, sbContent, false, null, null);
	}
	
	public void initHead(String url)
	{
//		headers.put("Cookie", "JSESSIONID=D517551DB9E3FBF4045E997AA3D6B9DC");
		headers.put("Authorization", "Basic cWlTeXMyMDEzX3VzZXI6WCpRMkBpfiYpX1Q=");
		headers.put("Referer", url);
		headers.put("User-Agent", "XXXX");
	}
	
	/**
	 * 起点 sxjyA1513
	 * http://118.112.188.108:8090/svn/
	 */
	public void doStart()
	{
		String url = "http://192.168.10.70:8090/svn/";
		doPost(url);
		doListProject(sbContent, url);
	}
	Pattern pA =  Pattern.compile("<a\\s*href=\"([^\"]+?)\"\\s*>", Pattern.MULTILINE | Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	Pattern struts2 =  Pattern.compile("struts2-core-(\\d(\\.\\d)+).jar", Pattern.MULTILINE | Pattern.DOTALL);
	Pattern ta3 =  Pattern.compile("ta3-[^\\-]+-(\\d(\\.\\d)+).jar", Pattern.MULTILINE | Pattern.DOTALL);
	
	/**
	 * 获取当前页面中的连接信息
	 * @param sbContent
	 * @param urlBase
	 */
	public  void doAList(StringBuffer sbContent, String urlBase)
	{
		Matcher m = pA.matcher(sbContent.toString());
		int n = 0, j = 0;
		while(m.find())
		{
			if(0 < m.groupCount())
			{
				if("../".equals(m.group(1)))continue;
				if(doOneFile(urlBase, m.group(1)))
					n++;
				j++;
				if(5 < j && 0 == n)
					break;
			}
		}
		// 
		Pattern pNoA = Pattern.compile("/svn/[0-9a-zA-Z\\-_]+/$", Pattern.CASE_INSENSITIVE|Pattern.DOTALL); 
		if(0 == j && pNoA.matcher(urlBase).find())
		{
			insertErrUrl(tableType, urlBase);
//			TestHashCodeParm.writeFile("/Volumes/MyWork/MyWork/sfTester/无权限.txt", urlBase + "\t没有读权限、或该项目没有提交内容\n");
		}
	}
	
	boolean bStart = false;
	/**
	 * 遍历所有项目
	 * @param sbContent
	 * @param urlBase
	 */
	public  void doListProject(StringBuffer sbContent, String urlBase)
	{
		Matcher m = pA.matcher(sbContent.toString());
		String szU;
		int n = 0, j = 0;//getLastPos(tableType, 0);
		while(m.find())
		{
			if(0 < m.groupCount())
			{
				if("../".equals(m.group(1)))continue;
//				if(!bStart && !"sxjyA1513/".equals(m.group(1)))
//				{
//					System.out.println(m.group(1));
//					continue;
//				}
				// 跳过断点
				if(n < j)
				{
					n++;
					continue;
				}
				
				bStart = true;
				sbContent.delete(0, sbContent.length());
				szU = urlBase + m.group(1);
				doPost(szU);
				doAList(sbContent, szU);
				insertLastPosl(tableType, n);
				n++;
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		CheckSvnStruts2 css = new CheckSvnStruts2();
		css.useMysql();
//		css.delete("delete  from svnInfo where type='commonsCollections'");
		css.doStart();
		css.update("update svnInfo set ver='' where ver='collections'");
		String s1 = MyQDataInfo.getFile(new File("/Users/xiatian/project/sfTester/javacode/com/mtx/safegene/test/xiatian/hacker/projectInfo.txt"));
		String []a = s1.trim().split("\\n"), x;
		for(String s:a)
		{
			x = s.trim().split("	");
			if(3 == x.length)
			{
				css.update("update svnInfo set ProjectName='" + x[2].trim() + "',depart='" + x[0].trim() + "' where projectId='" + x[1].trim() + "' and ProjectName=''");
			}
		}
		
		// and not (ver == '3.2.2' or ver =='4.1') 
		css.querySQL("select * from svnInfo where type='commonsCollections' order by depart,ProjectName", false, new ArrayList<TreeMap<String, Object>>()
		{
			private static final long	serialVersionUID	= -7061840719725188173L;
			public boolean add(TreeMap<String, Object> m)
			{
				     // 序号
//					System.out.print("\t");
					String syb = String.valueOf(m.get("depart"));
					if(null == syb || "null".endsWith(syb))syb = "";
					System.out.print(syb + "\t");
					// 项目名
					syb = String.valueOf(m.get("ProjectName"));
					if(null == syb || "null".endsWith(syb))syb = "";
					System.out.print(syb + "\t");
					
					System.out.print(m.get("projectId") + "\t");
					System.out.print(m.get("ver") + "\t");
					System.out.print(m.get("lastScan") + "\t");
					System.out.print(m.get("url"));
					System.out.print("\n");
				return true;
			}
		});
	}

}
