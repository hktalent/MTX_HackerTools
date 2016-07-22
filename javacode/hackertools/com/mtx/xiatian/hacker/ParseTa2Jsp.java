package com.mtx.xiatian.hacker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mtx.xiatian.MD5Util;

/**
 * <pre>
 * 分析Ta2 jsp 表格标签
 CREATE TABLE `mydb`.`ta2DataGrid` (
  `tagname` VARCHAR(20) NOT NULL,
  `proname` VARCHAR(50) NOT NULL,
  `val` VARCHAR(150) NOT NULL);

 * </pre>
 * @author xiatian
 */
public class ParseTa2Jsp extends CommonTools
{
	public ParseTa2Jsp()
	{
		super.useMysql();
		super.hvLastScan = false;
	}
	
	private long nCnt = 1; 
	
	/**
	 * 后缀
	 */
	public String szEndWith = "\\.(jsp)$", newPath = "/Volumes/other/tools/k/";
	
	/**
	 * 属性合并
	 * @param m
	 * @param s
	 */
	public void doPropertis(Map<String,String> m, String s)
	{
		Matcher m1 = p1.matcher(s);
		while(m1.find())
		{
			m.put(m1.group(1).trim(), m1.group(2).trim());
		}
	}
	
	public Pattern p2 = Pattern.compile("<xui:(DataGrid|DataGridItem)\\s+(.*?)\\s*\\/?>", Pattern.MULTILINE),
			p1 = Pattern.compile("([^=]+?)=\\s*\"([^\"]*?)\"", Pattern.MULTILINE);
	/**
	 * 解析jsp
	 * @param s
	 * @return
	 */
	public Map<String,String>[] getTagInfo(String s)
	{
		
		 Map<String,String> m1 = new  HashMap<String,String>(), m2 = new  HashMap<String,String>();
		 if(null != s)
		 {
			 Matcher m = p2.matcher(s);
			 while(m.find())
			 {
				 if("DataGrid".equals(m.group(1)))
				 {
					 doPropertis(m1, m.group(2));
				 }
				 else if("DataGridItem".equals(m.group(1)))
				 {
					 doPropertis(m2, m.group(2));
				 }
			 }
		 }
		 return new Map[]{m1,m2};
	}
	
	public void doDir(String szDir)
	{
		Pattern p = Pattern.compile(szEndWith, Pattern.DOTALL|Pattern.MULTILINE);
		File []fs = new File(szDir).listFiles();
		String szName = null, md5;
		TreeMap<String, Object> m = new TreeMap<String, Object>(), mQ1 = null; 
		for(File f: fs)
		{
			szName = f.getAbsolutePath();
			if(".".equals(szName) || "..".equals(szName))continue;
			if(f.isDirectory())
				doDir(szName);
			else if(!p.matcher(szName).find())
				continue;
			else
			{
				m.clear();
				Map<String,String>[]ms = getTagInfo(readFileString(szName));
				
				String szTag ;
					
					Iterator <String>it = null;
					String keyT;
					String szV;
					Map<String,String> kk;
					for(int i = 0; i < ms.length; i++)
//					for(Map<String,String> kk:ms)
					{
						kk = ms[i];
						szTag = 0 == i ? "DataGrid" : "DataGridItem";
						m.put("tagname", szTag);
						it = kk.keySet().iterator();
						while(it.hasNext())
						{
							keyT = it.next();
							szV = kk.get(keyT).trim().replaceAll("'", "''");
							mQ1 = super.querySQL("select * from ta2DataGrid where tagname='" +szTag + "' and proname='" + keyT + "' and val='" + szV + "'");
							if(null == mQ1 || 0 == mQ1.size())
							{
								m.put("proname", keyT);
								m.put("val", kk.get(keyT));
								super.insertTable("ta2DataGrid", null, m);
//								System.out.println(keyT + " = " + kk.get(keyT));
							}
						}
					}
			}
		}
	}
	
	public  String readFileString(String s)
    {
		try
        {
	        return new String(readFile(s), "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
	        e.printStackTrace();
        }
		return null;
    }
	 /**
     * 读取文件
     * @param s
     * @return
     */
    public  byte[] readFile(String s)
    {
    	    System.out.println(s);
        InputStream in = null;
        ByteArrayOutputStream out = null;
        byte []b = null;
        try
        {
            in = new FileInputStream(new File(s));
            out = new ByteArrayOutputStream();
            b = new byte[4096];
            int i;
            while (-1 < (i = in.read(b, 0, 4096)))
            {
                out.write(b, 0, i);
            }
            out.flush();
            b = out.toByteArray();
           out.reset();
           out = null;
        }
        catch (Throwable e)
        {
        		e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != out) out.close();
            }
            catch (Throwable e)
            {
            		e.printStackTrace();
            }
            try
            {
                if (null != in) in.close();
            }
            catch (Throwable e)
            {
            	e.printStackTrace();
            }
            in = null;
            out = null;
        }
        return b;
    }
	/**
	 * 获取md5
	 * @param b
	 * @return
	 */
	public String md5(byte []b)
	{
		return MD5Util.MD5(b);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ParseTa2Jsp df = new ParseTa2Jsp();
		// df.doDir("/Volumes/other/project/hissicp3/webapp/");
		df.doDir("/Volumes/other/project/hissicp3/");
		df.doDir("/Volumes/other/project/ynsicp3/");
	}
}
