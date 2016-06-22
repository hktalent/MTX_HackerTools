package com.mtx.xiatian.hacker;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多线程下载
 * @author xiatian
 */
public class DownloadWithCurl
{

	public static void clean(final Object buffer) throws Exception {
		if(null == buffer)return;
		AccessController.doPrivileged(new PrivilegedAction() {  
			  public Object run() {  
			    try {  
			      Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);  
			      getCleanerMethod.setAccessible(true);  
			      sun.misc.Cleaner cleaner = (sun.misc.Cleaner)   
			      getCleanerMethod.invoke(buffer, new Object[0]);  
			      cleaner.clean();  
			    } catch (Exception e) {  
			      e.printStackTrace();  
			    }  
			    return null;  
			  }  
			}); 
		}
	/**
	 * 内部启动线程下载文件
	 * @param url1
	 * @param fileName
	 * @param nS
	 * @param nE
	 */
	public void doDownload(final String url1, final String fileName, final long nS, final long nE)
	{
		try
        {
	        Thread.sleep(13);
        } catch (Exception e1)
        {
	        doExcpetion(e1);
        }
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				URL url = null;
				HttpURLConnection conn = null;
				RandomAccessFile raf = null;
				FileChannel fc = null;
				InputStream isN = null;
				MappedByteBuffer out = null;
				Map <String,String>headers = new HashMap<String,String>();
				if(bAcceptRanges)
				{
					headers.put("Range", "bytes=" + nS + "-" + nE);
					System.out.println("开始下载：" + headers.get("Range"));
				}
				String []aHds = {"DNT", "1",
						"Cache-Control", "max-age=0",
						"Connection", "Close",
						"Referer", url1,
						"Host", url1.replaceAll("(.*?:\\/\\/)|(\\/.*$)", ""),
						"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
						"User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
						"Accept-Charset", "UTF-8"};
				// 头信息处理，避免冲突，不覆盖外部设置的头信息
				for(int i = 0, j = aHds.length; i< j; i+=2)
				{
					if(headers.containsKey(aHds[i]))
						continue;
					headers.put(aHds[i], aHds[i + 1]);
				}
				try
				{
					raf = new RandomAccessFile(fileName, "rw");
					fc = raf.getChannel();
					url = new URL(url1);
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(10*1000);
					conn.setReadTimeout(3333);
					conn.setUseCaches(false);
					conn.setDefaultUseCaches(false);
					Iterator it = headers.keySet().iterator();
					String k;
					while(it.hasNext())
					{
						conn.setRequestProperty(k = it.next().toString(), headers.get(k));
					}
					conn.setDoInput(true);
					conn.setDoOutput(false);
//					raf.seek(nS);
					conn.connect();
					isN = conn.getInputStream();
				    // 每次映射8M，进行处理
					int nBufSize = 1024* 1024 * 8;
					byte[] b = new byte[nBufSize];
					int i = 0;
					long nCnt = 0;
					while (-1 < (i = isN.read(b, 0, nBufSize)))
					{
						out =  fc.map(FileChannel.MapMode.READ_WRITE, nS + nCnt, i);
						out.put(b, 0, i);
						out.force();
						try
	                    {
		                    clean(out);
	                    } catch (Exception e1)
	                    {
	                    		doExcpetion(e1);
	                    }
						nCnt += i;
					}
					
					isN.close();
					isN = null;
					System.out.println("下载结束: "  + nS + " - " + nE);
				} catch (Exception e)
				{
					doExcpetion(e);
				} finally
				{ 
					try
                    {
	                    clean(out);
                    } catch (Exception e1)
                    {
                    	doExcpetion(e1);
                    }
					if (null != fc)
					{
						try
						{
							fc.close();
						} catch (Exception e)
						{
							doExcpetion(e);
						}
					}
					if (null != isN)
						try
						{
							isN.close();
						} catch (Exception e)
						{
							doExcpetion(e);
						}
					conn.disconnect();
				}
			}
		});
		thread.setName("Thread_" + nS + "-" + nE);
		thread.start();
	}
	/**
	 * 获取头信息
	 * @param http
	 * @return
	 */
	private Map<String, String> getHttpResponseHeader(
            HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>(){
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
            if(null != key)
            {
//            		System.out.println(key.toLowerCase() + " = "+ mine);
            		header.put(key.toLowerCase(), mine);
            }
        }
        return header;
    }
	
	/**
	 * 是否允许：Accept-Ranges
	 */
	boolean bAcceptRanges = true;
	/**
	 * <pre>
	 * 获取下载文件
server = Apache
accept-ranges = bytes
content-type = application/octet-stream
last-modified = Sun, 22 May 2016 09:49:24 GMT
etag = "e19cebf0a5291cbdbd337c67458c2092:1463910596"
content-length = 111597524
date = Mon, 23 May 2016 07:55:10 GMT
connection = close
</pre>
	 * @param url1
	 * @param nM 每个线程字节数大小
	 */
	public synchronized void getUrlStr(String url1, int nM)
	{
		System.setProperty("java.net.useSystemProxies", "true");
		String szName = "./" + url1.substring(url1.lastIndexOf("/") + 1);
		
		url1 = url1.trim();
		URL url = null;
		HttpURLConnection conn = null;
		long nSize =  1024 * 1024 * nM;
		
		try
		{
			url = new URL(url1);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Cache-Control", "max-age=0");
			// keep-alive
//			conn.setRequestProperty("Connection", "close");
			
			Map<String, String> header = getHttpResponseHeader(conn);
			if(0 == header.size())
			{
				System.err.println("下载失败！");
				return;
			}
			if(null == header.get("content-length"))
				System.err.println("未找到 content-length！");
			else System.err.println("找到 content-length:" + header.get("content-length"));
			
			long nFileSize = null == header.get("content-length") ? nSize: Long.parseLong(header.get("content-length"));
			conn.disconnect();
			// 允许分段下载： Content-Range 片段缓存
			if(null != header.get("Accept-Ranges"))
			{
				System.out.println("允许分段下载！ ");
				this.bAcceptRanges = true;
				for(long i = -1L; i < nFileSize; i+= nSize)
				{
					doDownload(url1, szName, i + 1, Math.min(i + nSize, nFileSize));
				}
			}
			else
			{
				System.err.println("不支持分段下载 ");
				doDownload(url1, szName, 0, nFileSize);
			}
		} catch (Exception e)
		{
			doExcpetion(e);
		} finally
		{
			conn.disconnect();
		}
	}
	
	public void doExcpetion(Exception e)
	{
		System.out.println(Thread.currentThread().getName() + ": ");
		e.printStackTrace();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new DownloadWithCurl().getUrlStr("http://osx.metasploit.com/metasploitframework-latest.pkg", 20);
//		new DownloadWithCurl().getUrlStr("http://baike.baidu.com/view/342424241.htm", 1);
//		new DownloadWithCurl().getUrlStr("http://erp.yinhai.com:8082/login.aspx", 1);
	}

}
