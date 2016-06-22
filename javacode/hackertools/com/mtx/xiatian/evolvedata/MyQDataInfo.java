package com.mtx.xiatian.evolvedata;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.mtx.core.tools.dbtools.SysDbUtils;
import com.mtx.face.IMyResultHandler;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class MyQDataInfo {
	
	
	/**
	 * 输出压缩对象
	 * @param response
	 * @param context
	 * @param o
	 */
	public static void writeObject(String szFile, Object o)
	{
		GZIPOutputStream stream =  null;
		FileOutputStream fos = null; 
		ObjectOutputStream out = null; 
		try{
			fos = new FileOutputStream(new File(szFile));
			stream = new GZIPOutputStream(fos);
			out = new ObjectOutputStream(stream);
			out.writeObject(o);
			out.flush();
			out.close();
			out = null;
			stream.close();
			stream = null;
			fos.close();
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			if(null != out)
				try
				{
					out.close();
					if(null != stream)stream.close();
				}catch(Throwable e){
					e.printStackTrace();
				}
			}
	}
	
	/**
	 * 读取文件
	 * @param szFile
	 * @return
	 */
	public static Object readObject(String szFile)
	{
		Object o = null;
		GZIPInputStream stream =  null;
		FileInputStream fos = null; 
		ObjectInputStream out = null; 
		try{
			fos = new FileInputStream(new File(szFile));
			stream = new GZIPInputStream(fos);
			out = new ObjectInputStream(stream);
			o = out.readObject();
			out.close();
		}catch(Throwable e){
			e.printStackTrace();
			}finally{
				try
				{
					out.close();
					if(null != stream)stream.close();
				}catch(Throwable e){
					e.printStackTrace();
				}
			}
		return o;
	}
	/**
	 * 输出日志
	 * @param a
	 */
	public static void log(String ...a)
	{
//		if(true)return;
		StringBuffer sb = new StringBuffer();
		for(String s:a)
			sb.append(s);
		System.out.println(sb.toString());
	}
	
	public static String szErrFileName = "/Users/xiatian/Downloads/errUrls.txt";
	/**
	 * 清空文件内容
	 * @param f
	 */
	public static void processErrUrlFile(String szWrite)
	{
		File f = new File(szErrFileName);
		try
		{
			FileWriter writer = new FileWriter(f, true);
			writer.append(szWrite);
			writer.append("\n");
			writer.close();
		}catch(Exception e){}
	}
	
	/**
	 * 清空文件内容
	 * @param f
	 */
	public static synchronized void processUrlFile(String fn, String szWrite)
	{
		File f = new File(fn);
		try
		{
			FileWriter writer = new FileWriter(f, false);
			writer.append(szWrite);
			writer.close();
		}catch(Exception e){}
	}
	
	public static SysDbUtils sdb = SysDbUtils.getInstance();
	/**
	 * 处理一条QQ信息
	 * doOneQInfo("595256993","", "女", "90887114", "23");
	 * @param qq
	 * @param xm
	 * @param xb
	 * @param qun
	 */
	public static synchronized int doOneQInfo(String qq, String xm, String xb, String qun, String nl)
	{
		int nRst = 0;
		xm = xm.trim();
		nl = nl.trim();
		qq = qq.trim();
		qun = qun.trim();
		
		// 转义
		xm = xm.replaceAll("['\\\"\\\\/\\s\\.<>_\\*\\^]", "");
		if(0 == xm.trim().length())xm = "无名字";
//		log("处理：", qq, " 名字：", xm);
		Map <String, Object> parm = new HashMap<String, Object>();
		final List <Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
		// 判断存在否？
		sdb.queryForList("select * from qq where qq='" + qq + "'", new IMyResultHandler(){
			public void doResult(Map<String, Object> m) {
				lst.add(m);
			}});
		// 插入
		if(0 == lst.size())
		{
			parm.clear();
			parm.put("qq", qq);
			parm.put("age", Integer.parseInt(nl));
			parm.put("sex", xb);
			if(0 < sdb.insert("qq", parm))
			{
				nRst++;
				log("插入QQ成功：" , qq);
			}
		}
		else
		{
			log("QQ表中已经存在：" , qq);
			nRst++;
		}
		lst.clear();
		
		// 判断存在否？
		sdb.queryForList("SELECT qun FROM qqun where qun='" + qun + "'", new IMyResultHandler(){
			public void doResult(Map<String, Object> m) {
				lst.add(m);
			}});
		// 插入
		if(0 == lst.size())
		{
			parm.clear();
			parm.put("qun", qun);
			if(0 < sdb.insert("qqun", parm))
			{
				nRst++;
				log("插入qqun成功：" , qun);
			}
		}
		else
		{
			log("qqun表中已经存在：" , qun);
			nRst++;
		}
		lst.clear();
		
		// 判断存在否？
		sdb.queryForList("SELECT qq,qun FROM mydb.B where qun='" + qun + "' and qq = '" + qq + "'", new IMyResultHandler(){
			public void doResult(Map<String, Object> m) {
				lst.add(m);
			}});
		// 插入
		if(0 == lst.size())
		{
			parm.clear();
			parm.put("qun", qun);
			parm.put("qq", qq);
			if(0 < sdb.insert("B", parm))
			{
				nRst++;
				log("插入B成功：" , qun , "(" , qq , ")");
			}
		}
		else {
			log("B表中已经存在：" , qun , "(" , qq , ")");
			nRst++;
		}
		lst.clear();
		
		
		// 更新失败就插入
		if(0 == sdb.update("update xm set xmcnt = xmcnt + 1 where qq='" + qq + "' and xm = '" + xm + "' "))
		{
			parm.clear();
			parm.put("xm", xm);
			parm.put("qq", qq);
			// 判断存在否？
			sdb.queryForList("SELECT * FROM xm where  xm = '" + xm + "' and qq='" + qq + "'", new IMyResultHandler(){
				public void doResult(Map<String, Object> m) {
					lst.add(m);
				}});
			
			if(0 == lst.size())
			{
				parm.put("xmcnt", new Integer(1));
				if(0 < sdb.insert("xm", parm))
				{
					nRst++;
					log("插入xm成功：" , qq , "(" , xm , ")");
				}
			}
			else
			{
				log("xm表中已经存在：" , qq , "(" , xm , ")");
				nRst++;
			}
		}
		else
		{
			nRst++;
			log("更新xm成功：" , qq , "(" , xm , ")");
		}
		return nRst;
	}
	
	/**
	 * 线程
	 */
	public static MyThreadPoolExecutor pool = MyThreadPoolExecutor.getInstance();
	/**
	 * 处理单个QQ信息文件到数据库
	 * @param s
	 */
	public static  void doOneFileStr(String s)
	{
		if(null != s)
		{
			AC01Info.getQqList(s, new IDoOneQQ(){
				private static final long serialVersionUID = 1L;
				public boolean doOneQQ(Map<String, String> m) {
					try
					{
						int nR = doOneQInfo(m.get("qq"),m.get("xm"), m.get("xb"), m.get("qun"), m.get("nl"));
						if(4 == nR)
						{
							processUrlFile(m.get("qq"));
//							
//							String url = null;
//							url = "http://qun.594sgk.com/qq/" + m.get("qq") + ".html";
//							MyQDataInfo.doOneFileStr(AC01Info.getUrlStr(url));
//							url = "http://qun.594sgk.com/qq/" + m.get("qun") + ".html";
//							MyQDataInfo.doOneFileStr(AC01Info.getUrlStr(url));
//							
							pool.addRunnable("http://qun.594sgk.com/qq/" + m.get("qq") + ".html");
							pool.addRunnable("http://qun.594sgk.com/qq/" + m.get("qun") + ".html");
						}
						else log(m.get("qq"),  " 失败了： ", nR + "");
					}catch(Exception e){
						e.printStackTrace();
					}
					return true;
				}});
		}
	}
	/**
	 * 处理一个文件
	 * @param f
	 */
	public static void doOneFile(final File f)
	{
		if(!f.getAbsolutePath().endsWith(".html"))return;
//		log("处理" , f.getAbsolutePath());
		String s = getFile(f);
		doOneFileStr(s);
	}
	
	/**
	 * 获取文件内容
	 * @param f
	 * @return
	 */
	public static String getFile(File f )
	{
		try
		{
			if(0L == f.length())
			{
				log("0字节文件: " , f.getAbsolutePath());
				return null;
			}
			FileInputStream in = new FileInputStream(f);
		    ByteOutputStream out = new ByteOutputStream();
		    byte []a = new byte[1024];
		    int i = 0;
		    while(-1 < (i = in.read(a, 0, a.length)))
		    {
		    		out.write(a,0, i);
		    }
		    in.close();
		    a = out.getBytes();
		    out.close();out = null;
		    return new String(a, "UTF-8");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 处理本地文件
	 * @param s
	 */
	public static void doAllFile(String s)
	{
		if(null == s)s = "/Users/xiatian/Downloads/web/qun.594sgk.com/qq";
		new File(s).listFiles(new FileFilter(){
			public boolean accept(File k) {
				doOneFile(k);
				return false;
			}});
	}
	
	public static String cacheQFile = "/Users/xiatian/Downloads/cacheQUrlFile.txt";
	/**
	 * 处理url文件
	 */
	public static void doAllURLFile()
	{
		String s = "http://qun.594sgk.com/qq/626822028.html";
		String s1 = getFile(new File(cacheQFile));
		if(null != s1)s = s1;
		
		Map<String, Object> mParams = new HashMap<String, Object>();
		mParams.put("url", s);
		sdb.delete("url", mParams);
		
		String url1 =s;
		String qq = url1.substring(url1.lastIndexOf("/") + 1, url1.lastIndexOf("."));
		
		AC01Info.mYjclQQ.remove(qq);
		doOneFileStr(AC01Info.getUrlStr(s));
	}
	/**
	 * 记录断点文件
	 * @param qq
	 */
	public static void processUrlFile(String qq)
	{
		processUrlFile(cacheQFile, "http://qun.594sgk.com/qq/" + qq + ".html");
	}

	/**
	 * http://qun.594sgk.com/qq/526501506.html
	 * http://qun.594sgk.com/qq/626822028.html
	 * @param args
	 */
	public static void main(String[] args) {
		doAllURLFile();
		
//		String []a = getFile(new File(szErrFileName)).split("\\n");
//		for(String s: a)
//		{
//			pool.addRunnable(s);
//		}
//		
		while(true)
		{
			try {
				Thread.sleep(13);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		AC01Info.getUrlStr("http://qun.594sgk.com/qq/11602011.html");
//		processUrlFile(cacheQFile, "http://qun.594sgk.com/qq/116213.html");
//		log("good ' \" \\ / just".replaceAll("['\\\"\\\\/]", ""));
	}

}
