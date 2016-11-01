package com.mtx.xiatian.hacker;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.mtx.xiatian.evolvedata.MyQDataInfo;
import com.mtx.xiatian.evolvedata.MyThreadPoolExecutor;
/**
 * <pre>
 * 1、目录大小结算；
 * 2、多线程深度拷贝目录
 * 3、git更新脚本生成
 * </pre>
 * 
 * @author xiatian
 * 
 */
public class MyDir implements Runnable
{
	public static MyThreadPoolExecutor	tpe	= MyThreadPoolExecutor.getInstance();
	private File	                   src, des;
	public MyDir(){}
	/**
	 * @param s
	 *            源
	 * @param d
	 *            目的
	 */
	public MyDir(File s, File d)
	{
		this();
		src = s;
		des = d;
	}
	public void run()
	{
		// 跳过已经处理的文件
		if (des.isFile() && des.exists())
		{
			if (src.length() == des.length())
				;// System.out.println("Ok exists: " + des.getAbsolutePath());
			else
				System.out.println("err exists: " + src.getAbsolutePath() + " -> " + des.getAbsolutePath());
			return;
		}
		if (src.isDirectory())
		{
			if (!des.exists())
				des.mkdir();
			File[] fs = src.listFiles();
			if (null != fs)
			{
				for (File t : fs)
				{
					tpe.addRunnable(new MyDir(t, new File(des.getAbsolutePath() + File.separator + t.getName())));
				}
			}
			return;
		}
		// 拷贝的事情
		if (src.isFile())
		{
			doCopy(src, des);
		}
	}
	/**
	 * 写文件
	 * 
	 * @param f
	 * @param s
	 * @param out
	 * @return
	 */
	public static OutputStream writeFile(File f, String s, OutputStream out)
	{
		int lnSize = 1024 * 1024 * 1; // 100M
		try
		{
			if (null == out)
				out = new BufferedOutputStream(new FileOutputStream(f, true), lnSize);
			out.write(s.getBytes("UTF-8"));
			out.flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return out;
	}
	/**
	 * 32M缓存
	 * 
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
			byte[] b = new byte[lnSize];
			while (0 < (iL = in.read(b, 0, lnSize)))
			{
				out.write(b, 0, iL);
			}
			out.flush();
			System.out.println("Ok: " + s.getCanonicalPath() + " -> " + d.getAbsolutePath());
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (null != in)
					in.close();
				if (null != out)
					out.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	/**
	 * 获取目录字节数大小
	 * 
	 * @param d
	 * @return
	 */
	public static long getDir(File d)
	{
		if (null == d)
			return 0L;
		String n = d.getName();
		if (".".equals(n) || "..".equals(n))
			return 0L;
		long ln = 0;
		File[] fs = d.listFiles();
		if (null != fs)
			for (File f : fs)
			{
				if (f.isDirectory())
					ln += getDir(f);
				else
				{
					ln += f.length();
				}
			}
		return ln;
	}
	/**
	 * 生成git更新脚本
	 * 
	 * @param dir
	 * @param list
	 */
	public static void doGetGitDir(File dir, List<String> list)
	{
		String szN = ".git";
		if (null == dir || dir.getName().equals(szN))
			return;
		File[] fs = dir.listFiles();
		if (null != fs)
		{
			for (int i = 0, j = fs.length; i < j; i++)
			{
				File f  = fs[i];
				if (f.isDirectory())
				{
					if(szN.equals(f.getName()))
					{
						String k = f.getAbsolutePath();
						if(-1 < k.indexOf("/Desktop/"))
							continue;
						list.add(f.getParent());
						return;
					}
				}
				else fs[i] = null;
			}
			/**
			 * 深度遍历子目录
			 */
			for (File f : fs)
			{
				if(null == f)continue;
				if (f.isDirectory())
				{
					if(f.getName().startsWith("."))
						continue;
					doGetGitDir(f, list);
				}
			}
		}
	}
	/**
	 * 生成更新脚本
	 * 
	 * @param s
	 */
	public static void makeUpdateGitShell(String ...s)
	{
		final File f = new File("/Users/xiatian/updateAllGit.sh");
		f.delete();
		final OutputStream out = writeFile(f, "echo 开始更新所有git项目\n", null);
		List<String> list = new ArrayList<String>()
		{
			private static final long	serialVersionUID	= -5434103528090201977L;
			public boolean add(String s)
			{
				String s1 = "echo 开始更新: " + s + "\ncd \"" + s + "\"\ngit pull\n";
				writeFile(f, s1, out);
				System.out.println(s1);
				return true;
			}
		};
		for(String sk: s)
		{
			doGetGitDir(new File(sk), list);
		}
		try
		{
			out.flush();
			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void fixGitDirName(String sDir)
	{
		File []fs = new File(sDir).listFiles();
		String s;
		int n;
		String s1 = "url = ", s3 = "//";
		for(File f:fs)
		{
			if(f.isDirectory())
			{
				s = f.getAbsolutePath();
				if(new File(s + "/.git").isDirectory())
				{
					String rst = MyQDataInfo.getFile(new File(s + "/.git/config"));
					n = rst.indexOf(s1);
					if(-1 < n)
						rst = rst.substring(n + s1.length());
					n = rst.indexOf(s3);
					if(-1 < n)
						rst = rst.substring(n + s3.length());
					n = rst.indexOf('/');
					if(-1 < n)
						rst = rst.substring(n + 1).trim();
					n = rst.indexOf('\n');
					if(-1 < n)
						rst = rst.substring(0, n).trim();
					rst = rst.replaceAll("\\.git", "").replaceAll("\\/", "_");
//					if(-1 < rst.indexOf('\n'))
					if(!f.getName().endsWith(rst))
					{
						String szNew = s.substring(0, s.lastIndexOf('/') + 1) + rst;
						File nF = new File(szNew);
						if(nF.exists())
						{
							System.out.println("rm -rf " + szNew);
						}
						else
						{
//							System.out.println(s + " >> " + szNew);
							f.renameTo(nF);
						}
					}
//					/Volumes/BOOK/安全/project/reverse-shell_routersploit
//					/Volumes/BOOK/安全/project/python_dht/m4n3dw0lf_PytheM
//					/Volumes/BOOK/安全/project/python_dht/bmuller_kademlia
//					/Volumes/BOOK/安全/project/python_dht/Tribler_tribler
//					/Volumes/BOOK/安全/project/python_dht/Fuck-You-GFW_p2pspider
//					/Volumes/BOOK/安全/project/python_dht/CISOfy_lynis
					// git reset --hard HEAD~1;
//					System.out.println("echo 更新" + s +";cd " + s + ";git pull");
				}
				else
				{
					String sK = f.getName();
					if(".".endsWith(sK) || "..".equals(sK))continue;
					fixGitDirName(s);
				}
				
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Backup: 10G   Backups.backupdb
//		System.out.println(new MyDir().getDir(new File("/Volumes/BOOK/Backups.backupdb")) / 1024 / 1024/1024);
//		System.out.println(new MyDir().getDir(new File("/Users/xiatian/.npm")) / 1024 / 1024);
		
//		makeUpdateGitShell("/Users/xiatian/safe/", "/Volumes/BOOK/安全/", "/Volumes/other/project/");
//		 MyDir.tpe.addRunnable(
//				 new MyDir(
//						 new File("/Users/xiatian/Downloads/metasploit-framework-4.12.14/"),
//						 new File("/Users/xiatian/safe/metasploit-framework/")
//						 ));
		 fixGitDirName("/Volumes/BOOK/安全/project");
		fixGitDirName("/Users/xiatian/safe");
//		 MyDir.tpe.addRunnable(
//				 new MyDir(
//						 new File("/Volumes/BOOK/安全/Metasploit/Meterpreter-Scripts"),
//						 new File("/Users/xiatian/.msf4/modules")
//						 ));
//		 MyDir.tpe.addRunnable(new MyDir(
//		 new File("/Volumes/other/xiatian/.rvm"),
//		 new File("/Users/xiatian/.rvm")
//		 ));
		// MyDir.tpe.addRunnable(new MyDir(
		// new File("/Volumes/other/xiatian/.p2"),
		// new File("/Users/xiatian/.p2")
		// ));
		// MyDir.tpe.addRunnable(new MyDir(
		// new File("/Volumes/other/xiatian/.idm"),
		// new File("/Users/xiatian/.idm")
		// ));
		// String s = "/Users/xiatian/Library/Caches"; // Library/Application
		// Support // "/Users/xiatian"
		// File []fs = new File(s).listFiles();
		// long k = 100 * 1024 * 1024, t;
		// if(null != fs)
		// for(File f:fs)
		// {
		// if(f.isDirectory())
		// {
		// t = getDir(f);
		// if(t > k)
		// {
		// System.out.println(f.getName() + ": " + t);
		// }
		// }
		// }
	}
}
