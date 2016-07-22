package com.mtx.xiatian.hacker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.mtx.xiatian.evolvedata.MyThreadPoolExecutor;

/**
 * 目录大小结算；多线程深度拷贝目录
 * @author xiatian
 *
 */
public class MyDir implements Runnable
{
	public static MyThreadPoolExecutor tpe = MyThreadPoolExecutor.getInstance(); 
	private File src, des;
	/**
	 * @param s 源
	 * @param d 目的
	 */
	public MyDir(File s, File d)
	{
		src = s;
		des = d;
	}
	public void run()
	{
		// 跳过已经处理的文件
		if(des.isFile() && des.exists())
		{
			if(src.length() == des.length())
				;// System.out.println("Ok exists: " +  des.getAbsolutePath());
			else System.out.println("err exists: " + src.getAbsolutePath() + " -> " + des.getAbsolutePath());
			return;
		}
		if(src.isDirectory())
		{
			if(!des.exists())
				des.mkdir();
			File []fs = src.listFiles();
			if(null != fs)
			{
				for(File t: fs)
				{
					tpe.addRunnable(new MyDir(t, new File(des.getAbsolutePath() + File.separator + t.getName())));
				}
			}
			return;
		}
		
		// 拷贝的事情
		if(src.isFile())
		{
			doCopy(src, des);
		}
	}
	
	/**
	 * 32M缓存
	 * @param s
	 * @param d
	 */
	public void doCopy(File s, File d)
	{
		int lnSize = 1024 * 1024 * 32, iL = 0; // 100M
		OutputStream out = null;
		InputStream in = null;
		try
		{
			in = new FileInputStream(s);
			out = new BufferedOutputStream(new FileOutputStream(d, false), lnSize);
			byte []b = new byte[lnSize];
			while(0 < (iL = in.read(b, 0, lnSize)))
			{
				out.write(b, 0, iL);
			}
			out.flush();
			System.out.println("Ok: " + s.getCanonicalPath() + " -> " + d.getAbsolutePath());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(null != in)in.close();
				if(null != out)out.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 获取目录字节数大小
	 * @param d
	 * @return
	 */
	public static long getDir(File d)
	{
		if(null == d)return 0L;
		String n = d.getName();
		if(".".equals(n) || "..".equals(n))return 0L;
		long ln = 0;
		File []fs = d.listFiles();
		if(null != fs)
		for(File f:fs)
		{
			if(f.isDirectory())
				ln += getDir(f);
			else
			{
				ln += f.length();
			}
		}
		return ln;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		MyDir.tpe.addRunnable(new MyDir(new File("/Users/xiatian/.rvm"), new File("/Volumes/other/xiatian/.rvm")));
//		MyDir.tpe.addRunnable(new MyDir(
//				new File("/Volumes/other/xiatian/.rvm"),
//				new File("/Users/xiatian/.rvm")
//				));
		
//		MyDir.tpe.addRunnable(new MyDir(
//				new File("/Volumes/other/xiatian/.p2"),
//				new File("/Users/xiatian/.p2")
//				));
		MyDir.tpe.addRunnable(new MyDir(
				new File("/Volumes/other/xiatian/.idm"),
				new File("/Users/xiatian/.idm")
				));
		
//		String s = "/Users/xiatian/Library/Caches"; // Library/Application Support  // "/Users/xiatian"
//		File []fs = new File(s).listFiles();
//		long k = 100 * 1024 * 1024, t;
//		if(null != fs)
//		for(File f:fs)
//		{
//			if(f.isDirectory())
//			{
//				t = getDir(f);
//				if(t > k)
//				{
//					System.out.println(f.getName() + ": " + t);
//				}
//			}
//		}
	}
}
