package com.mtx.xiatian.db;

import java.util.ArrayList;
import java.util.List;

public class ExecuteScript
{

	public static void main(String[] args)
	{

		System.exit(ececute(args[0], true));

	}

	public static int ececute(String cmd, boolean isNeedPrint)
	{
		Process proc = null;
		int result = -1;
		try
		{
			List<StreamReader> list = new ArrayList<StreamReader>();
			proc = Runtime.getRuntime().exec(cmd);
			StreamReader error = new StreamReader(proc.getErrorStream(), "Error");
			StreamReader output = new StreamReader(proc.getInputStream(), "Output");

			if (isNeedPrint)
			{

				list.add(error);
				list.add(error);
			}

			error.start();
			output.start();
			if (isNeedPrint)
			{
				for (StreamReader sr : list)
				{
					sr.join();
				}
			}
			result = proc.waitFor();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			proc.destroy();
		}

		return result;
	}

}