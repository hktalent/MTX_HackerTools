package com.mtx.xiatian.hacker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 设置cookie自动下载、更新公司知识库
 */ 
public class MyDownloadWithCookie {
	
	// "./doc/"
	public static String path = "/Users/xiatian/Library/Mobile Documents/com~apple~CloudDocs/公司知识库文档/";

//	GET http://erp.yinhai.com:8085/zlwdgl/webfrm_zlwd_wjxz.aspx?wjbh=31 HTTP/1.1
//		Host: erp.yinhai.com:8085
//		Proxy-Connection: keep-alive
//		Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
//		User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36
//		DNT: 1
//		Accept-Encoding: gzip,deflate,sdch
//		Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.6
//		Cookie: ASP.NET_SessionId=nejwtnicx4bk5y45gzabcuff; yinhai.ygyzm=obwv7qZYOTc=; yinhai.yzm=A68D0605227830FB7CD32236CDED4425
///////////////////////////////////	
//	HTTP/1.1 200 OK
//	Cache-Control: private
//	Transfer-Encoding: chunked
//	Content-Type: application/octet-stream
//	Server: Microsoft-IIS/7.5
//	X-AspNet-Version: 2.0.50727
//	Content-Disposition: attachment;  filename=11-JYYH-YZ%e6%97%a5%e5%b8%b8%e7%ae%a1%e7%90%86%e8%a1%a8%e6%a0%bcISO%e5%b7%a5%e5%85%b7%e5%8c%85.rar
//	X-Powered-By: ASP.NET
//	Date: Thu, 23 Jul 2015 06:20:44 GMT
	/**
	 * 
	 */ 
	public static synchronized  void getUrlStr(String url1, String szCookie, OutputStream out, ICallBack cbk)
	{
		url1 = url1.trim();
		URL url = null;
		HttpURLConnection conn = null;
		InputStream isN = null;
		try
		{
			url = new URL(url1);
			conn = (HttpURLConnection)url.openConnection();
//			conn.setReadTimeout(1333);
			if(null != szCookie)
				conn.setRequestProperty("Cookie", szCookie);
			conn.setRequestProperty("DNT","1");
			conn.setRequestProperty("Cache-Control","max-age=0");
			conn.setRequestProperty("Referer",url1);
			conn.setRequestProperty("Host", url1.replaceAll("(.*?:\\/\\/)|(\\/.*$)", ""));
			conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (I fuck XXX you) JB XXX");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
//			conn.setInstanceFollowRedirects(true);
	        conn.setDoInput(true);
	        conn.setDoOutput(false);
	        String szFileName = null;
	        if(null == out)
	        {
		        Map<String, String> header = getHttpResponseHeader(conn);
		        if(null != cbk)cbk.doMap(header);
		        szFileName = header.get("Content-Disposition");
		        if(null != szFileName)szFileName = java.net.URLDecoder.decode(szFileName.substring("attachment;  filename=".length()), "UTF-8");
		        else 
		        {
//		        		System.out.println("no File: " + url1);
		        		return;
		        }
		        File file = new File(path + szFileName);
		        // 文件已经存在
		        if(file.exists())
		        {
		        		System.out.println("文件已经存在，跳过下载: " + szFileName);
		        		return;
		        }
		        out = new FileOutputStream(file);
	        }
	        isN = conn.getInputStream();
	        
	        byte []b = new byte[1024];
	        int i = 0;
	        long l = 0;
	        while(-1 < (i = isN.read(b, 0, 1024)))
	        {
	        		out.write(b, 0, i);
	        		l += i;
	        }
	        System.out.println("Ok(" + l + "字节): " + szFileName);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(null != out)
			{
				try{
					out.flush();
					out.close();
				}catch(Exception e){e.printStackTrace();}
			}
			if(null != isN)
				try{isN.close();}catch(Exception e){e.printStackTrace();}
			conn.disconnect();
		}
	}
	
//	private static void printResponseHeader(HttpURLConnection http) throws UnsupportedEncodingException {
//        Map<String, String> header = getHttpResponseHeader(http);
//        for (Map.Entry<String, String> entry : header.entrySet()) {
//            String key = entry.getKey() != null ? entry.getKey() + ":" : "";
//            System.out.println(key + entry.getValue());
//        }
//    }
     
    private static Map<String, String> getHttpResponseHeader(
            HttpURLConnection http) throws UnsupportedEncodingException {
        Map<String, String> header = new LinkedHashMap<String, String>(){
        	public String get(Object key)
        	{
        		return super.get(key.toString().toLowerCase());
        	}
        };
        String mine, key;
        for (int i = 0;; i++) {
            mine = http.getHeaderField(i);
            if (mine == null)
                break;
            key = http.getHeaderFieldKey(i);
            if(null != key)
            header.put(key.toLowerCase(), mine);
        }
        return header;
    }
    
    /**
     * 下载所有知识库文章
     * @param szCookie
     */
    public static void downloadAllDoc(String szCookie)
    {
    	    for(int i = 0; i < 30000; i++)
    	    		getUrlStr("http://erp.yinhai.com:8085/zlwdgl/webfrm_zlwd_wjxz.aspx?wjbh=" + i, szCookie, null, null);
    }
    
	/**
	 * 批量自动下载公司知识库，免得每次去查找很不方便
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws Exception 
	{
		System.setProperty("java.net.useSystemProxies", "true");
		// http://erp.yinhai.com:8085/zlwdgl/WebFrm_cxwj.aspx
		// http://erp.yinhai.com:8085/zlwdgl/webfrm_zlwd_wjxz.aspx?wjbh=31
//		OutputStream out = null;// new FileOutputStream(new File("./q.doc"));
//		getUrlStr("http://erp.yinhai.com:8085/zlwdgl/webfrm_zlwd_wjxz.aspx?wjbh=31", "ASP.NET_SessionId=nejwtnicx4bk5y45gzabcuff; yinhai.ygyzm=obwv7qZYOTc=; yinhai.yzm=A68D0605227830FB7CD32236CDED4425", out);
		downloadAllDoc("ASP.NET_SessionId=4jjezq3j1qsrpt45shcobu55; sid=5erjbk7n5gf1qcflf8gjvvvee7; yinhai.yzm=2643B5C9650071CEC5C6B314EFE1A1C1");
		
//		System.out.println(java.net.URLDecoder.decode("%257BDefault.aspx%253Fzsbh%253D208%2526wjbh%253D208%2526zlwj%253Dy%2526id%253D%255B*%252C0%255D%252C6%257D&Scale=0.9&ZoomTransition=easeOut&ZoomTime=0.5&ZoomInterval=0.2&FitPageOnLoad=true&FitWidthOnLoad=true&ProgressiveLoading=true&MinZoomSize=0.2&MaxZoomSize=5&InitViewMode=Portrait&ViewModeToolsVisible=true&ZoomToolsVisible=true&NavToolsVisible=true&CursorToolsVisible=true&SearchToolsVisible=true&localeChain=zh_CN", "UTF-8"));
	}

}
