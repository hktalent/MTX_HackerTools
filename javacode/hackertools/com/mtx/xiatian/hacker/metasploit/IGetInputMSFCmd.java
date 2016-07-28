package com.mtx.xiatian.hacker.metasploit;

import java.io.OutputStream;

/**
 * 获取输入
 * @author xiatian
 *
 */
public interface IGetInputMSFCmd
{
	/**
	 * 命令回调，直到null
	 * @return
	 */
	public String getCmd();
	
	/**
	 * 输出流回调
	 * @return
	 */
	public OutputStream getOutputStream();
}
