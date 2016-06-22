package com.mtx.xiatian.hacker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池的简单封装
 * @author xiatian
 */
public class MyExecutors
{
	private static MyExecutors _ins = new MyExecutors();
	private ExecutorService cachedThreadPool;
	public MyExecutors()
	{
		cachedThreadPool = Executors.newCachedThreadPool();;
	}
	
	public static MyExecutors getInstance()
	{
		return _ins;
	}

	/**
	 * 超时时间
	 */
	long timeOut = Long.MAX_VALUE;
	
	public void setTimeOut(long l)
	{
		this.timeOut = l;
	}
	
	public MyExecutors add(Runnable... a)
	{
	    cachedThreadPool = Executors.newCachedThreadPool();
		for (Runnable r : a)
		{
			cachedThreadPool.execute(r);
		}
//		while (!(cachedThreadPool.isShutdown() || cachedThreadPool.isTerminated()))
//		{
//			try
//			{
//				Thread.sleep(133);
//			} catch (InterruptedException e)
//			{
//				e.printStackTrace();
//				cachedThreadPool.shutdown();
//				break;
//			}
//		}
		return this;
	}

}
