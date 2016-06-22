package com.mtx.xiatian.evolvedata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mtx.face.IMyResultHandler;

public class MyHashMap extends HashMap  implements Serializable{

	private static final long serialVersionUID = -8417856416047383668L;

	public synchronized  Object get(String s) {
		String []a = String.valueOf(s).split("");
		Map m = this;
		Object o = null;
		for(String k:a)
		{
			if(0 == k.length())continue;
			if(this ==m)
				o = super.get(k);
			else o = m.get(k);
			if(o instanceof Map)
				m =  (Map)o;
			if(null == o)break;
		}
		return o;
	}

	public synchronized Object put(String k1, Object v) {
		String []a = String.valueOf(k1).split("");
		Map m = this,m1 = null;
		Object o = null;
		String k = null;
		for(int i = 0, j = a.length - 1; i < j; i++)
		{
			k = a[i];
			if(0 == k.length())continue;
			if(this ==m)
				o = super.get(k);
			else o = m.get(k);
			if(null == o)
			{
				m.put(k, m1 = new HashMap());
				m = m1;
				continue;
			}
			if(o instanceof Map)
				m =  (Map)o;
		}
		if(null != m)
			m.put(a[a.length - 1], v);
		return v;
	}
	
	public static String cacheFileName = "MyHashMap.bin";
	
	public static void initMapFile()
	{
		String s = "11602011";
		final Map m = new MyHashMap();	
		m.put(s, "1");
		
		MyQDataInfo.sdb.queryForList("SELECT url FROM url", new IMyResultHandler(){
			public void doResult(Map<String, Object> m1) {
				String url1 = (String)m1.get("url");
				String qq = url1.substring(url1.lastIndexOf("/") + 1, url1.lastIndexOf("."));
				m.put(qq, "1");
//				if("100027997".equals(qq))
//				System.out.println(qq);
			}});
		
		MyQDataInfo.writeObject(cacheFileName, m);
	}
	
	public Object remove(String s) {
		String []a = String.valueOf(s).split("");
		Map m = this;
		Object o = null;
		for(String k:a)
		{
			if(0 == k.length())continue;
			if(this ==m)
				o = super.get(k);
			else o = m.get(k);
			if(o instanceof Map)
				m =  (Map)o;
			if(null == o)break;
		}
		m.remove(o);
		return o;
	}

	public static void main(String[] args) {
//		initMapFile();
//		Map m = (Map)MyQDataInfo.readObject(cacheFileName);
//		String s = (String)AC01Info.mYjclQQ.get("3135773");
//		System.out.println("3135773: " + s);
	}

}
