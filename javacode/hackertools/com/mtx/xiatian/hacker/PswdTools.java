package com.mtx.xiatian.hacker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.mtx.xiatian.MD5Util;

public class PswdTools extends CommonTools
{

	/**
	 * 批量插入密码到数据库
	 * 
	 * @param szPswdFile
	 */
	public void doInsertPswd(final String szPswdFile)
	{
		hvLastScan = false;
		final Map<String, String> mPP1 = new HashMap<String, String>();
		BufferedReader br = null;
		try
		{
			String line = null;

			br = new java.io.BufferedReader(new java.io.FileReader(szPswdFile));
			TreeMap<String, Object> mData = null;
			while (true)
			{
				line = br.readLine();
				if (line == null)
					break;
				line = line.trim();
				if(mPP1.containsKey(line))
					continue;
				line = line.replaceAll("'", "\\'");
				if (0 == line.length())
					continue;
				mPP1.put(line, "");
				mData = new TreeMap<String, Object>();
				mData.put("pswd", line);
				mData.put("md5", MD5Util.MD5(line));
				mData.put("cnt", 1);
				// info(line);
				if (0 == insertTable("xt_pswd", "pswd='{pswd}'", mData))
					update("update xt_pswd set cnt=cnt+1 where pswd='" + line + "'");
			}
		} catch (Exception e)
		{
		} finally
		{
			try
			{
				if (null != br)
					br.close();
			} catch (Exception e)
			{
			}
		}
		// "pswd={pswd}",

	}

	private Map<String, String> mPP1 = new HashMap<String, String>();
	/**
	 * 批量插入密码到数据库
	 * 
	 * @param szPswdFile
	 */
	public void doInsertPswdStream(final String szPswdFile)
	{
		useMysql();
		hvLastScan = false;
		insertStream("xt_pswd", new IGetOneMap()
		{
			BufferedReader	br	= null;

			public TreeMap<String, Object> getOneMap()
			{
				try
				{
					if (null == br)
						br = new java.io.BufferedReader(new java.io.FileReader(szPswdFile));
					String line = br.readLine();
					TreeMap<String, Object> mData = new TreeMap<String, Object>();
					if (line == null)
					{
						br.close();
						return null;
					}
					line = line.trim();
					if(mPP1.containsKey(line))
					{
//						info("跳过: ", line);
						return mData;
					}
					if(-1 < line.indexOf("'"))
						info(line);
					line = line.replaceAll("'", "''");
					
//					line = line.replaceAll("'", "\\'");
					
					if(mPP1.containsKey(line))return mData;
					mPP1.put(line, "");
					mData.put("pswd", line);
					mData.put("md5", MD5Util.MD5(line));
					mData.put("cnt", 1);
					if (1 == update("update xt_pswd set cnt=cnt+1 where pswd='" + line + "'"))
						mData.clear();
//					if(0 < mData.size())
//						info("处理: ", line);
					return mData;
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	/**
	 * 提交数据了: 3580000
	 * @param args
	 */
	public static void main(String[] args)
	{
		final PswdTools pt = new PswdTools();
		pt.useMysql();
		pt.querySQL("select pswd from xt_pswd", false, new ArrayList<TreeMap<String,Object>>(){
            public boolean add(TreeMap<String, Object> m)
            {
            	     if(0 < m.size())
            	    	 	pt.mPP1.put(String.valueOf(m.get("pswd")), "");
	            return true;
            }
		});
//		pt.doInsertPswd("/Users/xiatian/safe/密码/txt");
		pt.doInsertPswdStream("/Users/xiatian/safe/密码/txt");
		pt.doInsertPswdStream("/Users/xiatian/safe/密码/手机号密码字典.txt");
		pt.doInsertPswdStream("/Users/xiatian/safe/密码/常用密码字典.txt");
//		MyThreadPoolExecutor.getInstance().addRunnable(new Runnable()
//		{
//			public void run()
//			{
//				pt.doInsertPswdStream("/Users/xiatian/safe/密码/手机号密码字典.txt");
//			}
//		},new Runnable()
//		{
//			public void run()
//			{
//				pt.doInsertPswdStream("/Users/xiatian/safe/密码/常用密码字典.txt");
//			}
//		},new Runnable()
//		{
//			public void run()
//			{
//				pt.doInsertPswdStream("/Users/xiatian/safe/密码/txt");
//			}
//		});
//		
		
	}

}
