package com.mtx.xiatian.hacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * <pre>
CREATE TABLE `mydb`.`weibo` (
  `email` VARCHAR(180) NOT NULL,
  `pswd` VARCHAR(45) NULL,
  PRIMARY KEY (`email`));

</pre>
 * @author xiatian
 */
public class Weibo extends CommonTools
{
	
	/**
	 * 处理文本文件
	 * @param excelFilePath
	 * @param list
	 */
	public void doOneText(String excelFilePath, final List<TreeMap<String, Object>> list)
	{
		System.out.println("开始打开文件...");
		System.out.println(excelFilePath);
		String[] a =
		{ "email" ,"pswd"};
		String[] a1 = null;
		long lnCnt = 0;
		TreeMap<String, Object> map = null;
		BufferedReader reader = null;
		int i, j;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(excelFilePath)), "GBK"));
			String s = null;
			while (null != (s = reader.readLine()))
			{
				s = s.trim();
				if (0 == s.length())
					continue;
				a1 = s.split("   \\s*");
				map = new TreeMap<String, Object>();
				lnCnt++;
				if(lnCnt < 1200000)continue;
				if (0L == lnCnt % 30000)
				{
					System.out.println("开始处理数据行：" + lnCnt);
					System.gc();
				}
				for (i = 0, j = Math.min(a1.length, a.length); i < j; i++)
				{
					map.put(a[i], a1[i].trim());
				}
				list.add(map);
				// 通知有数据
				synchronized (list)
				{
					list.notifyAll();
				}
				synchronized (list)
				{
					while (0 < list.size())
					{
						list.wait();
					}
				}
			}
			list.add(null);
			synchronized (list)
			{
				list.notifyAll();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void doOneFile(String excelFilePath)
	{
		super.useMysql();
		super.hvLastScan = false;
		final List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>();
		System.out.println("开始insert...");
		// 必须以并行线程进行
		new Thread(new Runnable()
		{
			public void run()
			{
				insert("weibo", list);
			}
		}).start();
		if (excelFilePath.endsWith(".txt"))
			doOneText(excelFilePath, list);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final String[] aFs = "weibo.com_12160.dbh.txt".split(" ");
		final String path = "/Volumes/dbdata/sgkzl/weibo12160/";
		final Weibo dd = new Weibo();
		dd.useMysql();
		dd.hvLastScan = false;
//		final MyMap map = new MyMap();
//		dd.query("select  * from csdn", new ArrayList<TreeMap<String,Object>>(){
//            public boolean add(TreeMap<String, Object> m)
//            {
//            		map.put((String)m.get("zh"), m);
//	            return true;
//            }
//		});
//		System.out.println("Ok");
		for (String s : aFs)
		{
			dd.doOneFile(path + s.trim());
		}
	}

}
