# MTX_HackerTools
1、weblogic java deserialize （批量java 反序列化漏洞渗透测试）
2、struts2 Remote Code Execution（批量struts2漏洞渗透测试）

# 代码更新
git clone https://github.com/hktalent/MTX_HackerTools.git

cd MTX_HackerTools
# 编译环境
jdk 1.6
# 使用
com.mtx.xiatian.Main
自己参照函数修改了
/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// 如果当前环境有代理，就使用系统设置的代理
		System.setProperty("java.net.useSystemProxies", "true");
		
		testWeblogic_Struts2(
				"60.173.247.15;222.168.33.117;58.210.227.26;218.62.83.78;183.131.128.215;222.168.33.108;124.42.10.247", 
				"7001;8001;7001;8080;80;9001;80,7001", 
				"/login");
	}

# 作者
@Hktalent3135773
