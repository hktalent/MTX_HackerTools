package com.mtx.xiatian;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mtx.xiatian.db.StreamGobbler;

public class MyNMapTest
{

	public static String	path	= "./tmp/";

	/**
	 * 获取头信息
	 * 
	 * @param http
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static Map<String, String> getHttpResponseHeader(HttpURLConnection http) throws UnsupportedEncodingException
	{
		Map<String, String> header = new LinkedHashMap<String, String>()
		{
			public String get(Object key)
			{
				return super.get(key.toString().toLowerCase());
			}
		};
		String mine, key;
		for (int i = 0;; i++)
		{
			mine = http.getHeaderField(i);
			if (mine == null)
				break;
			key = http.getHeaderFieldKey(i);
			if (null != key)
				header.put(key.toLowerCase(), mine);
		}
		return header;
	}

	/**
	 * 下载给定的url
	 */
	public static synchronized void getUrlStr(String url1, String szCookie, OutputStream out, ICallBack cbk)
	{
		url1 = url1.trim();
		URL url = null;
		HttpURLConnection conn = null;
		InputStream isN = null;
		try
		{
			url = new URL(url1);
			conn = (HttpURLConnection) url.openConnection();
			// conn.setReadTimeout(1333);
			if (null != szCookie)
				conn.setRequestProperty("Cookie", szCookie);
			conn.setRequestProperty("DNT", "1");
			conn.setRequestProperty("Cache-Control", "max-age=0");
			conn.setRequestProperty("Referer", url1);
			conn.setRequestProperty("Host", url1.replaceAll("(.*?:\\/\\/)|(\\/.*$)", ""));
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (I fuck XXX you) JB XXX");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			// conn.setInstanceFollowRedirects(true);
			conn.setDoInput(true);
			conn.setDoOutput(false);
			String szFileName = null;
			if (null == out)
			{
				Map<String, String> header = getHttpResponseHeader(conn);
				if (null != cbk)
					cbk.doMap(header);
				szFileName = header.get("Content-Disposition");
				if (null != szFileName)
					szFileName = java.net.URLDecoder.decode(szFileName.substring("attachment;  filename=".length()), "UTF-8");
				else
				{
					// System.out.println("no File: " + url1);
					return;
				}
				File file = new File(path + szFileName);
				// 文件已经存在
				if (file.exists())
				{
					System.out.println("文件已经存在，跳过下载: " + szFileName);
					return;
				}
				out = new FileOutputStream(file);
			}
			isN = conn.getInputStream();

			byte[] b = new byte[1024];
			int i = 0;
			long l = 0;
			while (-1 < (i = isN.read(b, 0, 1024)))
			{
				out.write(b, 0, i);
				l += i;
			}
			System.out.println("Ok(" + l + "字节): " + szFileName);
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != out)
			{
				try
				{
					out.flush();
					out.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			if (null != isN)
				try
				{
					isN.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			conn.disconnect();
		}
	}

	/**
	 * 获取域名的ip Map m = getIp("erp.yinhai.com"); System.out.println(m.get("IP"));
	 * System.out.println(m.get("IPNum")); System.out.println(m.get("IPDZ"));
	 * http://ip.chinaz.com/?IP=erp.yinhai.com <span class="info3"
	 * >您的IP:[<strong class="red">118.112.188.111</strong>] 来自:<strong>四川省成都市
	 * 电信</strong> 操作系统:<strong>Unknown</strong><span id="resolution"
	 * style=" margin-left:5px;"></span> 语言:<strong>zh-CN</strong><br />
	 * 浏览器:<strong>Chrome</strong> Mozilla/5.0 (Macintosh; Intel Mac OS X
	 * 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94
	 * Safari/537.36 </span>请输入IP或域名： <input id="address" isget="false"
	 * type="text" value="erp.yinhai.com" autocomplete="off" url="true"
	 * class="input" size="40" title="多个查询用,或|隔开"/> <input id="submit1"
	 * name="button" isget="false" type="submit" class="but" value="查 询" />
	 * <span id="status" class="info1"> <strong class="red">查询结果[1]:
	 * 118.112.188.108 ==>> 1987099756 ==>> 四川省成都市 电信</strong><br />
	 * 
	 * 上面三项依次显示的是 : 获取的IP地址 ==>> 数字地址 ==>> IP的物理位置<br />
	 * 
	 * </span> <span id="gadsense" class="info1"></span> </div>
	 * 
	 * @param s
	 * @return
	 */
	public static Map getIp(String s)
	{
		Map m = new HashMap();
		s = s.replaceAll("(^.*?:)|(/.*$)", "");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getUrlStr("http://ip.chinaz.com/?IP=" + s, null, out, null);
		String s1 = "", s2 = "", s3 = "";
		try
		{
			s1 = new String(out.toByteArray(), "UTF-8");
			s2 = "您的IP:[<strong class=\"red\">";
			s3 = "上面三项依次显示的是";
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		int n = s1.indexOf(s2);
		if (-1 < n)
		{
			s1 = s1.substring(n + s2.length());

			n = s1.indexOf(s3);
			if (-1 < n)
			{
				s1 = s1.substring(0, n);
				// 您的IP:\\[<strong class=\"red\">(.*?)<\\/strong>\\][.\\r\\n]*?
				Pattern p = Pattern.compile("查询结果\\[\\d*?\\]:\\s*(.*?)\\s*==>>\\s*(\\d*?)\\s*==>>\\s*(.*?)\\s*<", Pattern.MULTILINE);
				Matcher m1 = null;
				if ((m1 = p.matcher(s1)).find())
				{
					int i = 1;
					// m.put("YouIP", m1.group(i++));
					m.put("IP", m1.group(i++));
					m.put("IPNum", m1.group(i++));
					m.put("IPDZ", m1.group(i++));
				}
				// System.out.println(s1);
			}
		}
		return m;
	}

	/**
	 * 查询给定的ip地址的所有域名 http://www.sameip.org/118.112.188.108
	 * 
	 * @param ip
	 * @return
	 */
	public static List getDomainForIp(String ip)
	{
		List lst = new ArrayList();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getUrlStr("http://www.sameip.org/" + ip, null, out, null);
		String s1 = "";
		try
		{
			s1 = new String(out.toByteArray(), "UTF-8");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		Pattern p = Pattern.compile("<div class=\"col-sm-6\">\\s*<ol>\\s*(?:<li>(.*?)<\\/li>)*\\s*<\\/ol>\\s*<\\/div>*", Pattern.MULTILINE);
		Matcher m1 = null;
		if ((m1 = p.matcher(s1)).find())
		{
			int i = 1;
			lst.add(m1.group(i++));
		}
		return lst;
	}

	public static String getWebServerInfo(String s)
	{
		ICallBack cbk = new ICallBack()
		{
			Map<String, String>	m;

			public void doMap(Map<String, String> m1)
			{
				m = m1;
			}

			public Map<String, String> getMap()
			{
				return m;
			}
		};

		getUrlStr(s, null, null, cbk);
		Map<String, String> m1 = cbk.getMap();
		if (null != m1)
		{
			return m1.get("Server");
		}
		return null;
	}

	/**
	 * 获取java运行环境加载的jar信息
	 * 
	 * @return
	 */
	public static String getJVMLoadJarInfo()
	{
		return System.getProperty("sun.boot.class.path");
	}

	/**
	 * 项目中class类及jar的加载顺序信息的获取
	 * 
	 * @return
	 */
	public static String getWebProjectLoadJarInfo()
	{
		return System.getProperty("java.class.path");
	}

	/**
	 * 执行命令、返回结果
	 * 
	 * @return
	 */
	public static String getCmdResult(String[] arg)
	{
		String[] a = arg;
		Process p = null;
		ByteArrayOutputStream out = null;
		InputStream in = null;
		OutputStream outCmd = null;
		String s = null;
		try
		{
			out = new ByteArrayOutputStream();
			p = Runtime.getRuntime().exec(a);
			p.waitFor();
//            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
//            errorGobbler.start();
			// 命令行信息
			// System.out.println(StringUtils.join(a, " ").toString());
            in = p.getInputStream();
//			outCmd.flush();
			
            
			byte[] b = new byte[1024];
			int i = 0;
			while (-1 < (i = in.read(b, 0, b.length)))
			{
				out.write(b, 0, i);
			}
			b = out.toByteArray();
			s = new String(b, "UTF-8");
			
			
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try{if(null != outCmd)outCmd.close();}catch (Exception e){e.printStackTrace();}
			try{if(null != in)in.close();}catch (Exception e){e.printStackTrace();}
			try{if(null != out){out.close();}}catch (Exception e){e.printStackTrace();}
			if(null != p)p.destroy();
		}

		return s;
	}
	public static String getCmdResult(String[] arg, String szWrite)
	{
		String[] a = arg;
		Process p = null;
		ByteArrayOutputStream out = null;
		InputStream in = null;
		OutputStream outCmd = null;
		String s = null;
		try
		{
			out = new ByteArrayOutputStream();
			p = Runtime.getRuntime().exec(a);
			
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            errorGobbler.start();
            
			p.waitFor();
			if(null != szWrite)
			{
				outCmd = p.getOutputStream();
				outCmd.write(szWrite.getBytes());
				outCmd.close();
				outCmd = null;
			}
            
			// 命令行信息
			// System.out.println(StringUtils.join(a, " ").toString());
            in = p.getInputStream();
//			outCmd.flush();
			
            
			byte[] b = new byte[1024];
			int i = 0;
			while (-1 < (i = in.read(b, 0, b.length)))
			{
				out.write(b, 0, i);
			}
			b = out.toByteArray();
			s = new String(b, "UTF-8");
			
			
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try{if(null != outCmd)outCmd.close();}catch (Exception e){e.printStackTrace();}
			try{if(null != in)in.close();}catch (Exception e){e.printStackTrace();}
			try{if(null != out){out.close();}}catch (Exception e){e.printStackTrace();}
			if(null != p)p.destroy();
		}

		return s;
	}

	

	/**
	 * 获取web server服务器信息，以及开启的端口信息
	 * 
	 * @return
	 */
	public static String getWebServerSysInfo(String ip)
	{
		try
		{
			// 1、-sV 检查端口、版本信息
			// 2、获取系统信息 -O, 默认会获取端口
			// sudo nmap --system-dns -O 192.168.10.0/24
			// 3、路由跟踪
			// sudo nmap --system-dns --traceroute 192.168.10.149
			// 4、 -sP 可以检测到Mac地址
			String s1 = getCmdResult(new String[]
			{ "sudo", "/opt/local/bin/nmap", "-sV", ip }), s2 = "";
			StringBuffer sb = new StringBuffer("PORT     STATE SERVICE    VERSION\n");
			Pattern p1 = Pattern.compile("((\\d{1,}\\/)|(Service Info)).*?\n", Pattern.MULTILINE);
			Matcher m = p1.matcher(s1);
			int n = sb.length();
			while (m.find())
			{
				s2 = m.group(0);
				if (-1 < s2.indexOf("open") || -1 < s2.indexOf("Service Info"))
					sb.append(s2);
			}
			if (sb.length() == n)
				return "";
			return sb.toString();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 提交安全渗透测试的数据到服务器
	 * 
	 * @param type
	 * @param data
	 */
	public static void postData(String type, String data, String szCookie)
	{
		try
		{
			String szUrl = "http://192.168.8.178:8080/Selenium/test/softTest.servlet";
			URL url = new URL(szUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 设置是否向connection输出，因为这个是post请求，参数要放在
			// http正文内，因此需要设为true
			connection.setDoOutput(true);
			// Set the post method. Default is GET
			connection.setRequestMethod("POST");
			// Post 请求不能使用缓存
			connection.setUseCaches(false);
			connection.setRequestProperty("Cookie", szCookie);
			// 进行编码
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.connect();
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = type + "=" + URLEncoder.encode(data, "UTF-8");
			out.writeBytes(content);
			out.flush();
			out.close();
			connection.disconnect();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 登录返回cookie
	 * 
	 * @param url
	 * @param user
	 * @param pswd
	 * @return
	 */
	public static String login(String url, String user, String pswd)
	{
		;
		return null;
	}

	public static void log(Object... arg)
	{
		StringBuffer sb = new StringBuffer();
		for (Object s : arg)
			sb.append(String.valueOf(s));
		sb.append("\r\n");
		System.out.print(sb.toString());
	}

	public static String getUrlMd5(String url)
	{
		return MD5Util.MD5(url);
	}

	/**
	 * 处理一个url的主机安全扫描
	 * 
	 * @param url
	 */
	public static void doUrlPortSafe(String url)
	{
		new File("md5/").mkdirs();
		File f = new File("md5/" + getUrlMd5(url));
		if (f.exists())
		{
			// log("历史中已经处理：", url, " ", f.getAbsolutePath());
			// log(readFile(f));
			return;
		}
		int nPort = 80;
		if (url.startsWith("https:"))
			nPort = 443;
		Pattern p = Pattern.compile(":(\\d+)", Pattern.DOTALL);
		Matcher m = p.matcher(url);
		if (m.find())
		{
			nPort = Integer.parseInt(m.group(1));
		}
		log("端口：", nPort);
		p = Pattern.compile("\\/\\/([^:\\/]+)", Pattern.DOTALL);
		m = p.matcher(url);
		if (m.find())
		{
			log("主机：", m.group(1));
			String szInfo = getWebServerSysInfo(m.group(1));
			if (null == szInfo || 0 == szInfo.length())
				return;
			// szInfo = szInfo.replaceAll("(\n(" + nPort +
			// "|80|443)\\/.*\r)|\\?","");
			szInfo = szInfo.replaceAll("(\n(" + nPort + "|80|443)\\/.*?\r)|\\?", "");
			// log(szInfo);
			writeFile(f, url + "\r\n" + szInfo);
		}
	}

	/**
	 * 写文件
	 * 
	 * @param f
	 * @param s
	 */
	public static void writeFile(File f, String s)
	{
		try
		{
			FileOutputStream out = new FileOutputStream(f);
			out.write(s.getBytes("UTF-8"));
			out.flush();
			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 读文件
	 * 
	 * @param f
	 * @return
	 */
	public static String readFile(File f)
	{
		try
		{
			FileInputStream in = new FileInputStream(f);
			int nLen = (int) f.length();
			byte[] b = new byte[nLen + 1];
			int i = 0, j;
			while (-1 < (j = in.read(b, i, nLen)))
			{
				i += j;
				if (i >= nLen)
					break;
				// nLen -= j;
			}
			in.close();
			return new String(b, "UTF-8");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 批量处理所有server的安全监察
	 */
	public static void doGetAllInfo()
	{
		String[] a = readFile(new File("javacode/evolveData/com/mtxt/evolvedata/xiatian/md5/urls.txt")).split("\n");
		for (int i = 0, j = a.length; i < j; i++)
		{
			a[i] = a[i].replaceAll("\\s*$", "").trim();
			if (0 < a[i].length())
			{
				log("开始处理：", a[i]);
				doUrlPortSafe(a[i]);
			}
		}
	}

	/**
	 * 批量处理所有server的安全监察
	 */
	public static void createReportAllInfo()
	{
		String[] a = readFile(new File("md5/urls.txt")).split("\n");
		for (int i = 0, j = a.length; i < j; i++)
		{
			a[i] = a[i].replaceAll("\\s*$", "").trim();
			if (0 < a[i].length())
			{
				log("开始处理：", a[i]);
				doUrlPortSafe(a[i]);
			}
		}
	}
	
	
	public static void doScp(String src, String des)
	{
		src = "/Volumes/MyWork/MyWork/JY15MD002SK/04.Implement/lib/SafeGene.jar";
		des = "root@192.168.10.115:/MyWork";
		// /usr/bin/scp
		String s1 = getCmdResult(new String[]
				{"/usr/bin/scp", "-r", src, des }, "Goolge+_)=-0\n");
		System.out.println(s1);
	}

	/**
	 * post 1、sysinfo=系统信息 2、webinfo=web容器信息 我会吧这些信息传给你的url
	 * 
	 * nikto -h 118.112.188.108 -p 8070 -c /QIMS nikto -C all  -T
	 * 0123456789abgx  -h 118.112.188.108 -p 8082 -o result.txt
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		MyNMapTest nmt = new MyNMapTest();
		String s = nmt.getCmdResult(new String[]{"expect -c \"spawn sudo nmap --system-dns -Pn -sS 192.168.10.186\r\nexpect {\\\"*assword\\\" {set timeout 300; send \\\"Miracl~!@`12\\r\\\";}\r\n}\r\nexpect eof\""});
		System.out.println(s);
//		MyNMapTest.doScp(null, null);
//		MyNMapTest.doAllNmap();
//		MyNMapTest.getMacs("192.168.10.194", new ArrayList<TreeMap<String, String>>());
		//
//		doGetAllInfo();
		// doUrlPortSafe("http://gx.si.gov.cn:8005/gxsi");
		// 获取域名对应的ip地址
		// Map m = getIp("erp.yinhai.com");
		// System.out.println(m.get("YouIP"));
		// System.out.println(m.get("IP"));
		// System.out.println(m.get("IPNum"));
		// System.out.println(m.get("IPDZ"));

		// 获取ip地址对应的域名(s)
		// List lst = getDomainForIp(m.get("IP").toString());
		// for(Object s:lst.toArray())
		// // if(0 < lst.size())
		// System.out.println(s.toString());

		// 获取web容器信息
		// System.out.println(getWebServerInfo("http://erp.yinhai.com:8082/login.aspx"));

		// System.out.println(getWebServerSysInfo("192.168.8.178"));
		// http://192.168.8.37:9090/QIMS/qims/qiOrgAction!saveUser.do?id=9969&name=%E6%9D%A8%E6%8C%AF%E5%8D%8E&departManager=2&available=1
		// http://192.168.8.37:9090/QIMS/qims/qiOrgAction!saveUser.do?id=9969&name=杨振华&departManager=2&available=1
		/*Nmap scan report for 192.168.10.149
Host is up (0.0014s latency).
Not shown: 989 closed ports
PORT      STATE SERVICE       VERSION
135/tcp   open  msrpc         Microsoft Windows RPC
139/tcp   open  netbios-ssn
445/tcp   open  netbios-ssn
3389/tcp  open  ms-wbt-server Microsoft Terminal Service
5357/tcp  open  http          Microsoft HTTPAPI httpd 2.0 (SSDP/UPnP)
|_http-methods: No Allow or Public header in OPTIONS response (status code 503)
|_http-title: Service Unavailable
49152/tcp open  msrpc         Microsoft Windows RPC
49153/tcp open  msrpc         Microsoft Windows RPC
49154/tcp open  msrpc         Microsoft Windows RPC
49155/tcp open  msrpc         Microsoft Windows RPC
49156/tcp open  msrpc         Microsoft Windows RPC
49157/tcp open  msrpc         Microsoft Windows RPC
Device type: general purpose
Running: Microsoft Windows 7|2008
OS CPE: cpe:/o:microsoft:windows_7::- cpe:/o:microsoft:windows_7::sp1 cpe:/o:microsoft:windows_server_2008::sp1 cpe:/o:microsoft:windows_8
OS details: Microsoft Windows 7 SP0 - SP1, Windows Server 2008 SP1, or Windows 8
Network Distance: 2 hops
Service Info: OS: Windows; CPE: cpe:/o:microsoft:windows

Host script results:
|_nbstat: NetBIOS name: YINHAI-PC, NetBIOS user: <unknown>, NetBIOS MAC: ba:eb:b7:a9:51:6b (unknown)
| smb-os-discovery: 
|   OS: Windows 7 Ultimate 7601 Service Pack 1 (Windows 7 Ultimate 6.1)
|   OS CPE: cpe:/o:microsoft:windows_7::sp1
|   Computer name: yinhai-PC
|   NetBIOS computer name: YINHAI-PC
|   Workgroup: WORKGROUP
|_  System time: 2015-12-18T13:16:59+08:00
| smb-security-mode: 
|   Account that was used for smb scripts: guest
|   User-level authentication
|   SMB Security: Challenge/response passwords supported
|_  Message signing disabled (dangerous, but default)
|_smbv2-enabled: Server supports SMBv2 protocol

TRACEROUTE (using port 1723/tcp)
HOP RTT     ADDRESS
1   ...
2   1.28 ms 192.168.10.149
*/
	}

}
