package com.mtx.xiatian.hacker.metasploit;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 异步处理结果
 * @author xiatian
 *
 */
public class MetasploitRest implements Runnable
{

	private InputStream	in;
	private OutputStream	out;

	public MetasploitRest(InputStream input,  OutputStream out)
	{
		this.in = input;
		this.out = out;
	}

	public void run()
	{
		try
		{
			in = new BufferedInputStream(in);
			byte[] b = new byte[1024];
			int i = 0;
			long t = System.currentTimeMillis();
			while (0 < (i = in.read(b, 0, b.length)))
			{
				out.write(b, 0, i);
			}
			System.out.println("结束");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally{
			try
			{
				if (null != in)
					in.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
