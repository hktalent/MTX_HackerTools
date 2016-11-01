package com.mtx.safegene.test.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

public class UrlTestTool
{
	private HttpClient httpclient;
	private String	   szCookie;
	private String	   charset	= "UTF-8";

	public UrlTestTool()
	{
		httpclient = new DefaultHttpClient();
	}

	Pattern pCharset = Pattern.compile("content=\"text/html;\\s*charset=([^\"]+)\"", Pattern.DOTALL);
	Pattern pCharset1 = Pattern.compile("<meta\\s*charset=\"([^\"]+)\">", Pattern.DOTALL);
	
	/**
	 * 从文件内容获取字符集
	 * @param s
	 * @param szDft
	 * @return
	 */
	public String getCharSetFromHtml(String s, String szDft)
	{
		Matcher m = pCharset.matcher(s);
		if(m.find())
		{
			return m.group(1);
		}else if((m = pCharset1.matcher(s)).find())
			return m.group(1);
		return szDft;
	}
	
	/**
	 * 获取返回结果
	 * 
	 * @param httpresponse
	 * @return
	 */
	public void getResponse(HttpResponse httpresponse, StringBuffer sb, OutputStream out1, String charset, Map<String, String> header)
	{
		try
		{
			if (null == httpresponse || null == httpresponse.getEntity())
				return;
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			OutputStream out = null == out1 ? bo : out1;
			byte[] a = new byte[4096];
			InputStream in = httpresponse.getEntity().getContent();
			if("gzip".equalsIgnoreCase(header.get("Content-Encoding")))
			{
				in = new GZIPInputStream(in);
			}
			int i = 0;
			while (-1 < (i = in.read(a, 0, 4096)))
				out.write(a, 0, i);
			in.close();out.flush();
			out.close();
			if(null == out1 && 0 < bo.size())
			{
				// 修正字符集
				String szCtt = new String(bo.toByteArray(),  charset);
				String szC = getCharSetFromHtml(szCtt, charset).trim();
				if(!szC.equals(charset))
					szCtt = new String(bo.toByteArray(),  szC);
				sb.append(szCtt);
			}
			
			// // 取出响应内容
			// BufferedReader in = new BufferedReader(new InputStreamReader(
			// "UTF-8"));
			// String line = "";
			// String NL = System.getProperty("line.separator");
			// while ((line = in.readLine()) != null)
			// {
			// sb.append(line + NL);
			// }
			// // 关闭流
			// in.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * 请求、返回的数据呈现
	 * 1.若o是header[],就以:链接到存储到sbProcess
	 * 2.若o是Map<String,Object>,就以=链接到存储到sbProcess
	 * 3.若o是Strig,就直接存储到sbProcess
	 * 4.若o是throwable,就以流方式转换为String,直接存储到sbProcess
     * 5.其它情况，直接存储到sbProcess
     * </pre>
	 * @param o
	 * @param sbProcess
	 */
	public void doReport(Object o, StringBuffer sbProcess)
	{
		if (null != sbProcess)
		{
			if (o instanceof Header[])
			{
				Header[] hs = (Header[]) o;
				String n, v;
				for (Header h : hs)
				{
					n = h.getName();
					v = h.getValue();
					if ("SafeGene_Msg".equalsIgnoreCase(n))
						try
						{
							v = java.net.URLDecoder.decode(v, "UTF-8");
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					sbProcess.append(n).append(": ").append(v).append("\n");
				}
				hs = null;
			} else if (o instanceof Map)
			{
				@SuppressWarnings("unchecked")
				Map<String, Object> mParams = (Map<String, Object>) o;
				Object v = null;
				String k;
				Iterator<String> i = mParams.keySet().iterator();
				while (i.hasNext())
				{
					try
					{
						k = i.next();
						//sbProcess.append(java.net.URLEncoder.encode(k, charset)).append("=");
						sbProcess.append(k).append("=");
						v = mParams.get(k);
						if (v instanceof File)
							v = ((File) v).getAbsolutePath();
						//sbProcess.append(java.net.URLEncoder.encode(String.valueOf(v), charset)).append("&");
						sbProcess.append(String.valueOf(v)).append("&");
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}

			} else if (o instanceof String)
				sbProcess.append(o);
			else if (o instanceof Throwable)
			{
				Throwable e = (Throwable) o;
				try
				{
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					PrintStream p = new PrintStream(out);
					e.printStackTrace(p);
					p.close();
					out.flush();
					sbProcess.append(new String(out.toByteArray(), "UTF-8"));
					out.close();
				} catch (Exception e9)
				{
				}
			} else
				sbProcess.append(String.valueOf(o)).append("\n");

		}
	}

	private String getUrl(String s)
	{
		String[] a = s.split("\\?"), a1;
		if (1 < a.length)
		{
			StringBuffer sb = new StringBuffer(a[0] + "?");
			s = s.substring(a[0].length() + 1);
			a = s.split("&");
			for (String s1 : a)
			{
				a1 = s1.split("=");
				try
				{
					sb.append(java.net.URLEncoder.encode(a1[0], "UTF-8")).append("=");
					if (1 < a1.length)
						sb.append(java.net.URLEncoder.encode(a1[1], "UTF-8"));
					sb.append("&");
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			s = sb.toString();
		}
		if(s.endsWith("&"))s = s.substring(0, s.length() - 1);
		s = s.replaceAll("\"", "%22").replaceAll("</", "%2F%3C").replaceAll("<", "%3C").replaceAll(">", "%3E");
		s = s.replaceAll("\\(", "%28").replaceAll("\\)", "%29").replaceAll("\\\\", "%5C");
		s = s.replaceAll("\\[", "%5b").replaceAll("\\]", "%5d");
		if(-1 < s.indexOf("perl"))
		{
			;
//			System.out.println(s);
		}
		return s;
	}

	/**
	 * 执行reuqest请求，并将response的信息携带回来
	 * @param url
	 * @param key
	 * @param value
	 * @param map
	 * @param headers
	 * @param sbContent   返回的HTML内容
	 * @param bMultipart  
	 * @param p
	 * @param sbProcess 存储header等信息的容器,用于在报告中显示
	 * @throws Throwable 
	 */
	public void doPost(String url, String key, String value, Map<String, Object> map, Map<String, String> headers, StringBuffer sbContent,
	        boolean bMultipart, HttpUriRequest p, StringBuffer sbProcess) throws Throwable
	{
		doPost(url,  key,  value,  map,  headers,  sbContent,
		         bMultipart,  p,  sbProcess, null);
	}
	
	/**
	 * 字符集修正
	 * @param s
	 * @return
	 */
	public String checkCharset(String s, String szCharset)
	{
		String a[] = {"gb2312", "utf8", "UNICODE","ANSI","ASCII","us-ascii", "ISO-8859-1","gbk", "utf-8"};
		s = s.toLowerCase().trim();
		for(String k:a)
		{
			if(s.equals(k.toLowerCase()))return k;
		}
//		System.out.println(szCharset);
		return a[a.length - 1];
	}

	/**
	 *  Get或Post_f请求或Post_m请求方式提交
	 * @param url  request的请求地址
	 * @param key  request请求的键
	 * @param value request请求的值
	 * @param map  request请求的键值对
	 * @param headers 头信息
	 * @param sbContent   返回的内容信息
	 * @param bMultipart  是否是文件上传类型
	 * @param p           request
	 * @param sbProcess   临时存储Map或header的数据
	 * @param out1
	 * @throws Throwable 
	 */
	public void doPost(String url, String key, String value, Map<String, Object> map, Map<String, String> headers, StringBuffer sbContent,
	        boolean bMultipart, HttpUriRequest p, StringBuffer sbProcess, OutputStream out1) throws Throwable
	{
//		CookieSpecProvider easySpecProvider = new CookieSpecProvider() {  
//		    public CookieSpec create(HttpContext context) {  
//		  
//		        return new BrowserCompatSpec() {  
//		            @Override  
//		            public void validate(Cookie cookie, CookieOrigin origin)  
//		                    throws MalformedCookieException {  
//		                // Oh, I am easy  
//		            }  
//		        };  
//		    }  
//		  
//		};  
//		Registry<CookieSpecProvider> reg = RegistryBuilder.<CookieSpecProvider>create()  
//		        .register(CookieSpecs.BEST_MATCH,  
//		            new BestMatchSpecFactory())  
//		        .register(CookieSpecs.BROWSER_COMPATIBILITY,  
//		            new BrowserCompatSpecFactory())  
//		        .register("mySpec", easySpecProvider)  
//		        .build();  
//		  
//		RequestConfig requestConfig = RequestConfig.custom()  
//		        .setCookieSpec("mySpec")  
//		        .build();  
//		CloseableHttpClient httpclient = HttpClients.custom()  
//		        .setDefaultCookieSpecRegistry(reg)  
//		        .setDefaultRequestConfig(requestConfig)  
//		        .build();  
//		url = getUrl(url);
//		System.out.println(url);
		if (null == headers.get("User-Agent"))
			headers.put("User-Agent",
			        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36");
		
		headers.put("Connection", "none");
		headers.put("DNT", "1");
		headers.put("Referer", url);
		headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		// 不压缩：但是服务器还是压缩了
//		headers.put("Accept-Encoding", "none");
		HttpUriRequest post = null;
		try
		{
			HttpResponse httpresponse = null;
			HttpEntity formEntiry = null;
			doReport("\nRequest headers：", sbProcess);
			// 设置登录参数
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			if (null != p)
			{
				post = p;
			}

			if (null != key && null != value && key.length() > 0 && value.length() > 0)
				map.put(key, value);
			if (null != map && 0 < map.size())
			{
				if (null == post)
					post = new HttpPost(url);
				MultipartEntityBuilder k = null;
				ContentType p1 = null;
				if (bMultipart)
				{
					k = MultipartEntityBuilder.create().setCharset(Charset.forName(charset)).setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					p1 = ContentType.create("text/html", Charset.forName(charset));
				}
				for (String sk : map.keySet())
				{
					if (bMultipart)
					{
						if (map.get(sk) instanceof File)
							k.addPart(sk, new FileBody((File) map.get(sk)));
						else
							k.addPart(sk, new StringBody(String.valueOf(map.get(sk)), p1));
					} else
						parameters.add(new BasicNameValuePair(sk, String.valueOf(map.get(sk))));
				}
				if (bMultipart)
					formEntiry = k.build();
				else
					formEntiry = new UrlEncodedFormEntity(parameters);
				((HttpPost) post).setEntity(formEntiry);
			} else if (null == post)
			{
				post = new HttpGet(url);
			}
			
			// xiatian 2015-12-24 增加对https的支持
			if(url.startsWith("https"))
			{
				SSLContext ctx = SSLContexts.createSystemDefault();
	            SSLConnectionSocketFactory fac = new SSLConnectionSocketFactory(ctx, new String[] { "TLSv1" }, null,
	                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	            httpclient = HttpClientBuilder.create().setSSLSocketFactory(fac).build();
			}
			doReport(post, sbProcess);
			if (null != szCookie)
				post.addHeader("Cookie", szCookie);
			if (null != headers && 0 < headers.size())
			{
				Iterator<?> iter = headers.entrySet().iterator();
				while (iter.hasNext())
				{
					Entry<String, String> e = (Entry<String, String>) iter.next();
					post.setHeader(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
				}
			}
			doReport(post.getAllHeaders(), sbProcess);
			// 链接超时
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);   
			// 读取超时
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			httpresponse = httpclient.execute(post);

			doReport("\n\n提交的参数：\n", sbProcess);
			doReport(map, sbProcess);

			doReport("\nResponse headers：", sbProcess);

			// 登录
			if (null != headers)
			{
				headers.clear();
				StatusLine sl = httpresponse.getStatusLine();
				headers.put(sl.getStatusCode() + "", post.getMethod() + " " + url + " " + sl.getProtocolVersion());
				if (null != szCookie)
					headers.put("Cookie", szCookie);

				Header[] h = httpresponse.getAllHeaders();
				// 相应状态码
				doReport(sl, sbProcess);
				// 相应头信息
				doReport(h, sbProcess);
				// String szTmp1 = sbProcess.toString();
				// sbProcess.delete(0, sbProcess.length());
				// Matcher m1 = pSafeGene_Msg.matcher(szTmp1);
				// if(m1.find())
				// {
				// m1.appendReplacement(sbProcess,
				// java.net.URLDecoder.decode(m1.group(0), "UTF-8"));
				// }
				// m1.appendTail(sbProcess);

				for (Header header : h)
				{
					headers.put(header.getName(), header.getValue());
				}
				headers.put("Code", "" + sl.getStatusCode());
				headers.put("Status Code", "" + sl.getStatusCode());
				if (null != headers.get("Set-Cookie"))
				{
					szCookie = headers.get("Set-Cookie");
				}
			}
			if (null != sbContent)
			{
				sbContent.delete(0, sbContent.length());
				String szCharset = headers.get("Content-Type");
				if(null != szCharset)
					szCharset = checkCharset(szCharset.trim().replaceAll(".*?charset=", ""),  szCharset);
				else szCharset = "UTF-8";
				getResponse(httpresponse, sbContent, out1, szCharset, headers);
			}
			if (null != sbContent && 0 < sbContent.length())
			{
				doReport("\nResponse Body：", sbProcess);
				doReport(sbContent, sbProcess);
			}

		} catch (Throwable e1)
		{
			// TestHashCodeParm.writeFile("/Volumes/MyWork/MyWork/sfTester/errUrl.txt",
			// url);
			System.err.println("测试url(" + url + ")发生异常：\n");
			e1.printStackTrace();
			doReport("测试url(" + url + ")发生异常：\n", sbProcess);
			doReport(e1, sbProcess);
			throw e1;
			// doPost(url, key, value, map, headers, sbContent,
			// bMultipart, p, sbProcess);
			// if (null != e1 && null != e1.getMessage() && -1 ==
			// e1.getMessage().indexOf("Content-Length header already present"))
			// e1.printStackTrace();
		} finally
		{
			// 作废
			if (null != post)
				post.abort();
		}
	}

	Pattern	pSafeGene_Msg	= Pattern.compile("(SafeGene_Msg[^\\r\\n]*?)", Pattern.MULTILINE);

	/**
	 * 允许设置多段提交标志
	 * 
	 * @param url
	 * @param key
	 * @param value
	 * @param map
	 * @param headers
	 * @param sbContent
	 * @param bMultipart
	 * @throws Throwable 
	 */
	public void doPost(String url, String key, String value, Map<String, Object> map, Map<String, String> headers, StringBuffer sbContent,
	        boolean bMultipart) throws Throwable
	{
		doPost(url, null, null, map, headers, sbContent, bMultipart, null, null);
	}

	/**
	 * 提交post、get数据到服务器,将返回的Header信息存储到headers Map中,返回的HTML内存存储到sbContent中
	 * 
	 * @param url
	 * @param key
	 * @param value
	 * @param map
	 *            有数据就用post
	 * @param headers
	 * @param sbContent
	 */
	public void doPost(String url, String key, String value, Map<String, Object> map, Map<String, String> headers, StringBuffer sbContent) throws Throwable
	{
		doPost(url, key, value, map, headers, sbContent, false, null, null);
	}

	/**
	 * 采用多段提交，允许传入post实例
	 * 
	 * @param url
	 * @param map
	 * @param headers
	 * @param sbContent
	 * @param posts
	 */
	public void doPost(String url, Map<String, Object> map, Map<String, String> headers, StringBuffer sbContent, HttpUriRequest posts) throws Throwable
	{
		doPost(url, null, null, map, headers, sbContent, true, posts, null);
	}

	/**
	 * Ta3模拟登录
	 * 
	 * @param url
	 * @param map
	 * @param headers
	 * @param sbContent
	 */
	public void loginTa3(String url, Map<String, Object> map, Map<String, String> headers, StringBuffer sbContent, StringBuffer sbProcess) throws Throwable
	{
		// utt.doPost(url + "/j_spring_security_check", "r", Math.random()+"",
		// map, headers, sbContent);
		headers.clear();
		doPost(url + "/loginSuccessAction.do", "", "", null, headers, sbContent, true, null, sbProcess);
		headers.clear();
		doPost(url + "/indexAction.do", "", "", null, headers, sbContent, true, null, sbProcess);
	}

}
