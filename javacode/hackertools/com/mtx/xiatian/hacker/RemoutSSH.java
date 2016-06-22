package com.mtx.xiatian.hacker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;
import net.neoremind.sshxcute.task.impl.ExecShellScript;

/**
 * 通过ssh远程执行命令 2016-1-2
 * 
 * @author xiatian
 */
public class RemoutSSH extends CommonTools
{
	private String	ip, user, pswd;
	private SSHExec	ssh;

	public RemoutSSH(String ip, String user, String pswd)
	{
		this.ip = ip;
		this.user = user;
		this.pswd = pswd;
		getLstPswd(pswd);
		if(null != ip)
			init();
	}

	/**
	 * 初始化
	 */
	private void init()
	{
		// 新建一个 ConnBean 对象，三个参数依次是 ip 地址、用户名、密码
		// 将上面新建的 ConnBean 作为参数传递给 SSHExec 的静态单例方法，得到一个 SSHExec 的实例
		ssh = SSHExec.getInstance(new ConnBean(ip, user, pswd));
		// 利用上面得到的 SSHExec 实例连接主机
		long lnTm = 0, lnFive = 2 * 60 * 1000;
		while(!ssh.connect())
		{
			try
            {
	            Thread.sleep(333);
	            lnTm += 333;
	            if(lnTm > lnFive)
	            {
	            		info("多次连接主机都失败了！");
	            		break;
	            }
            } catch (Exception e)
            {
            		info(e);
            }
		}
	}

	protected void finalize() throws Throwable
	{
		destroy();
		super.finalize();
	}

	public void destroy()
	{
		if (null != ssh)
		{
			ssh.disconnect();
		}
	}

	/**
	 * 上传文件夹下全部文件到远程主机;上传本地文件到服务器上
	 * 
	 * @param szLocalFile
	 * @param remotePath
	 * @param bExec
	 *            上传后执行
	 */
	public Result doUploadFile(String szLocalFile, String remotePath, boolean bExec)
	{
		Result rst = new Result();
		try
		{
			if (new File(szLocalFile).isDirectory())
				ssh.uploadAllDataToServer(szLocalFile, remotePath);
			else
				ssh.uploadSingleDataToServer(szLocalFile, remotePath);
			if (bExec)
			{
				int n = szLocalFile.lastIndexOf('/');
				if (-1 == n)
					n = 0;
				else
					n++;
				CustomTask ct1 = new ExecShellScript(remotePath, szLocalFile.substring(n), "hello world");
				return ssh.exec(ct1);
			}
		} catch (Exception e)
		{
			info(e);
			rst.error_msg = e.getMessage();
		}
		return rst;
	}

	/**
	 * 执行命令
	 * @param cmd
	 * @return
	 */
	public Result doExec(String ...cmd)
	{
		if(null == ssh)return null;
		Result rst = new Result();
		String []a = new String[cmd.length * 3];
		int i = 0;
		for(String s:cmd)
		{
		      a[i++] = "echo " + s;
		      a[i++] = "echo ===========================================";
		      a[i++] = s;
		}
		
		CustomTask sampleTask = new ExecCommand(a);
		try
		{
			return ssh.exec(sampleTask);
		}
		catch (Exception e)
		{
			if(-1 < e.getMessage().indexOf("session is down"))
			{
				init();
				return doExec(cmd);
			}
			info(e);
			rst.error_msg = e.getMessage();
		}
		return rst;
	}
	
	private String pswdTable = "pswdTable";
	/**
	 * <pre>
	 * 修改密码
	 * echo "NewPass" |passwd foo --stdin
	 * </pre>
	 * @param newPswd
	 * @return
	 */
	public Result chpasswd(String newPswd)
	{
		if(null == newPswd)
			newPswd = getPswd();
		if(null == ssh || newPswd.equals(pswd))return null;
//		doExec("ps -ef|grep passwd|grep -v grep|cut -c 9-15|xargs kill -9");
		TreeMap<String, Object> m99= new TreeMap<String, Object>();
		m99.put("ip", ip);
		m99.put("user", user);
		m99.put("oldPswd", pswd);
		m99.put("newPswd", newPswd);
		Result rst = doExec("echo \"" + newPswd + "\" |passwd "  + user +" --stdin ");
		//  && -1 < rst.sysout.indexOf("all authentication tokens updated successfully")
		if(null != rst && rst.isSuccess)
		{
			if(1 != insertTable(pswdTable, "ip='" + ip + "' and user='" + user + "' and oldPswd='" + pswd + "' and newPswd='" + newPswd + "'", m99))
				info("记录新密码失败了！");
		}
		else info("修改密码失败！", ip);
		return rst;
	}
	
	/**
	 * 获取最后一次的最新密码
	 * @param szCurPswd
	 * @return
	 */
	private String getLstPswd(String szCurPswd)
	{
		// 将上次的新密码作为本次的老密码
		TreeMap<String, Object>m1 = querySQL("select newPswd from " + pswdTable + " where ip='" + ip + "' and user='" + user + "' order by lastScan desc  limit 1", false, null);
		if(null != m1 && 0 < m1.size() && null != m1.get("newPswd"))
		{
			String pswd1 = String.valueOf(m1.get("newPswd"));
			if(!szCurPswd.equals(pswd1))
			{
				info("当前密码和最后一次最新密码不一致，已经更新为：", pswd1);
				pswd = pswd1;
				return pswd1;
			}
		}
		return szCurPswd;
	}
	
	/**
	 * 修改密码
	 * @param ip
	 * @param user
	 * @param pswd
	 * @param newPswd
	 * @return
	 */
	public static Result changePswd(String ip, String user, String pswd, String newPswd)
	{
		final RemoutSSH rssh = new RemoutSSH(ip, user, pswd);
		// 将上次的新密码作为本次的老密码
		Result rst = rssh.chpasswd(newPswd);
		
		List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>(){
			public boolean add(TreeMap<String, Object> m)
			{
				rssh.info(m.toString() + "\n");
				return true;
			}
		};
//		rs.delete("delete  from " + rs.pswdTable);
		rssh.querySQL("select * from " + rssh.pswdTable + " where ip='" + ip + "' and user='" + user + "' order by lastScan desc limit 1", false, list);
		rssh.destroy();
		return rst;
	}
	
	/**
	 * 密码模板
	 */
	private static char []szPswdStr = "~!#%^*()_+POIUYTREWQASDFGHJKL:?><MNBVCXZqazwsxMTXedcrfvtgbyhnujmik,ol.p;/[]=-0987654321".toCharArray();
	
	private static Random rd = new Random(System.nanoTime());
	/**
	 * 获取一个随机数
	 * @return
	 */
	private static int getRandom()
	{
		return  Math.abs(rd.nextInt());
	}
	
	/**
	 * 获取一个随机密码
	 * @return
	 */
	public static String getPswd()
	{
		int i = (int)Math.max(16, getRandom() % 26), j = szPswdStr.length;
		char []a = new char[i];
		while(0 <= --i)
		{
			a[i] = szPswdStr[Math.abs(getRandom() % j)];
		}
		return String.valueOf(a);
	}
	/**
	 * 批量修改密码
	 */
	public static void doSafeServerChgPswd()
	{
		Result rst = null;
		String szPswd = "Z2zoO01I|l6b9QGS5*"; // Z2zoO01I|l6b9QGS5123 
//		szPswd = "Yinhai!@#$";  
		String [][] a = {
//				{"192.168.10.115", "root", "Goolge+_)=-0", szPswd},
//				{"192.168.10.123", "root", "Goolge+_)=-0", szPswd},
//				{"192.168.10.184", "root", "Yinhai!@#$", szPswd},
//				{"192.168.10.216", "root", "Goolge+_)=-0", szPswd},
				{"192.168.10.217", "root", 
					 "Yinhai!@#$",
//					"Z2zoO01I|l6b9QGS5*", 
					szPswd},
		};
		for(String []p : a)
		{
			if(4 == p.length)
			{
				rst = changePswd(p[0], p[1], p[2], p[3]);
				if(null != rst)
					System.out.println(rst.sysout);
			}
		}
	}
	
	/**
	 * 释放缓存
	 * @return
	 */
	public Result freeCache()
	{
		return doExec("sync", "sync", "echo 1 > /proc/sys/vm/drop_caches", "echo 3 > /proc/sys/vm/drop_caches");
	}
	
	/**
	 * 获取系统信息
	 * @param a
	 * @return
	 */
	public Result getInfo(String ...a)
	{
		String []p = {"cat /proc/sys/fs/file-nr",
				"ulimit -n",
				"free -m",
				"uname -a",
				"cat /etc/passwd |cut -f 1 -d :" // 查看所有用户
				,"lsblk -a"// Linux块设备的信息.块设备是硬盘和闪驱等之类的存储设备
				,"df -a" // 文件系统信息
				,"ps -ef"
				,"cat /proc/cpuinfo"// cpu信息
				,"cat /proc/meminfo" // | head -1 内存使用情况
				,"cat /proc/partitions" // 列出系统的分区信息
				,"ifconfig"
				,"netstat -anp" // 端口和服务的查看
				};
		String []x = new String[p.length + a.length];
		System.arraycopy(p, 0, x, 0, p.length);
		System.arraycopy(a, 0, x, p.length, a.length);
		return doExec(x);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		doSafeServerChgPswd();
		// newPswd
		// Goolge+_)=-0
		RemoutSSH rs = new RemoutSSH(
//				null
				"192.168.10.115"
				, "root", "Z2zoO01I|l6b9QGS5*");
//		rs.delete("delete from " + rs.pswdTable + " where ip='192.168.10.184'");
//		rs.delete("delete from " + rs.pswdTable + " where ip='192.168.10.217'");
//		rs.querySQL("select ip,user,newPswd  from " + rs.pswdTable + " where substr(lastScan, 0, 11)='2016-01-04'", true, null);
		Result rst = null;// rs.getInfo();
//		rs.doExec("ps -ef|grep passwd|grep -v grep|cut -c 9-15|xargs kill -9");
		rst = rs.doExec( 
				"ls /MyWork/Project/logs/*_" + rs.getTime("yyyyMMdd") + "*.log",
				"ulimit -n",
				"free -m");
//		try
//        {
		if(null != rst)
	        rs.info(rst.sysout);
//        } catch (Exception e)
//        {
//        		rs.info(e);
//        }
//		for(int i = 0; i < 100; i++)
//			rs.info(rs.getPswd());
		rs.destroy();
//		
//		rs = new RemoutSSH("192.168.10.115", "root", "Goolge+_)=-0");
//		rst = rs.doExec("ps -ef");
//		rs.info(rst.sysout);
//		rs.destroy();
	}

}
