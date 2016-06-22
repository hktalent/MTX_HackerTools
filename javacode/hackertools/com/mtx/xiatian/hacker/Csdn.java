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
  CREATE TABLE `mydb`.`csdn` (
  `zh` VARCHAR(200) NOT NULL,
  `pswd` VARCHAR(150) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL ,
  `email` VARCHAR(150) NULL,
  PRIMARY KEY (`zh`));
  ALTER TABLE `mydb`.`csdn` 
CHANGE COLUMN `pswd` `pswd` VARCHAR(150) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL ;

Running: /Applications/MySQLWorkbench.app/Contents/MacOS/mysqldump --defaults-file="/var/folders/k5/n1bnf5d57lj_b_zz8zyxrdn80000gn/T/tmpavwZv3/extraparams.cnf"  --user=root --host=127.0.0.1 --protocol=tcp --port=3306 --default-character-set=utf8 --single-transaction=TRUE --skip-triggers "mydb"

</pre>
 * @author xiatian
 */
public class Csdn extends CommonTools
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
		{ "zh", "pswd", "email" };
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
				a1 = s.split("\\s*#\\s*");
				map = new TreeMap<String, Object>();
				lnCnt++;
				// 487001
				if(lnCnt < 4376319)continue;
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
	
	/**
	 * 处理文本文件
	 * @param excelFilePath
	 * @param list
	 */
	public void doOneText1(String excelFilePath, final List<TreeMap<String, Object>> list)
	{
		System.out.println("开始打开文件...");
		System.out.println(excelFilePath);
		String[] a1 = null;
		TreeMap<String, Object> map = null;
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(excelFilePath)), "GBK"));
			String s = null;
			long lnCnt = 0;
			while (null != (s = reader.readLine()))
			{
				lnCnt++;
				s = s.trim();
				if (0 == s.length())
					continue;
				a1 = s.split("\\s*#\\s*");
				map = super.querySQL("select zh from csdn where zh='" + a1[0] + "'");
				if(null == map || 0 == map.size())
				{
					super.writeFile("/Volumes/dbdata/sgkzl/csdn/csdn_noIn.txt", lnCnt + "\t" + s + "\n");
					System.out.println(lnCnt + ": " + s);
				}
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
				insert("csdn", list);
			}
		}).start();
		if (excelFilePath.endsWith(".sql"))
			doOneText(excelFilePath, list);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final String[] aFs = "www.csdn.net.sql".split(" ");
		final String path = "/Volumes/dbdata/sgkzl/csdn/";
		final Csdn dd = new Csdn();
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
